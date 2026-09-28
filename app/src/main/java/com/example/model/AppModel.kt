package com.example.model

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Categorization for iOS 27 style App Library clusters.
 */
enum class AppCategory(val title: String) {
  SUGGESTIONS("Suggestions"),
  SOCIAL("Social & Communication"),
  UTILITIES("Utilities"),
  PRODUCTIVITY("Productivity & Finance"),
  ENTERTAINMENT("Entertainment"),
  CREATIVE("Creativity & Media"),
  SYSTEM("System & Tools")
}

/**
 * Data class representing an application model with appName, packageName, and icon.
 */
data class AppModel(
  val appName: String,
  val packageName: String,
  val icon: ImageBitmap? = null,
  val activityName: String = "",
  val category: AppCategory = AppCategory.UTILITIES,
  val isSystemApp: Boolean = false,
  val installTime: Long = 0L,
  val isFolder: Boolean = false,
  val folderId: String = "",
  val folderName: String = "",
  val folderAppKeys: List<String> = emptyList()
) {
  val label: String get() = appName
  val iconBitmap: ImageBitmap? get() = icon
}

typealias AppInfo = AppModel

/**
 * Live weather state for iOS 27 weather widget.
 */
data class WeatherState(
  val location: String = "Current Location",
  val temperatureCelsius: Int = 0,
  val condition: String = "Tap to refresh",
  val highTemp: Int = 0,
  val lowTemp: Int = 0,
  val uvIndex: Int = 0,
  val windSpeedKmh: Int = 0
)

/**
 * Battery and system state for iOS 27 power widget.
 */
data class BatteryState(
  val percentage: Int = 94,
  val isCharging: Boolean = false,
  val powerSaveMode: Boolean = false
)

/**
 * Quick toggle state for Control Center widget.
 */
data class QuickToggleState(
  val isWifiEnabled: Boolean = true,
  val isBluetoothEnabled: Boolean = true,
  val isFlashlightOn: Boolean = false,
  val isAirplaneMode: Boolean = false,
  val isDoNotDisturb: Boolean = false,
  val isHotspotOn: Boolean = false
)
