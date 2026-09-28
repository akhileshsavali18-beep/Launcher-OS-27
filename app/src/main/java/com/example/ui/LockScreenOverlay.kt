package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryState
import com.example.model.WeatherState
import com.example.ui.components.GlassCard
import com.example.ui.theme.CupertinoGreen
import com.example.ui.theme.GlassWhiteMedium

@Composable
fun LockScreenOverlay(
  timeString: String,
  dateString: String,
  weather: WeatherState,
  battery: BatteryState,
  onDismiss: () -> Unit,
  onFlashlight: () -> Unit,
  onCamera: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("lock_screen_overlay")
      .clickable(onClick = onDismiss)
      .background(
        Brush.verticalGradient(
          listOf(Color(0xDD06111F), Color(0xCC111B35), Color(0xDD050A14))
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    GlassCard(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
      shape = RoundedCornerShape(36.dp),
      backgroundColor = GlassWhiteMedium.copy(alpha = 0.16f),
      borderColor = Color.White.copy(alpha = 0.30f),
      elevation = 18.dp,
      onClick = onDismiss
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(timeString, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Light)
        Text(dateString, color = Color.White.copy(alpha = 0.82f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.size(24.dp))

        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(24.dp),
          backgroundColor = GlassWhiteMedium.copy(alpha = 0.16f),
          borderColor = Color.White.copy(alpha = 0.20f),
          elevation = 4.dp
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(weather.location, color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
            Text("${weather.temperatureCelsius}°  •  ${weather.condition}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("H ${weather.highTemp}°   L ${weather.lowTemp}°   •   Battery ${battery.percentage}%", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
          }
        }

        Spacer(Modifier.size(28.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          LockShortcut("Flashlight", "✦", onFlashlight)
          Text("Tap anywhere to unlock", color = Color.White.copy(alpha = 0.62f), fontSize = 11.sp)
          LockShortcut("Camera", "◉", onCamera)
        }

        Spacer(Modifier.size(8.dp))
        Text("Launcher OS 27", color = CupertinoGreen.copy(alpha = 0.82f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun LockShortcut(label: String, symbol: String, onClick: () -> Unit) {
  GlassCard(
    modifier = Modifier.size(width = 78.dp, height = 68.dp),
    shape = CircleShape,
    backgroundColor = Color.White.copy(alpha = 0.12f),
    borderColor = Color.White.copy(alpha = 0.24f),
    elevation = 4.dp,
    onClick = onClick
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(symbol, color = Color.White, fontSize = 21.sp)
      Text(label, color = Color.White.copy(alpha = 0.76f), fontSize = 8.sp)
    }
  }
}
