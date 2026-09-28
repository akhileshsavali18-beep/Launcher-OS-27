package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryState
import com.example.ui.theme.CupertinoGreen
import com.example.ui.theme.CupertinoPink
import com.example.ui.theme.CupertinoYellow
import com.example.ui.theme.GlassBorder

/**
 * iOS 27 Dynamic Island and Status Bar Header.
 */
@Composable
fun TopStatusBar(
  timeString: String,
  battery: BatteryState,
  appCount: Int,
  dateString: String = "",
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 20.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Row: Time, Dynamic Island, Status Icons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Clock Time
      Text(
        text = timeString,
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.width(68.dp)
      )

      // Center: Dynamic Island Pill
      DynamicIsland(
        isExpanded = isExpanded,
        battery = battery,
        appCount = appCount,
        dateString = dateString,
        onToggle = { isExpanded = !isExpanded }
      )

      // Right: Cellular, Wi-Fi, Battery
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.width(68.dp)
      ) {
        Icon(
          imageVector = Icons.Default.SignalCellular4Bar,
          contentDescription = "Cellular",
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.Default.Wifi,
          contentDescription = "Wi-Fi",
          tint = Color.White,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
          contentDescription = "Battery",
          tint = if (battery.isCharging) CupertinoGreen else Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun DynamicIsland(
  isExpanded: Boolean,
  battery: BatteryState,
  appCount: Int,
  dateString: String = "",
  onToggle: () -> Unit
) {
  val shape = RoundedCornerShape(32.dp)
  val borderBrush = Brush.linearGradient(
    listOf(
      Color(0x55FFFFFF),
      Color(0x15FFFFFF)
    )
  )

  Box(
    modifier = Modifier
      .animateContentSize(animationSpec = spring())
      .clip(shape)
      .background(Color(0xFF000000))
      .border(1.dp, borderBrush, shape)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onToggle
      )
      .padding(
        horizontal = if (isExpanded) 18.dp else 12.dp,
        vertical = if (isExpanded) 12.dp else 6.dp
      )
      .testTag("dynamic_island"),
    contentAlignment = Alignment.Center
  ) {
    if (!isExpanded) {
      // Compact Pill Mode
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.MusicNote,
          contentDescription = "Now Playing indicator",
          tint = CupertinoPink,
          modifier = Modifier.size(12.dp)
        )
        Text(
          text = "Launcher OS 27",
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(if (battery.isCharging) CupertinoGreen else CupertinoYellow)
        )
      }
    } else {
      // Expanded Island View
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(220.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = null,
              tint = CupertinoPink,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "iOS 27 Theme Active",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (dateString.isBlank()) "$appCount Apps Ready" else "$dateString • $appCount Apps Ready",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
              )
            }
          }

          if (battery.isCharging) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = CupertinoGreen,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "${battery.percentage}%",
                color = CupertinoGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
