package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.R
import com.example.data.AppPreferences

class FloatingWidgetService : android.app.Service() {

    private lateinit var windowManager: WindowManager
    private var floatView: View? = null
    private var menuView: View? = null

    private lateinit var prefs: AppPreferences
    private var isHoldingData = false
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var remainingTimeMs = 0L

    override fun onBind(intent: Intent?): android.os.IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        if (canDrawOverlays()) {
            showFloatingButton()
        }
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun showFloatingButton() {
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        
        // Custom created layout programmatically or via inflate
        val view = layoutInflater.inflate(R.layout.layout_floating_widget, null)
        floatView = view

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        val skullBtn = view.findViewById<View>(R.id.btn_skull_icon)
        val timerTxt = view.findViewById<TextView>(R.id.txt_floating_timer)
        val cardBg = view.findViewById<CardView>(R.id.card_floating_container)

        updateFloatingStateUI(cardBg, timerTxt)

        // Touch & Drag listener with 0.9s (900ms) long-press detection
        skullBtn.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isLongPressHandled = false

            private val longPressRunnable = Runnable {
                isLongPressHandled = true
                showFloatingMenu(params.x, params.y + 160)
            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isLongPressHandled = false
                        handler.postDelayed(longPressRunnable, 900) // 0.9s threshold
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val diffX = (event.rawX - initialTouchX).toInt()
                        val diffY = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(diffX) > 10 || Math.abs(diffY) > 10) {
                            handler.removeCallbacks(longPressRunnable)
                        }
                        params.x = initialX + diffX
                        params.y = initialY + diffY
                        windowManager.updateViewLayout(floatView, params)
                        menuView?.let { m ->
                            val mParams = m.layoutParams as WindowManager.LayoutParams
                            mParams.x = params.x
                            mParams.y = params.y + 180 // maintain spaced gap
                            windowManager.updateViewLayout(m, mParams)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacks(longPressRunnable)
                        if (!isLongPressHandled) {
                            val diffX = Math.abs(event.rawX - initialTouchX)
                            val diffY = Math.abs(event.rawY - initialTouchY)
                            if (diffX < 10 && diffY < 10) {
                                // Toggle ON/OFF
                                isHoldingData = !isHoldingData
                                LocalVpnService.setHoldState(isHoldingData, prefs.holdDurationMs)
                                updateFloatingStateUI(cardBg, timerTxt)
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatView, params)
    }

    private fun updateFloatingStateUI(cardBg: CardView, timerTxt: TextView) {
        if (isHoldingData) {
            cardBg.setCardBackgroundColor(0xFF22C55E.toInt()) // Green ON
            startTimerCountdown(timerTxt)
        } else {
            cardBg.setCardBackgroundColor(0xFFEF4444.toInt()) // Red OFF
            stopTimerCountdown(timerTxt)
        }
    }

    private fun startTimerCountdown(timerTxt: TextView) {
        stopTimerCountdown(timerTxt)
        remainingTimeMs = prefs.holdDurationMs

        timerRunnable = object : Runnable {
            override fun run() {
                if (remainingTimeMs > 0) {
                    val sec = (remainingTimeMs / 1000.0)
                    timerTxt.text = String.format("%.1fs", sec)
                    remainingTimeMs -= 100
                    handler.postDelayed(this, 100)
                } else {
                    timerTxt.text = "0s"
                    isHoldingData = false
                    LocalVpnService.setHoldState(false, prefs.holdDurationMs)
                    floatView?.let { v ->
                        val cardBg = v.findViewById<CardView>(R.id.card_floating_container)
                        val tTxt = v.findViewById<TextView>(R.id.txt_floating_timer)
                        updateFloatingStateUI(cardBg, tTxt)
                    }
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopTimerCountdown(timerTxt: TextView) {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerTxt.text = "OFF"
    }

    private fun showFloatingMenu(btnX: Int, btnY: Int) {
        if (menuView != null) {
            try { windowManager.removeView(menuView) } catch (e: Exception) {}
            menuView = null
        }

        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.layout_floating_menu, null)
        menuView = view

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = btnX
            y = btnY // Placed below with space
        }

        val seekBar = view.findViewById<SeekBar>(R.id.seekbar_duration)
        val textValue = view.findViewById<TextView>(R.id.txt_seekbar_val)
        val closeBtn = view.findViewById<View>(R.id.btn_close_menu)

        // Range: 100ms to 2000ms (Max 2s)
        val currentMs = prefs.holdDurationMs.toInt().coerceIn(100, 2000)
        seekBar.max = 2000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.min = 100
        }
        seekBar.progress = currentMs

        fun updateLabel(ms: Int) {
            textValue.text = if (ms >= 1000) {
                String.format("%.1fs", ms / 1000.0)
            } else {
                "${ms}ms (${String.format("%.1f", ms / 1000.0)}s)"
            }
        }
        updateLabel(currentMs)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val actualProgress = progress.coerceAtLeast(100)
                updateLabel(actualProgress)
                prefs.holdDurationMs = actualProgress.toLong()
                if (isHoldingData) {
                    LocalVpnService.setHoldState(true, actualProgress.toLong())
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        closeBtn.setOnClickListener {
            dismissMenu()
        }

        windowManager.addView(menuView, menuParams)
    }

    private fun dismissMenu() {
        menuView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
            menuView = null
        }
    }

    override fun onDestroy() {
        dismissMenu()
        floatView?.let {
            try { windowManager.removeView(it) } catch (e: Exception) {}
            floatView = null
        }
        super.onDestroy()
    }
}
