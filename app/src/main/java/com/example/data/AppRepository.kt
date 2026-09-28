package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.BatteryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

  @Volatile private var isFlashlightOn: Boolean = false

  private val packageManager: PackageManager = context.packageManager

  suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
    val appList = mutableListOf<AppInfo>()

    for (resolveInfo in resolveInfos) {
      val packageName = resolveInfo.activityInfo.packageName
      // Skip the launcher itself from the app grid
      if (packageName == context.packageName) continue

      val activityName = resolveInfo.activityInfo.name
      val label = resolveInfo.loadLabel(packageManager).toString()
      val drawable = try {
        resolveInfo.loadIcon(packageManager)
      } catch (e: Exception) {
        null
      }
      val iconBitmap = drawable?.toImageBitmapSafe()
      val category = categorizeApp(packageName, label)

      appList.add(
        AppInfo(
          appName = label,
          packageName = packageName,
          activityName = activityName,
          icon = iconBitmap,
          category = category,
          isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        )
      )
    }

    // Sort alphabetically
    appList.sortedBy { it.label.lowercase() }
  }

  fun launchApp(app: AppInfo): Boolean {
    return try {
      val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        ?: Intent().apply {
          component = ComponentName(app.packageName, app.activityName)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(launchIntent)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }


  fun uninstallApp(app: AppInfo): Boolean {
    return try {
      val intent = Intent(Intent.ACTION_DELETE).apply {
        data = android.net.Uri.parse("package:${app.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) { false }
  }

  fun openAppInfo(app: AppInfo): Boolean {
    return try {
      val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = android.net.Uri.parse("package:${app.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) { false }
  }

  fun openDefaultLauncherSettings(): Boolean {
    return try {
      val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) { false }
  }

  fun openSystemSettings(): Boolean {
    return try {
      val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun openWifiSettings(): Boolean {
    return try {
      val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun toggleFlashlight(): Boolean {
    return try {
      val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return false
      val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
          cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
      } ?: cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
      } ?: return false
      val nextState = !isFlashlightOn
      cameraManager.setTorchMode(cameraId, nextState)
      isFlashlightOn = nextState
      true
    } catch (e: Exception) {
      false
    }
  }

  fun isFlashlightOn(): Boolean = isFlashlightOn

  fun getWifiEnabled(): Boolean = try {
    val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
    wifi?.isWifiEnabled ?: false
  } catch (_: Exception) { false }

  fun getBluetoothEnabled(): Boolean = try {
    if (android.os.Build.VERSION.SDK_INT >= 31 &&
      androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return false
    android.bluetooth.BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false
  } catch (_: Exception) { false }

  fun getAirplaneModeEnabled(): Boolean = try {
    android.provider.Settings.Global.getInt(context.contentResolver, android.provider.Settings.Global.AIRPLANE_MODE_ON, 0) != 0
  } catch (_: Exception) { false }

  fun openBluetoothSettings(): Boolean {
    return try {
      val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun getBatteryStatus(): BatteryState {
    return try {
      val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
      val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 95
      val isCharging = bm?.isCharging ?: false
      BatteryState(
        percentage = level.coerceIn(0, 100),
        isCharging = isCharging
      )
    } catch (e: Exception) {
      BatteryState(percentage = 92, isCharging = false)
    }
  }

  private fun categorizeApp(packageName: String, label: String): AppCategory {
    val lowerPkg = packageName.lowercase()
    val lowerLabel = label.lowercase()

    return when {
      lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("messenger") ||
        lowerPkg.contains("twitter") || lowerPkg.contains("instagram") || lowerPkg.contains("facebook") ||
        lowerPkg.contains("contacts") || lowerPkg.contains("dialer") || lowerPkg.contains("phone") ||
        lowerPkg.contains("messaging") || lowerPkg.contains("sms") || lowerLabel.contains("phone") ||
        lowerLabel.contains("messages") || lowerLabel.contains("chat") -> AppCategory.SOCIAL

      lowerPkg.contains("youtube") || lowerPkg.contains("spotify") || lowerPkg.contains("netflix") ||
        lowerPkg.contains("music") || lowerPkg.contains("video") || lowerPkg.contains("media") ||
        lowerPkg.contains("game") || lowerLabel.contains("music") || lowerLabel.contains("tv") -> AppCategory.ENTERTAINMENT

      lowerPkg.contains("camera") || lowerPkg.contains("photos") || lowerPkg.contains("gallery") ||
        lowerPkg.contains("editor") || lowerPkg.contains("draw") || lowerLabel.contains("camera") ||
        lowerLabel.contains("photos") -> AppCategory.CREATIVE

      lowerPkg.contains("docs") || lowerPkg.contains("sheets") || lowerPkg.contains("slides") ||
        lowerPkg.contains("notes") || lowerPkg.contains("calendar") || lowerPkg.contains("mail") ||
        lowerPkg.contains("gmail") || lowerPkg.contains("office") || lowerLabel.contains("notes") ||
        lowerLabel.contains("calendar") -> AppCategory.PRODUCTIVITY

      lowerPkg.contains("settings") || lowerPkg.contains("system") || lowerPkg.contains("launcher") ||
        lowerPkg.contains("android") -> AppCategory.SYSTEM

      else -> AppCategory.UTILITIES
    }
  }

  private fun Drawable.toImageBitmapSafe(): ImageBitmap? {
    return try {
      if (this is BitmapDrawable && this.bitmap != null) {
        return this.bitmap.asImageBitmap()
      }
      val width = if (intrinsicWidth > 0) intrinsicWidth else 144
      val height = if (intrinsicHeight > 0) intrinsicHeight else 144
      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      setBounds(0, 0, canvas.width, canvas.height)
      draw(canvas)
      bitmap.asImageBitmap()
    } catch (e: Exception) {
      null
    }
  }
}
