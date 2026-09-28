package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun DynamicIsland(
  state: DynamicIslandState,
  onMusicPlayPause: () -> Unit = {},
  onMusicNext: () -> Unit = {},
  onMusicPrevious: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val pulse by rememberInfiniteTransition(label = "island_pulse").animateFloat(
    initialValue = 0.55f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
    label = "pulse"
  )
  val hasContent = state.hasMusic || state.hasTimer || state.hasCall ||
    state.isCharging || state.bluetoothConnected

  AnimatedContent(
    targetState = expanded && hasContent,
    transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
    label = "dynamic_island"
  ) { isExpanded ->
    if (isExpanded) {
      Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 52.dp)
          .clip(RoundedCornerShape(28.dp)).background(Color(0xFF151515))
          .clickable { expanded = false }.padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Dynamic Island", color = Color.White, fontWeight = FontWeight.SemiBold)
          Text("Tap to close", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
        }
        if (state.hasCall) IslandRow("☎", "Incoming call", state.callerName.ifBlank { "Unknown caller" }, "Answer / Decline")
        if (state.hasMusic) MusicIslandRow(state, onMusicPlayPause, onMusicPrevious, onMusicNext)
        if (state.hasTimer) IslandRow("◷", "Timer", formatTimer(state.timerSeconds), "Countdown")
        if (state.isCharging) IslandRow("⚡", "Charging", "PCT%".replace("PCT", state.batteryPercent.toString()) + "%", "Power connected")
        if (state.bluetoothConnected) IslandRow("ᛒ", "Bluetooth", "Connected", "Accessory active")
      }
    } else {
      Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = if (hasContent) 84.dp else 108.dp)
          .height(34.dp).clip(RoundedCornerShape(18.dp)).background(Color.Black)
          .clickable(enabled = hasContent) { expanded = true }.padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          if (state.hasCall) Text("☎", color = Color.White, fontSize = 13.sp)
          else if (state.hasMusic) Text("♫", color = Color.White, fontSize = 13.sp)
          else if (state.isCharging) Text("⚡", color = Color.White.copy(alpha = pulse), fontSize = 13.sp)
          if (state.hasTimer) Text(formatTimer(state.timerSeconds), color = Color.White, fontSize = 12.sp)
          if (state.bluetoothConnected) Text("ᛒ", color = Color.White, fontSize = 12.sp)
          if (!hasContent) Spacer(Modifier.size(1.dp))
        }
      }
    }
  }
}

@Composable
private fun MusicIslandRow(state: DynamicIslandState, onPlayPause: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit) {
  Column(Modifier.fillMaxWidth()) {
    IslandRow("♫", if (state.musicPlaying) "Now Playing" else "Paused", state.musicTitle ?: "Music", state.musicArtist ?: "")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
      Text("⏮", color = Color.White, fontSize = 20.sp, modifier = Modifier.clickable { onPrevious() }.padding(10.dp))
      Text(if (state.musicPlaying) "Ⅱ" else "▶", color = Color.White, fontSize = 20.sp, modifier = Modifier.clickable { onPlayPause() }.padding(10.dp))
      Text("⏭", color = Color.White, fontSize = 20.sp, modifier = Modifier.clickable { onNext() }.padding(10.dp))
    }
  }
}

@Composable
private fun IslandRow(icon: String, title: String, value: String, subtitle: String) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
      Text(icon, color = Color.White)
    }
    Column(Modifier.weight(1f)) {
      Text(title, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
      Text(value, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1)
      if (subtitle.isNotBlank()) Text(subtitle, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
    }
  }
}

private fun formatTimer(seconds: Long): String {
  val safe = seconds.coerceAtLeast(0L)
  return "%02d:%02d".format(safe / 60L, safe % 60L)
}
