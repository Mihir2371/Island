package com.mihir.island

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.app.Notification

object IslandActivityContent {
    @Volatile var notificationTitle: String? = null
    @Volatile var notificationText: String? = null
    @Volatile var sourceApp: String? = null
}

class IslandNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_ONGOING_EVENT != 0 && sbn.packageName == packageName) return
        val extras = n.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = (extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras?.getCharSequence(Notification.EXTRA_TEXT))?.toString()?.trim().orEmpty()
        if (title.isBlank() && text.isBlank()) return
        IslandActivityContent.notificationTitle = title.ifBlank { sbn.packageName }
        IslandActivityContent.notificationText = text
        IslandActivityContent.sourceApp = sbn.packageName
        notifyOverlay()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Keep the latest event visible until a new notification replaces it.
    }

    private fun notifyOverlay() {
        try { startService(Intent(this, IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_REFRESH)) }
        catch (_: IllegalStateException) { }
    }
}
