package com.example.touchguard

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class DeadZoneOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var deadZoneView: View? = null
    private var startY = 0f

    companion object {
        private const val CHANNEL_ID = "touch_guard_channel"
        private const val NOTIFICATION_ID = 2001
        var accessibilityInstance: WhatsAppScrollHelperService? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            startForegroundServiceSafely()
            initDeadZone()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundServiceSafely() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Touch Guard Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }

        // Standard system icon avoids resource crashes if drawables are missing
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Touch Guard Active")
            .setContentText("Shielding swipe-to-talk area")
            .setSmallIcon(android.R.drawable.stat_notify_sync_noanim)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        // Classic startForeground call: fully compatible across Android 10, 11, 12, and 13
        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDeadZone() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        deadZoneView = View(this).apply {
            // Red color for visual verification while testing
            setBackgroundColor(Color.argb(100, 255, 0, 0))
            isClickable = true
            isFocusable = false

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startY = event.rawY
                        true // Intercept and consume initial touch
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val deltaY = startY - event.rawY
                        // Any upward drag exceeding 40px triggers forward scrolling via Accessibility
                        if (deltaY > 40) {
                            accessibilityInstance?.performChatScroll()
                        }
                        true
                    }
                    else -> true
                }
            }
        }

        // Set height to cover the lower input/mic strip (~80dp covers the bottom control bar)
        val heightPx = dpToPx(85f)

        val params = WindowManager.LayoutParams(
            // Span the full width of the screen across the bottom
            WindowManager.LayoutParams.MATCH_PARENT,
            heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // Drop FLAG_NOT_TOUCH_MODAL so touches inside the strip are never passed to WhatsApp
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            x = 0
            y = 0
        }

        try {
            windowManager?.addView(deadZoneView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        deadZoneView?.let {
            windowManager?.removeView(it)
            deadZoneView = null
        }
    }
}