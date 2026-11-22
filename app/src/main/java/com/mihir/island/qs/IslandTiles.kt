package com.mihir.island.qs

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** Lightweight local-state QS tiles; controls requiring consent are placeholders for now. */
abstract class IslandToggleTile(private val prefKey: String, private val title: String) : TileService() {
    private val prefs by lazy { getSharedPreferences("island_state", MODE_PRIVATE) }
    override fun onStartListening() { super.onStartListening(); syncTile() }
    override fun onClick() {
        super.onClick()
        val active = !prefs.getBoolean(prefKey, false)
        prefs.edit().putBoolean(prefKey, active).apply()
        syncTile()
    }
    private fun syncTile() {
        val active = prefs.getBoolean(prefKey, false)
        qsTile?.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.label = title
        qsTile?.subtitle = if (active) "On" else "Off"
        qsTile?.updateTile()
    }
}
class IslandTileService : IslandToggleTile("island_enabled", "Island")
class HideIslandTileService : IslandToggleTile("island_hidden", "Hide Island")
class PriorityTileService : IslandToggleTile("priority_mode", "Priority")
class ReduceMotionTileService : IslandToggleTile("reduce_motion", "Reduce Motion")
class PauseActivitiesTileService : IslandToggleTile("activities_paused", "Pause Activities")
class QuickTimerTileService : IslandToggleTile("quick_timer_active", "Quick Timer")
class RecorderTileService : IslandToggleTile("recorder_tile_requested", "Recorder")
