package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.ui.theme.CupertinoBlue
import com.example.ui.theme.CupertinoIndigo
import com.example.ui.theme.CupertinoPurple
import com.example.ui.theme.GlassBorder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
  app: AppInfo,
  modifier: Modifier = Modifier,
  iconSize: Dp = 60.dp,
  showLabel: Boolean = true,
  animatePress: Boolean = true,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  folderPreviewApps: List<AppInfo> = emptyList()
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (animatePress && isPressed) 0.88f else 1.0f,
    animationSpec = tween(durationMillis = if (animatePress) 90 else 0),
    label = "iconScale"
  )

  Column(
    modifier = modifier
      .width(iconSize + 16.dp)
      .padding(vertical = 4.dp)
      .scale(scale)
      .testTag("app_icon_${app.packageName}")
      .combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
        onLongClick = onLongClick
      ),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Squircle Icon Box
    val cornerRadius = (iconSize.value * 0.23f).dp
    val squircleShape = RoundedCornerShape(cornerRadius)

    Box(
      modifier = Modifier
        .size(iconSize)
        .shadow(6.dp, squircleShape, spotColor = Color(0x35000000))
        .clip(squircleShape)
        .background(
          Brush.linearGradient(
            colors = listOf(
              CupertinoIndigo.copy(alpha = 0.85f),
              CupertinoBlue.copy(alpha = 0.85f)
            )
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      if (app.isFolder) {
        Column(
          modifier = Modifier.fillMaxSize().padding(6.dp),
          verticalArrangement = Arrangement.spacedBy(3.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          val previews = folderPreviewApps.take(4)
          Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            previews.take(2).forEach { preview ->
              preview.iconBitmap?.let { bitmap ->
                Image(bitmap = bitmap, contentDescription = preview.label, modifier = Modifier.size(iconSize * 0.39f).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
              } ?: Icon(Icons.Default.Apps, preview.label, tint = Color.White, modifier = Modifier.size(iconSize * 0.39f))
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            previews.drop(2).take(2).forEach { preview ->
              preview.iconBitmap?.let { bitmap ->
                Image(bitmap = bitmap, contentDescription = preview.label, modifier = Modifier.size(iconSize * 0.39f).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
              } ?: Icon(Icons.Default.Apps, preview.label, tint = Color.White, modifier = Modifier.size(iconSize * 0.39f))
            }
          }
        }
      } else {
        val bitmap = app.iconBitmap
        if (bitmap != null) {
          Image(
            bitmap = bitmap,
            contentDescription = app.label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
          Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = app.label,
            tint = Color.White,
            modifier = Modifier.size(iconSize * 0.6f)
          )
        }
      }
    }

    if (showLabel) {
      Text(
        text = app.label,
        modifier = Modifier
          .padding(top = 5.dp)
          .width(iconSize + 14.dp),
        color = Color.White,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        style = TextStyle(
          shadow = Shadow(
            color = Color(0x99000000),
            offset = Offset(0f, 1.5f),
            blurRadius = 3f
          )
        )
      )
    }
  }
}
