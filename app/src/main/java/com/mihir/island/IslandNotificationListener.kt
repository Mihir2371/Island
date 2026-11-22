package com.mihir.island

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.app.Notification
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.content.ComponentName

object IslandActivityContent {
    @Volatile var notificationTitle: String? = null
    @Volatile var notificationText: String? = null
    @Volatile var sourceApp: String? = null
    @Volatile var mediaTitle: String? = null
    @Volatile var mediaArtist: String? = null
    @Volatile var mediaPlaying: Boolean = false
    @Volatile var activeMedia: MediaController? = null
    fun togglePlayback() {
        val c = activeMedia ?: return
        if (c.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING) c.transportControls.pause()
        else c.transportControls.play()
    }
    fun skipNext() { activeMedia?.transportControls?.skipToNext() }
    fun skipPrevious() { activeMedia?.transportControls?.skipToPrevious() }
}

class IslandNotificationListener : NotificationListenerService() {
    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateMedia(controllers?.firstOrNull())
    }
    private val component by lazy { ComponentName(this, IslandNotificationListener::class.java) }
    private var sessions: MediaSessionManager? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            sessions = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            sessions?.addOnActiveSessionsChangedListener(sessionListener, component)
            updateMedia(sessions?.getActiveSessions(component)?.firstOrNull())
        } catch (_: SecurityException) { }
    }
    override fun onListenerDisconnected() {
        try { sessions?.removeOnActiveSessionsChangedListener(sessionListener) } catch (_: Exception) {}
        sessions = null
        IslandActivityContent.activeMedia = null
        IslandActivityContent.mediaTitle = null
        IslandActivityContent.mediaArtist = null
        super.onListenerDisconnected()
    }
    private fun updateMedia(controller: MediaController?) {
        IslandActivityContent.activeMedia = controller
        val metadata = controller?.metadata
        IslandActivityContent.mediaTitle = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
        IslandActivityContent.mediaArtist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        IslandActivityContent.mediaPlaying = controller?.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        notifyOverlay()
    }
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
    private fun notifyOverlay() {
        try { startService(Intent(this, IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_REFRESH)) }
        catch (_: IllegalStateException) { }
    }
}
