package com.mihir.island

import android.content.Context

class IslandPreferences(context: Context) {
    private val store = context.getSharedPreferences("island_state", Context.MODE_PRIVATE)
    var reduceMotion: Boolean
        get() = store.getBoolean("reduce_motion", false)
        set(value) { store.edit().putBoolean("reduce_motion", value).apply() }
    var priorityMode: Boolean
        get() = store.getBoolean("priority_mode", false)
        set(value) { store.edit().putBoolean("priority_mode", value).apply() }
    var capsuleScale: Float
        get() = store.getFloat("capsule_scale", 1f).coerceIn(.75f, 1.35f)
        set(value) { store.edit().putFloat("capsule_scale", value.coerceIn(.75f, 1.35f)).apply() }
    var verticalOffsetDp: Int
        get() = store.getInt("capsule_offset_dp", 4).coerceIn(0, 160)
        set(value) { store.edit().putInt("capsule_offset_dp", value.coerceIn(0, 160)).apply() }
    var hideNotificationContent: Boolean
        get() = store.getBoolean("privacy_hide_notification_content", false)
        set(value) { store.edit().putBoolean("privacy_hide_notification_content", value).apply() }
}