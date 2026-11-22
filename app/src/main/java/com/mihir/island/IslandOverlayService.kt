package com.mihir.island

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs

class IslandOverlayService : Service() {
    private lateinit var manager: WindowManager
    private var pill: TextView? = null
    private var params: WindowManager.LayoutParams? = null
    private var expanded = false
    private var animator: ValueAnimator? = null
    private var timer: CountDownTimer? = null
    private var timerEndAt = 0L
    private var dragStartX = 0f
    private var dragStartY = 0f
    private var initialX = 0
    private var initialY = 0
    private var dragged = false
    private val prefs by lazy { getSharedPreferences("island_state", MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(41, notification())
        manager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (prefs.getBoolean("quick_timer_active", false)) restoreTimer()
        refresh()
    }

    private fun refresh() {
        removePill()
        if ((!prefs.getBoolean("island_enabled", false) && !prefs.getBoolean("quick_timer_active", false)) || prefs.getBoolean("island_hidden", false)) return
        expanded = false
        val label = TextView(this).apply {
            text = timerLabel()
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(8), dp(18), dp(8))
            background = GradientDrawable().apply { setColor(Color.BLACK); cornerRadius = dp(32).toFloat() }
            elevation = dp(12).toFloat()
            setOnTouchListener { _, event -> handleTouch(event) }
        }
        pill = label
        addPill()
    }

    private fun addPill() {
        val view = pill ?: return
        val safeTop = if (Build.VERSION.SDK_INT >= 28) {
            try { manager.defaultDisplay.cutout?.safeInsetTop ?: 0 } catch (_: Exception) { 0 }
        } else 0
        val lp = WindowManager.LayoutParams(
            dp(176), dp(42), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = maxOf(dp(8), safeTop + dp(4)) }
        params = lp
        try { manager.addView(view, lp) } catch (_: SecurityException) { stopSelf() }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val view = pill ?: return false
        val lp = params ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragged = false
                dragStartX = event.rawX
                dragStartY = event.rawY
                initialX = lp.x
                initialY = lp.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - dragStartX).toInt()
                val dy = (event.rawY - dragStartY).toInt()
                if (abs(dx) > dp(5) || abs(dy) > dp(5)) dragged = true
                if (dragged) {
                    lp.x = initialX - dx
                    lp.y = maxOf(dp(4), initialY + dy)
                    try { manager.updateViewLayout(view, lp) } catch (_: Exception) {}
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!dragged) toggleExpanded()
                return true
            }
        }
        return true
    }

    private fun toggleExpanded() {
        expanded = !expanded
        val view = pill ?: return
        view.text = if (expanded) expandedLabel() else timerLabel()
        val lp = params ?: return
        val targetW = dp(if (expanded) 286 else 176)
        val targetH = dp(if (expanded) 78 else 42)
        animator?.cancel()
        if (prefs.getBoolean("reduce_motion", false)) {
            lp.width = targetW; lp.height = targetH
            try { manager.updateViewLayout(view, lp) } catch (_: Exception) {}
        } else {
            val startW = lp.width; val startH = lp.height
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 260; interpolator = DecelerateInterpolator()
                addUpdateListener { animation ->
                    val f = animation.animatedValue as Float
                    lp.width = (startW + (targetW - startW) * f).toInt()
                    lp.height = (startH + (targetH - startH) * f).toInt()
                    try { manager.updateViewLayout(view, lp) } catch (_: Exception) {}
                }
                start()
            }
        }
    }

    private fun timerLabel(): String {
        if (timerEndAt > 0L && prefs.getBoolean("quick_timer_active", false)) {
            val sec = ((timerEndAt - System.currentTimeMillis()).coerceAtLeast(0L) / 1000L)
            return "◷   %02d:%02d".format(sec / 60, sec % 60)
        }
        return "●   ISLAND       〰"
    }
    private fun expandedLabel(): String {
        if (timerEndAt > 0L && prefs.getBoolean("quick_timer_active", false)) {
            val sec = ((timerEndAt - System.currentTimeMillis()).coerceAtLeast(0L) / 1000L)
            return "◷   Quick timer\n%02d:%02d remaining".format(sec / 60, sec % 60)
        }
        val title = IslandActivityContent.notificationTitle
        val body = IslandActivityContent.notificationText
        if (!title.isNullOrBlank()) {
            val cleanTitle = title.take(42)
            val cleanBody = body?.take(72).orEmpty()
            return if (cleanBody.isNotBlank()) "✦   $cleanTitle\n$cleanBody" else "✦   $cleanTitle\nTap to collapse"
        }
        return "♫   Island activity\nTap to collapse"
    }
    private fun startQuickTimer(durationMs: Long = prefs.getLong("timer_remaining_ms", 300_000L)) {
        timer?.cancel()
        val duration = durationMs.coerceAtLeast(1_000L)
        timerEndAt = System.currentTimeMillis() + duration
        prefs.edit().putBoolean("quick_timer_active", true).putLong("timer_remaining_ms", duration).apply()
        timer = object : CountDownTimer(duration, 1000L) {
            override fun onTick(left: Long) {
                prefs.edit().putLong("timer_remaining_ms", left).apply()
                pill?.text = if (expanded) expandedLabel() else timerLabel()
            }
            override fun onFinish() {
                timerEndAt = 0L
                prefs.edit().putBoolean("quick_timer_active", false).putLong("timer_remaining_ms", 300_000L).apply()
                pill?.text = "✓   Timer finished"
            }
        }.start()
        refresh()
    }
    private fun restoreTimer() {
        val left = prefs.getLong("timer_remaining_ms", 300_000L)
        if (left > 0L) startQuickTimer(left) else stopQuickTimer()
    }
    private fun stopQuickTimer() {
        timer?.cancel(); timer = null; timerEndAt = 0L
        prefs.edit().putBoolean("quick_timer_active", false).putLong("timer_remaining_ms", 300_000L).apply()
        refresh()
    }

    private fun removePill() {
        animator?.cancel()
        pill?.let { try { manager.removeView(it) } catch (_: IllegalArgumentException) {} }
        pill = null; params = null
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TIMER_START -> startQuickTimer(300_000L)
            ACTION_TIMER_STOP -> stopQuickTimer()
            ACTION_STOP -> { removePill(); stopSelf() }
            else -> refresh()
        }
        return START_STICKY
    }
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel("island_overlay", "Island overlay", NotificationManager.IMPORTANCE_LOW))
    }
    private fun notification(): Notification {
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "island_overlay") else Notification.Builder(this)
        return b.setContentTitle("Island is running").setContentText("Floating capsule overlay active")
            .setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(true).build()
    }
    override fun onDestroy() { timer?.cancel(); removePill(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    companion {
        const val ACTION_REFRESH = "com.mihir.island.REFRESH"
        const val ACTION_TIMER_START = "com.mihir.island.TIMER_START"
        const val ACTION_TIMER_STOP = "com.mihir.island.TIMER_STOP"
        const val ACTION_STOP = "com.mihir.island.STOP"
    }
}
