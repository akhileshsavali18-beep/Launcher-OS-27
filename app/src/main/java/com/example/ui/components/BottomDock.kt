package com.example.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AppInfo
import com.example.ui.theme.GlassWhiteHigh

/**
 * iOS 27 Frosted Glass Bottom Dock.
 * In edit mode, long-press + drag reorders icons; tap removes the icon from the dock.
 */
@Composable
fun BottomDock(
  dockApps: List<AppInfo>,
  editMode: Boolean = false,
  dockOpacity: Float = 0.55f,
  iconSizeDp: Int = 56,
  modifier: Modifier = Modifier,
  onAppClick: (AppInfo) -> Unit,
  onReorder: (Int, Int) -> Unit = { _, _ -> },
  onRemoveFromDock: (AppInfo) -> Unit = {},
  onLongPress: (AppInfo) -> Unit = {}
) {
  GlassCard(
    modifier = modifier
      .fillMaxWidth()
      .height(92.dp)
      .padding(horizontal = 16.dp)
      .testTag("bottom_dock"),
    shape = RoundedCornerShape(34.dp),
    backgroundColor = GlassWhiteHigh.copy(alpha = dockOpacity),
    elevation = 14.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      dockApps.take(4).forEachIndexed { index, app ->
        val dragOffset = remember(app.packageName) { mutableFloatStateOf(0f) }
        AppIconItem(
          app = app,
          iconSize = iconSizeDp.dp,
          showLabel = false,
          modifier = if (editMode) Modifier.pointerInput(app.packageName, dockApps) {
            detectDragGesturesAfterLongPress(
              onDragStart = { dragOffset.floatValue = 0f },
              onDragCancel = { dragOffset.floatValue = 0f },
              onDragEnd = { dragOffset.floatValue = 0f },
              onDrag = { change, amount ->
                change.consume()
                dragOffset.floatValue += amount.x
                if (dragOffset.floatValue > 70f && index < dockApps.lastIndex) {
                  onReorder(index, index + 1)
                  dragOffset.floatValue = 0f
                } else if (dragOffset.floatValue < -70f && index > 0) {
                  onReorder(index, index - 1)
                  dragOffset.floatValue = 0f
                }
              }
            )
          } else Modifier,
          onClick = {
            if (editMode) onRemoveFromDock(app) else onAppClick(app)
          },
          onLongClick = { onLongPress(app) }
        )
      }
    }
  }
}
