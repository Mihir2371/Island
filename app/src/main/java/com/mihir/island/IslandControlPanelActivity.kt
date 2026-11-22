package com.mihir.island

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PanelBg = Color(0xFF09090B)
private val PanelCard = Color(0xFF19191D)
private val PanelAccent = Color(0xFFB9F5D0)

class IslandControlPanelActivity : ComponentActivity() {
    private var torchOn by mutableStateOf(false)
    private var torchId: String? = null
    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) setTorch(!torchOn.value)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = PanelBg, surface = PanelCard, primary = PanelAccent)) {
                ControlPanel(torchOn = torchOn.value, onTorch = { toggleTorch() }, onWifi = { openWifiPanel() },
                    onHotspot = { openSettings(Settings.ACTION_TETHER_SETTINGS, Settings.ACTION_SETTINGS) },
                    onNfc = { openSettings(Settings.ACTION_NFC_SETTINGS, Settings.ACTION_SETTINGS) })
            }
        }
    }
    private fun toggleTorch() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermission.launch(Manifest.permission.CAMERA); return
        }
        setTorch(!torchOn.value)
    }
    private fun setTorch(enabled: Boolean) {
        try {
            val cm = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = torchId ?: cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
            if (id == null) return
            torchId = id; cm.setTorchMode(id, enabled); torchOn.value = enabled
        } catch (_: Exception) { torchOn.value = false }
    }
    private fun openWifiPanel() {
        try { if (android.os.Build.VERSION.SDK_INT >= 29) startActivity(Intent(Settings.Panel.ACTION_WIFI)) else startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
        catch (_: Exception) { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
    }
    private fun openSettings(primary: String, fallback: String) { try { startActivity(Intent(primary)) } catch (_: Exception) { startActivity(Intent(fallback)) } }
}
@Composable
private fun ControlPanel(torchOn: Boolean, onTorch: () -> Unit, onWifi: () -> Unit, onHotspot: () -> Unit, onNfc: () -> Unit) {
    Column(Modifier.fillMaxSize().background(PanelBg).padding(20.dp)) {
        Spacer(Modifier.height(8.dp)); Text("Quick Controls", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Island · Connectivity & devices", color = Color(0xFF9B9BA2), fontSize = 13.sp); Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ControlCard("◉", "Wi-Fi", "Networks & connect", false, onWifi, Modifier.weight(1f))
            ControlCard("⌁", "Hotspot", "Share internet", false, onHotspot, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ControlCard("⌗", "NFC", "Tap to open settings", false, onNfc, Modifier.weight(1f))
            ControlCard("ϟ", "Flashlight", if (torchOn) "On" else "Off", torchOn, onTorch, Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth().background(PanelCard, RoundedCornerShape(22.dp)).padding(16.dp)) {
            Text("Android system controls", color = PanelAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp))
            Text("Wi-Fi, hotspot and NFC open Android's native panel/settings where OS restrictions apply. Flashlight can toggle here.", color = Color(0xFFB6B6BC), fontSize = 12.sp, lineHeight = 18.sp)
        }
        Spacer(Modifier.weight(1f)); Text("Island dark popover · system settings remain in control", color = Color(0xFF77777E), fontSize = 11.sp)
    }
}
@Composable
private fun ControlCard(icon: String, title: String, subtitle: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.height(142.dp).background(if (active) Color(0xFF254234) else PanelCard, RoundedCornerShape(24.dp)).clickable(onClick).padding(16.dp)) {
        Text(icon, color = if (active) PanelAccent else Color.White, fontSize = 24.sp); Spacer(Modifier.height(12.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Color(0xFF9B9BA2), fontSize = 11.sp)
    }
}