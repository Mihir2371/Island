package com.mihir.island.qs

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings
import com.mihir.island.IslandOverlayService

abstract class IslandToggleTile(private val prefKey:String,private val tileLabel:String):TileService() {
    private val prefs by lazy { getSharedPreferences("island_state",MODE_PRIVATE) }
    override fun onStartListening(){super.onStartListening(); sync()}
    override fun onClick(){
        super.onClick()
        val value=!prefs.getBoolean(prefKey,false)
        prefs.edit().putBoolean(prefKey,value).apply()
        when(prefKey){
            "island_enabled" -> if(value && Settings.canDrawOverlays(this)) startService(Intent(this,IslandOverlayService::class.java)) else stopService(Intent(this,IslandOverlayService::class.java))
            "island_hidden","priority_mode","reduce_motion","activities_paused" -> startService(Intent(this,IslandOverlayService::class.java).setAction(IslandOverlayService.ACTION_REFRESH))
            // Timer and recorder tiles deliberately remain request flags until their consent/activity flows are added.
        }
        sync()
    }
    private fun sync(){
        val active=prefs.getBoolean(prefKey,false)
        qsTile?.state=if(active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.label=tileLabel
        qsTile?.subtitle=if(active) "On" else "Off"
        qsTile?.updateTile()
    }
}
class IslandTileService:IslandToggleTile("island_enabled","Island")
class HideIslandTileService:IslandToggleTile("island_hidden","Hide Island")
class PriorityTileService:IslandToggleTile("priority_mode","Priority")
class ReduceMotionTileService:IslandToggleTile("reduce_motion","Reduce Motion")
class PauseActivitiesTileService:IslandToggleTile("activities_paused","Pause activities")
class QuickTimerTileService:IslandToggleTile("quick_timer_active","Quick timer")
class RecorderTileService:IslandToggleTile("recorder_tile_requested","Recorder")
