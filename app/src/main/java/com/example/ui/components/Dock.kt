package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppInfo
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteMedium

/**
 * iOS 27 Floating Frosted Glass Bottom Dock.
 */
@Composable
fun FloatingDock(
  dockApps: List<AppInfo>,
  modifier: Modifier = Modifier,
  onAppClick: (AppInfo) -> Unit
) {
  GlassCard(
    modifier = modifier
      .fillMaxWidth()
      .height(92.dp)
      .padding(horizontal = 16.dp)
      .testTag("floating_dock"),
    shape = RoundedCornerShape(34.dp),
    backgroundColor = GlassWhiteHigh,
    elevation = 14.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      dockApps.take(4).forEach { app ->
        AppIconItem(
          app = app,
          iconSize = 56.dp,
          showLabel = false,
          onClick = { onAppClick(app) }
        )
      }
    }
  }
}
