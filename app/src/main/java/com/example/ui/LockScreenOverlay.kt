package com.example.ui

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
import androidx.compose.foundation.layout.size
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

/**
 * iOS 27-style in-app lock-screen experience.
 *
 * This is intentionally an in-app overlay rather than replacing Android's
 * system lock screen.
 */
@Composable
fun LockScreenOverlay(
  timeString: String,
  dateString: String,
  weather: WeatherState,
  battery: BatteryState,
  onDismiss: () -> Unit,
  onFlashlight: () -> Unit,
  onCamera: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            Color(0xFF081226).copy(alpha = 0.98f),
            Color(0xFF17134A).copy(alpha = 0.98f),
            Color(0xFF071B2A).copy(alpha = 0.99f)
          )
        )
      )
      .clickable(onClick = onDismiss)
      .testTag("lock_screen_overlay")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 42.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "LOCK SCREEN",
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
      )

      Spacer(Modifier.height(20.dp))

      Text(
        text = timeString,
        color = Color.White,
        fontSize = 78.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-3).sp
      )

      Text(
        text = dateString,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(Modifier.height(28.dp))

      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp),
        backgroundColor = GlassWhiteMedium
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                weather.location,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp
              )
              Text(
                weather.condition,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            Text(
              text = "${weather.temperatureCelsius}°",
              color = Color.White,
              fontSize = 34.sp,
              fontWeight = FontWeight.Light
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "Battery ${battery.percentage}%",
              color = Color.White.copy(alpha = 0.85f),
              fontSize = 13.sp
            )
            Text(
              if (battery.isCharging) "Charging" else "Ready",
              color = CupertinoGreen,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(Modifier.weight(1f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        GlassCard(
          modifier = Modifier.size(62.dp),
          backgroundColor = GlassWhiteMedium,
          onClick = onFlashlight
        ) {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("🔦", fontSize = 24.sp)
          }
        }

        GlassCard(
          modifier = Modifier.size(62.dp),
          backgroundColor = GlassWhiteMedium,
          onClick = onCamera
        ) {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("📷", fontSize = 24.sp)
          }
        }
      }

      Spacer(Modifier.height(18.dp))

      Text(
        "Tap anywhere to close",
        color = Color.White.copy(alpha = 0.5f),
        fontSize = 11.sp
      )
    }
  }
}
