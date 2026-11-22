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
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class IslandOverlayService : Service() {
    private lateinit var manager: WindowManager
    private var pill: TextView? = null
    private val prefs by lazy { getSharedPreferences("island_state", MODE_PRIVATE) }
    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(41, notification())
        manager=getSystemService(WINDOW_SERVICE) as WindowManager
        refresh()
    }
    private fun refresh() {
        removePill()
        if (!prefs.getBoolean("island_enabled",false) || prefs.getBoolean("island_hidden",false)) return
        val label=TextView(this).apply {
            text="●   ISLAND       〰"
            setTextColor(Color.WHITE); textSize=12f; gravity=Gravity.CENTER
            setPadding(dp(18),dp(8),dp(18),dp(8))
            background=GradientDrawable().apply { setColor(Color.BLACK); cornerRadius=dp(32).toFloat() }
            setOnClickListener {
                text=if(text.toString().contains("ISLAND")) "♫   Island activity\nTap to collapse" else "●   ISLAND       〰"
                updateLayout()
            }
        }
        pill=label
        addPill()
    }
    private fun addPill() {
        val view=pill ?: return
        val expanded=view.text.toString().contains("activity")
        val params=WindowManager.LayoutParams(
            dp(if(expanded) 286 else 176),dp(if(expanded) 76 else 42),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL; y=dp(10) }
        try { manager.addView(view,params) } catch (_: SecurityException) { stopSelf() }
    }
    private fun updateLayout() { removePill(); addPill() }
    private fun removePill() { pill?.let { try { manager.removeView(it) } catch (_: IllegalArgumentException) {} }; pill=null }
    private fun dp(value:Int)=(value*resources.displayMetrics.density).toInt()
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
        when(intent?.action) {
            ACTION_STOP -> { removePill(); stopSelf() }
            else -> refresh()
        }
        return START_STICKY
    }
    private fun createChannel() {
        if(Build.VERSION.SDK_INT>=26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("island_overlay","Island overlay",NotificationManager.IMPORTANCE_LOW))
    }
    private fun notification():Notification {
        val builder=if(Build.VERSION.SDK_INT>=26) Notification.Builder(this,"island_overlay") else Notification.Builder(this)
        return builder.setContentTitle("Island is running").setContentText("Floating capsule overlay active").setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(true).build()
    }
    override fun onDestroy(){ removePill(); super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
    companion object { const val ACTION_REFRESH="com.mihir.island.REFRESH"; const val ACTION_STOP="com.mihir.island.STOP" }
}
