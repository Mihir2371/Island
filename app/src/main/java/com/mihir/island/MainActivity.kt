package com.mihir.island

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Mint = Color(0xFFB9F5D0)
private val Ink = Color(0xFF050506)
private val Card = Color(0xFF141416)

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("island_state", MODE_PRIVATE) }
    private var overlayPermissionGranted by mutableStateOf(false)
    private var notificationAccessGranted by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayPermissionGranted = Settings.canDrawOverlays(this)
        notificationAccessGranted = hasNotificationAccess()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Card, primary = Mint)) {
                IslandScreen(
                    initialEnabled = prefs.getBoolean("island_enabled", false),
                    initialPriority = prefs.getBoolean("priority_mode", false),
                    initialReduceMotion = prefs.getBoolean("reduce_motion", false),
                    overlayGranted = overlayPermissionGranted,
                    notificationAccessGranted = notificationAccessGranted,
                    onEnabledChange = { requested ->
                        if (requested && !Settings.canDrawOverlays(this)) {
                            prefs.edit().putBoolean("island_enabled", false).apply()
                            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                        } else {
                            prefs.edit().putBoolean("island_enabled", requested).apply()
                            if (requested) startService(Intent(this, IslandOverlayService::class.java))
                            else stopService(Intent(this, IslandOverlayService::class.java))
                        }
                    },
                    onPriorityChange = { prefs.edit().putBoolean("priority_mode", it).apply() },
                    onReduceMotionChange = { prefs.edit().putBoolean("reduce_motion", it).apply() },
                    onRequestOverlay = { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) },
                    onRequestNotificationAccess = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        overlayPermissionGranted = Settings.canDrawOverlays(this)
        notificationAccessGranted = hasNotificationAccess()
        if (prefs.getBoolean("island_enabled", false) && overlayPermissionGranted) {
            startService(Intent(this, IslandOverlayService::class.java))
        }
    }
    private fun hasNotificationAccess(): Boolean {
        val enabled = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners").orEmpty()
        return enabled.contains(packageName)
    }
}

@Composable
private fun IslandScreen(
    initialEnabled: Boolean, initialPriority: Boolean, initialReduceMotion: Boolean, overlayGranted: Boolean, notificationAccessGranted: Boolean,
    onEnabledChange: (Boolean) -> Unit, onPriorityChange: (Boolean) -> Unit, onReduceMotionChange: (Boolean) -> Unit,
    onRequestOverlay: () -> Unit, onRequestNotificationAccess: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(initialEnabled) }
    var reduceMotion by remember { mutableStateOf(initialReduceMotion) }
    var priority by remember { mutableStateOf(initialPriority) }
    LaunchedEffect(initialEnabled) { enabled = initialEnabled }
    LaunchedEffect(initialPriority) { priority = initialPriority }
    LaunchedEffect(initialReduceMotion) { reduceMotion = initialReduceMotion }
    val width by animateDpAsState(if (expanded) 320.dp else 180.dp, if (reduceMotion) androidx.compose.animation.core.snap() else spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy), label = "pillWidth")
    val height by animateDpAsState(if (expanded) 116.dp else 46.dp, if (reduceMotion) androidx.compose.animation.core.snap() else spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy), label = "pillHeight")
    Box(Modifier.fillMaxSize().background(Ink)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(14.dp))
            if (enabled) {
                Box(Modifier.width(width).height(height).clip(RoundedCornerShape(32.dp)).background(Color.Black).clickable { expanded = !expanded }.padding(14.dp), contentAlignment = Alignment.Center) {
                    if (expanded) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF28282C)), contentAlignment = Alignment.Center) { Text("♫", color = Mint, fontSize = 20.sp) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text("Island is running", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("Overlay preview · tap to collapse", color = Color.Gray, fontSize = 11.sp) }
                        Text("Ⅱ  ›", color = Color.White, fontSize = 15.sp)
                    } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(Mint))
                        Text("ISLAND", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                        Row(verticalAlignment = Alignment.Bottom) { listOf(7.dp,12.dp,5.dp).forEach { bar -> Box(Modifier.padding(horizontal=1.dp).width(3.dp).height(bar).clip(RoundedCornerShape(2.dp)).background(Mint)) } }
                    }
                }
            } else Spacer(Modifier.height(46.dp))
            Spacer(Modifier.height(38.dp))
            Text("ISLAND", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
            Spacer(Modifier.height(7.dp)); Text("A living interface for Android", color = Color(0xFF96969B), fontSize = 14.sp)
            Spacer(Modifier.height(30.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).padding(18.dp)) {
                Text("ISLAND CONTROLS", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(14.dp))
                SettingRow("Floating Island", "Show the capsule over other apps", enabled) { enabled=it; onEnabledChange(it) }
                SettingRow("Priority mode", "Prioritize the latest activity", priority) { priority=it; onPriorityChange(it) }
                SettingRow("Reduce motion", "Disable spring animation", reduceMotion) { reduceMotion=it; onReduceMotionChange(it) }
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).padding(18.dp)) {
                Text("OVERLAY ACCESS", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(8.dp))
                Text(if (overlayGranted) "Permission is granted" else "Allow Island to display over apps", color = Color.White, fontSize = 14.sp)
                if (!overlayGranted) { Spacer(Modifier.height(12.dp)); Text("OPEN SETTINGS  ›", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick=onRequestOverlay)) }
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).padding(18.dp)) {
                Text("ACTIVITY INTEGRATION", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(8.dp))
                Text(if (notificationAccessGranted) "Notification access is connected" else "Optional: show notification activities in Island", color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                Text(if (notificationAccessGranted) "MANAGE NOTIFICATION ACCESS  ›" else "GRANT NOTIFICATION ACCESS  ›", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onRequestNotificationAccess))
            }
            Spacer(Modifier.height(16.dp))
            Text("Quick Settings tiles are available in the Android tile editor.", color = Color(0xFF74747A), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical=7.dp), verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title,color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Medium); Text(subtitle,color=Color(0xFF929298),fontSize=11.sp) }
        Box(Modifier.width(48.dp).height(28.dp).clip(RoundedCornerShape(18.dp)).background(if(value) Mint else Color(0xFF343438)).clickable { onChange(!value) }.padding(3.dp), contentAlignment=if(value) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(Modifier.size(22.dp).clip(RoundedCornerShape(50)).background(if(value) Color(0xFF101012) else Color.White))
        }
    }
}
