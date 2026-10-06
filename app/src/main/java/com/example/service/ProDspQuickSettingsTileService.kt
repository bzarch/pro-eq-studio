package com.example.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.audio.SystemAudioBridge

@RequiresApi(Build.VERSION_CODES.N)
class ProDspQuickSettingsTileService : TileService() {

    private lateinit var bridge: SystemAudioBridge

    override fun onCreate() {
        super.onCreate()
        bridge = SystemAudioBridge(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        if (tile.state == Tile.STATE_ACTIVE) {
            bridge.disableSystemDsp()
            DspBackgroundService.stopService(this)
            tile.state = Tile.STATE_INACTIVE
        } else {
            val success = bridge.enableSystemDsp()
            DspBackgroundService.startService(this)
            tile.state = if (success) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        tile.state = if (bridge.isSystemDspActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "PRO DSP"
        tile.updateTile()
    }
}
