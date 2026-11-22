package com.mihir.island

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Color(0xFF050506),
                surface = Color(0xFF111113),
                primary = Color(0xFFB9F5D0)
            )) {
                IslandHome()
            }
        }
    }
}


@Composable
private fun IslandHome() {
    var expanded by remember { mutableStateOf(false) }
    val pillWidth by animateDpAsState(
        targetValue = if (expanded) 340.dp else 184.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "island-width"
    )
    val pillHeight by animateDpAsState(
        targetValue = if (expanded) 112.dp else 48.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "island-height"
    )

    Box(Modifier.fillMaxSize().background(Color(0xFF050506))) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier.width(pillWidth).height(pillHeight)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.Black)
                        .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (expanded) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.width(38.dp).height(38.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF27272B)), contentAlignment = Alignment.Center) {
                            Text("♫", color = Color(0xFFB9F5D0), fontSize = 19.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Island Preview", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Tap to collapse", color = Color(0xFF98989D), fontSize = 11.sp)
                        }
                        Text("Ⅱ   ›", color = Color.White, fontSize = 15.sp)
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Box(Modifier.width(8.dp).height(8.dp).clip(RoundedCornerShape(50)).background(Color(0xFFB9F5D0)))
                        Text("ISLAND", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.2.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(3.dp).height(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFB9F5D0)))
                            Spacer(Modifier.width(3.dp))
                            Box(Modifier.width(3.dp).height(12.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFB9F5D0)))
                            Spacer(Modifier.width(3.dp))
                            Box(Modifier.width(3.dp).height(6.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFB9F5D0)))
                        }
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
            Text("ISLAND", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Spacer(Modifier.height(8.dp))
            Text("A living interface for Android", color = Color(0xFF929298), fontSize = 14.sp)
            Spacer(Modifier.height(36.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF121214)).padding(20.dp)) {
                Text("FIRST BUILD", color = Color(0xFFB9F5D0), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(10.dp))
                Text("Animation foundation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Tap the island above to preview its compact-to-expanded morph. This is our initial scaffold; notch-aware positioning and the activity system come next.", color = Color(0xFFACACB2), fontSize = 13.sp, lineHeight = 19.sp)
            }
        }
    }
}
