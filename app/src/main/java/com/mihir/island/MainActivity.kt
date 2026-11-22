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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Card, primary = Mint)) {
                IslandScreen(Settings.canDrawOverlays(this)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            }
        }
    }
}

@Composable
private fun IslandScreen(overlayGranted: Boolean, onRequestOverlay: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(true) }
    var reduceMotion by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(false) }
    val width by animateDpAsState(if (expanded) 328.dp else 180.dp, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy), label = "width")
    val height by animateDpAsState(if (expanded) 116.dp else 46.dp, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy), label = "height")
    Box(Modifier.fillMaxSize().background(Ink)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(14.dp))
            if (enabled) {
                Box(Modifier.width(width).height(height).clip(RoundedCornerShape(32.dp)).background(Color.Black)
                    .clickable { expanded = !expanded }.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    if (expanded) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF28282C)), contentAlignment = Alignment.Center) { Text("♫", color = Mint, fontSize = 20.sp) }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Island Activity", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Tap to collapse", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text("Ⅱ  ›", color = Color.White, fontSize = 15.sp)
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(Mint))
                            Text("ISLAND", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                            Row(verticalAlignment = Alignment.Bottom) {
                                listOf(7.dp, 12.dp, 5.dp).forEach { bar -> Box(Modifier.padding(horizontal = 1.dp).width(3.dp).height(bar).clip(RoundedCornerShape(2.dp)).background(Mint)) }
                            }
                        }
                    }
                }
            } else {
                Spacer(Modifier.height(46.dp)); Text("Island is hidden", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(38.dp))
            Text("ISLAND", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
            Spacer(Modifier.height(7.dp)); Text("A living interface for Android", color = Color(0xFF96969B), fontSize = 14.sp)
            Spacer(Modifier.height(30.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).padding(18.dp)) {
                Text("QUICK CONTROLS", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(14.dp))
                SettingRow("Island enabled", "Show or hide the demo pill", enabled) { enabled = it }
                SettingRow("Priority mode", "Prioritize the active activity", priority) { priority = it }
                SettingRow("Reduce motion", "Use less animated transitions", reduceMotion) { reduceMotion = it }
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).padding(18.dp)) {
                Text("OVERLAY PERMISSION", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(8.dp))
                Text(if (overlayGranted) "Permission granted" else "Needed for floating Island", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Text("Overlay service is a follow-up implementation step; this permission screen is ready.", color = Color(0xFF9D9DA3), fontSize = 12.sp, lineHeight = 17.sp)
                if (!overlayGranted) {
                    Spacer(Modifier.height(12.dp))
                    Text("OPEN PERMISSION SETTINGS  ›", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onRequestOverlay))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Tap the island to expand or collapse.", color = Color(0xFF74747A), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color(0xFF929298), fontSize = 11.sp)
        }
        Box(Modifier.width(48.dp).height(28.dp).clip(RoundedCornerShape(18.dp)).background(if (value) Mint else Color(0xFF343438)).clickable { onChange(!value) }.padding(3.dp), contentAlignment = if (value) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(Modifier.size(22.dp).clip(RoundedCornerShape(50)).background(if (value) Color(0xFF101012) else Color.White))
        }
    }
}
