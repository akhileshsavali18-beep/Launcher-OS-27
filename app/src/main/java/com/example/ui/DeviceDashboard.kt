package com.example.ui

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DeviceDashboardInfo(
  val batteryPercent: Int,
  val charging: Boolean,
  val health: String,
  val temperatureC: Float,
  val model: String,
  val androidVersion: String,
  val storageUsedPercent: Int
)

fun readDeviceDashboardInfo(context: Context): DeviceDashboardInfo {
  val battery = context.getSystemService(BatteryManager::class.java)
  val percent = runCatching { battery?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0 }.getOrDefault(0)
  val charging = runCatching { battery?.isCharging ?: false }.getOrDefault(false)
  val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
  val healthCode = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
    ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
  val health = when (healthCode) {
    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
    else -> "Unknown"
  }
  val temperature = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
  val stat = StatFs(Environment.getDataDirectory().path)
  val total = stat.totalBytes.coerceAtLeast(1L)
  val available = stat.availableBytes.coerceAtLeast(0L)
  val usedPercent = (((total - available).toDouble() / total.toDouble()) * 100.0).toInt().coerceIn(0, 100)
  return DeviceDashboardInfo(
    percent.coerceIn(0, 100), charging, health, temperature,
    "${Build.MANUFACTURER} ${Build.MODEL}".trim(), "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", usedPercent
  )
}

@Composable
fun DeviceDashboard(onDismiss: () -> Unit) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val info = remember { readDeviceDashboardInfo(context) }
  Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF171717)).padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Column {
        Text("Battery & Device", color = Color.White, fontSize = 20.sp)
        Text("Live device information", color = Color.White.copy(alpha = .55f), fontSize = 11.sp)
      }
      TextButton(onClick = onDismiss) { Text("Close") }
    }
    DashboardRow("Battery", info.batteryPercent.toString() + "%" + if (info.charging) " • Charging" else "")
    DashboardRow("Battery health", info.health)
    DashboardRow("Temperature", "%.1f°C".format(info.temperatureC))
    DashboardRow("Storage used", info.storageUsedPercent.toString() + "%")
    DashboardRow("Device", info.model)
    DashboardRow("Android", info.androidVersion)
  }
}

@Composable
private fun DashboardRow(label: String, value: String) {
  Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .07f)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, color = Color.White.copy(alpha = .65f), fontSize = 12.sp)
    Text(value, color = Color.White, fontSize = 12.sp)
  }
}
