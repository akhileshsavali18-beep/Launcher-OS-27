package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteLow
import com.example.ui.theme.GlassWhiteMedium

/**
 * Reusable Glassmorphism Card supporting iOS 27 specular highlight and frosted appearance.
 */
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(26.dp),
  backgroundColor: Color = GlassWhiteMedium,
  borderColor: Color = GlassBorder,
  elevation: Dp = 8.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val borderBrush = Brush.linearGradient(
    colors = listOf(
      GlassBorderLight,
      borderColor.copy(alpha = 0.25f),
      Color.Transparent
    )
  )

  val cardModifier = modifier
    .shadow(elevation, shape = shape, spotColor = Color(0x40000000), ambientColor = Color(0x20000000))
    .clip(shape)
    .background(
      Brush.verticalGradient(
        colors = listOf(
          backgroundColor,
          backgroundColor.copy(alpha = backgroundColor.alpha * 0.7f)
        )
      )
    )
    .border(width = 1.dp, brush = borderBrush, shape = shape)
    .then(
      if (onClick != null) {
        Modifier.clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(bounded = true, color = Color.White),
          onClick = onClick
        )
      } else Modifier
    )

  Box(
    modifier = cardModifier,
    content = content
  )
}

/**
 * Pill-shaped glass container used for search bars, widgets, and dynamic island.
 */
@Composable
fun GlassPill(
  modifier: Modifier = Modifier,
  backgroundColor: Color = GlassWhiteLow,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val pillShape = RoundedCornerShape(percent = 50)
  val borderBrush = Brush.horizontalGradient(
    listOf(
      GlassBorderLight,
      GlassBorder.copy(alpha = 0.2f),
      GlassBorderLight.copy(alpha = 0.5f)
    )
  )

  Box(
    modifier = modifier
      .clip(pillShape)
      .background(backgroundColor)
      .border(1.dp, borderBrush, pillShape)
      .then(
        if (onClick != null) {
          Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true, color = Color.White),
            onClick = onClick
          )
        } else Modifier
      ),
    content = content
  )
}
