package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryState
import com.example.model.QuickToggleState
import com.example.model.WeatherState
import com.example.viewmodel.CalendarEvent
import com.example.ui.theme.CupertinoBlue
import com.example.ui.theme.CupertinoGreen
import com.example.ui.theme.CupertinoIndigo
import com.example.ui.theme.CupertinoOrange
import com.example.ui.theme.CupertinoTeal
import com.example.ui.theme.CupertinoYellow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteLow
import com.example.ui.theme.GlassWhiteMedium

/**
 * iOS 27 Glass Clock and Date Widget.
 */
@Composable
fun ClockWidget(
  timeString: String,
  dateString: String,
  dayString: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  GlassCard(
    modifier = modifier
      .height(160.dp)
      .testTag("clock_widget"),
    backgroundColor = GlassWhiteMedium,
    onClick = onClick
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
            text = dayString.uppercase(),
            color = CupertinoOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = dateString,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NightsStay,
            contentDescription = "Clock Mode",
            tint = CupertinoYellow,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Text(
        text = timeString,
        color = Color.White,
        fontSize = 42.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1).sp
      )

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(CupertinoGreen)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Launcher OS 27 Live",
          color = Color.White.copy(alpha = 0.75f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

/**
 * iOS 27 Weather Glass Widget.
 */
@Composable
fun WeatherWidget(
  weather: WeatherState,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  GlassCard(
    modifier = modifier
      .height(160.dp)
      .testTag("weather_widget"),
    backgroundColor = Color(0x301B3A5A),
    onClick = onClick
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
        verticalAlignment = Alignment.Top
      ) {
        Column {
          Text(
            text = weather.location,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = weather.condition,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
          )
        }
        Icon(
          imageVector = Icons.Default.WbSunny,
          contentDescription = "Weather condition",
          tint = CupertinoYellow,
          modifier = Modifier.size(32.dp)
        )
      }

      Text(
        text = "${weather.temperatureCelsius}°",
        color = Color.White,
        fontSize = 44.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-1).sp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "H:${weather.highTemp}°  L:${weather.lowTemp}°",
          color = Color.White.copy(alpha = 0.8f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "UV ${weather.uvIndex}",
          color = CupertinoTeal,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

/**
 * iOS 27 Battery & Power Glass Widget.
 */
@Composable
fun BatteryWidget(
  battery: BatteryState,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  GlassCard(
    modifier = modifier
      .height(160.dp)
      .testTag("battery_widget"),
    backgroundColor = GlassWhiteMedium,
    onClick = onClick
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
        Text(
          text = "BATTERY",
          color = CupertinoGreen,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Icon(
          imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
          contentDescription = "Battery Status",
          tint = if (battery.isCharging) CupertinoGreen else Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "${battery.percentage}%",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (battery.isCharging) "Charging" else "On Battery",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(54.dp)
        ) {
          CircularProgressIndicator(
            progress = { battery.percentage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = if (battery.percentage < 20) Color(0xFFFF3B30) else CupertinoGreen,
            trackColor = Color.White.copy(alpha = 0.15f),
            strokeWidth = 6.dp
          )
          if (battery.isCharging) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "Fast Charging",
              tint = CupertinoYellow,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }

      Text(
        text = "SYSTEM STATUS",
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 11.sp
      )
    }
  }
}



@Composable
fun CalendarWidget(
  dateString: String,
  dayString: String,
  nextEvent: CalendarEvent? = null,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  GlassCard(
    modifier = modifier.height(160.dp).testTag("calendar_widget"),
    backgroundColor = Color(0x303C5A88),
    onClick = onClick
  ) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("CALENDAR", color = CupertinoOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("TODAY", color = Color.White.copy(alpha = .65f), fontSize = 11.sp)
      }
      Column {
        Text(dayString, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(dateString, color = Color.White.copy(alpha = .78f), fontSize = 13.sp)
      }
      if (nextEvent == null) {
        Text("No upcoming events", color = Color.White.copy(alpha = .62f), fontSize = 12.sp)
      } else {
        val eventTime = if (nextEvent.allDay) "All day" else java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(nextEvent.startMillis))
        Column {
          Text(nextEvent.title, color = Color.White, fontSize = 12.sp, maxLines = 1)
          Text(eventTime, color = Color.White.copy(alpha = .62f), fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
fun RecentAppsWidget(
  apps: List<com.example.model.AppModel>,
  modifier: Modifier = Modifier,
  onAppClick: (com.example.model.AppModel) -> Unit
) {
  GlassCard(
    modifier = modifier.height(160.dp).testTag("recent_apps_widget"),
    backgroundColor = GlassWhiteMedium
  ) {
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("RECENT APPS", color = CupertinoTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
      if (apps.isEmpty()) {
        Text("Open an app to see it here", color = Color.White.copy(alpha = .65f), fontSize = 12.sp)
      } else {
        apps.take(4).forEach { app ->
          Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onAppClick(app) }.padding(7.dp), verticalAlignment = Alignment.CenterVertically) {
            app.icon?.let { androidx.compose.foundation.Image(it, contentDescription = app.label, modifier = Modifier.size(28.dp)) }
            Spacer(Modifier.width(9.dp))
            Text(app.label, color = Color.White, fontSize = 12.sp, maxLines = 1)
          }
        }
      }
    }
  }
}

/**
 * iOS 27 Control Center Quick Toggles Glass Widget.
 */
@Composable
fun ControlCenterWidget(
  toggleState: QuickToggleState,
  modifier: Modifier = Modifier,
  onToggleWifi: () -> Unit,
  onToggleBluetooth: () -> Unit,
  onToggleFlashlight: () -> Unit,
  onToggleAirplane: () -> Unit,
  onOpenSettings: () -> Unit
) {
  GlassCard(
    modifier = modifier
      .height(160.dp)
      .testTag("control_center_widget"),
    backgroundColor = GlassWhiteMedium
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "CONTROL CENTER",
          color = CupertinoBlue,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onOpenSettings),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        TogglePillButton(
          icon = Icons.Default.Wifi,
          label = "Wi-Fi",
          isActive = toggleState.isWifiEnabled,
          activeColor = CupertinoBlue,
          onClick = onToggleWifi
        )
        TogglePillButton(
          icon = Icons.Default.Bluetooth,
          label = "Bluetooth",
          isActive = toggleState.isBluetoothEnabled,
          activeColor = CupertinoBlue,
          onClick = onToggleBluetooth
        )
        TogglePillButton(
          icon = Icons.Default.FlashlightOn,
          label = "Torch",
          isActive = toggleState.isFlashlightOn,
          activeColor = CupertinoYellow,
          onClick = onToggleFlashlight
        )
        TogglePillButton(
          icon = Icons.Default.AirplanemodeActive,
          label = "Airplane",
          isActive = toggleState.isAirplaneMode,
          activeColor = CupertinoOrange,
          onClick = onToggleAirplane
        )
      }
    }
  }
}

@Composable
private fun TogglePillButton(
  icon: ImageVector,
  label: String,
  isActive: Boolean,
  activeColor: Color,
  onClick: () -> Unit
) {
  val bgCol by animateColorAsState(
    targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.15f),
    label = "toggleBg"
  )
  val iconCol by animateColorAsState(
    targetValue = if (isActive) Color.White else Color.White.copy(alpha = 0.7f),
    label = "toggleIcon"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(46.dp)
        .clip(CircleShape)
        .background(bgCol),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = iconCol,
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      color = Color.White.copy(alpha = 0.85f),
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
