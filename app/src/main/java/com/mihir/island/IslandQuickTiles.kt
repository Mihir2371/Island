package com.mihir.island

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.content.Intent

abstract class IslandToggleTile : TileService() {
    abstract val prefKey: String
    abstract val tileLabel: String

    override fun onStartListening() {
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val prefs = getSharedPreferences("island_controls", MODE_PRIVATE)
        val current = prefs.getBoolean(prefKey, false)
        prefs.edit().putBoolean(prefKey, !current).apply()
        refreshTile()
    }

    private fun refreshTile() {
        val enabled = getSharedPreferences("island_controls", MODE_PRIVATE).getBoolean(prefKey, false)
        qsTile?.let {
            it.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.label = tileLabel
            it.subtitle = if (enabled) "On" else "Off"
            it.updateTile()
        }
    }
}

class IslandToggleQuickTile : IslandToggleTile() {
    override val prefKey = "island_enabled"
    override val tileLabel = "Island"
}
class HideIslandQuickTile : IslandToggleTile() {
    override val prefKey = "island_hidden"
    override val tileLabel = "Hide Island"
}
class PriorityModeQuickTile : IslandToggleTile() {
    override val prefKey = "priority_mode"
    override val tileLabel = "Priority Mode"
}
class ReduceMotionQuickTile : IslandToggleTile() {
    override val prefKey = "reduce_motion"
    override val tileLabel = "Reduce Motion"
}
class PauseActivitiesQuickTile : IslandToggleTile() {
    override val prefKey = "activities_paused"
    override val tileLabel = "Pause Activities"
}

class RecorderQuickTile : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("open_feature", "recorder")
        startActivityAndCollapse(intent)
    }
    override fun onStartListening() {
        qsTile?.let { it.state = Tile.STATE_INACTIVE; it.label = "Recorder"; it.subtitle = "Open Island"; it.updateTile() }
    }
}

class TimerQuickTile : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("open_feature", "timer")
        startActivityAndCollapse(intent)
    }
    override fun onStartListening() {
        qsTile?.let { it.state = Tile.STATE_INACTIVE; it.label = "Quick Timer"; it.subtitle = "Open Island"; it.updateTile() }
    }
}
