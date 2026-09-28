package com.example.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationCenter(
  notifications: List<LauncherNotification>,
  onDismiss: () -> Unit,
  onClearAll: () -> Unit,
  onRemove: (String) -> Unit,
  onOpenSettings: () -> Unit,
  onOpenApp: (String) -> Unit = {}
) {
  Box(
    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))
      .clickable(onClick = onDismiss)
      .padding(top = 44.dp)
  ) {
    Column(
      Modifier.fillMaxSize().padding(horizontal = 14.dp)
        .clip(RoundedCornerShape(28.dp)).background(Color(0xFF171717))
        .clickable(enabled = false) {}
        .padding(16.dp)
    ) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Notification Center", color = Color.White, fontSize = 22.sp)
        TextButton(onClick = onClearAll) { Text("Clear All") }
      }

      if (notifications.isEmpty()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
          Text("No Notifications", color = Color.White.copy(alpha = 0.7f))
          TextButton(onClick = onOpenSettings) { Text("Enable Notification Access") }
        }
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(notifications, key = { it.id }) { item ->
            NotificationCard(item, onRemove = { onRemove(item.id) }, onOpenApp = onOpenApp)
          }
        }
      }
    }
  }
}

@Composable
private fun NotificationCard(item: LauncherNotification, onRemove: () -> Unit, onOpenApp: (String) -> Unit) {
  Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.08f))
      .clickable(enabled = item.packageName.isNotBlank()) { onOpenApp(item.packageName) }
      .padding(14.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
      Text(item.appName, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
      Text(item.title.ifBlank { "Notification" }, color = Color.White, fontSize = 15.sp)
      Text(item.message, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, maxLines = 4)
      Text(DateUtils.getRelativeTimeSpanString(item.timestamp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
    }
    TextButton(onClick = onRemove) { Text("×") }
  }
}
