package com.example

import android.os.Bundle
import android.content.Intent
import android.provider.MediaStore
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HomeScreen
import com.example.ui.LockScreenOverlay
import com.example.ui.LockScreenController
import com.example.ui.DynamicIsland
import com.example.ui.DynamicIslandController
import com.example.ui.NotificationCenter
import com.example.ui.NotificationCenterController
import com.example.ui.MediaSessionBridge
import com.example.ads.AdsManager
import com.example.billing.SubscriptionManager
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
  private var billingManager: SubscriptionManager? = null
  override fun onResume() {
    super.onResume()
    // Refresh the launcher whenever it returns to the foreground so newly
    // installed/uninstalled apps are reflected immediately.
    val currentViewModel = androidx.lifecycle.ViewModelProvider(this)[LauncherViewModel::class.java]
    currentViewModel.refreshApps()
    currentViewModel.updateBatteryStatus()
    // Re-check Play entitlement when returning from Play Store/billing UI.
    billingManager?.restorePurchases()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    AdsManager.initialize(this)
    val subscriptionManager = SubscriptionManager(this)
    billingManager = subscriptionManager
    val lockScreenController = LockScreenController(this)
    val dynamicIslandController = DynamicIslandController(this)
    val notificationCenterController = NotificationCenterController(this)
    val mediaSessionBridge = MediaSessionBridge(this)
    setContent {
      val viewModel: LauncherViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsState()
      val dynamicIslandState by dynamicIslandController.state.collectAsState()
      val notifications by notificationCenterController.notifications.collectAsState()
      var showNotificationCenter by remember { mutableStateOf(false) }
      val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
      ) { result ->
        if (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true || result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
          viewModel.refreshWeather()
        }
      }
      LaunchedEffect(uiState.batteryState, uiState.quickToggles.isBluetoothEnabled) {
        val bluetoothConnected = if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
          runCatching {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            adapter?.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED ||
              adapter?.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
          }.getOrDefault(false)
        } else false
        dynamicIslandController.updateSystemState(
          charging = uiState.batteryState.isCharging,
          batteryPercent = uiState.batteryState.percentage,
          bluetoothConnected = bluetoothConnected
        )
      }

      LaunchedEffect(Unit) {
        while (true) {
          mediaSessionBridge.refresh { title, artist, playing ->
            dynamicIslandController.setMusic(title, artist, playing)
          }
          kotlinx.coroutines.delay(1500L)
        }
      }
      LaunchedEffect(Unit) {
        val coarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val fine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (coarse || fine) viewModel.refreshWeather()
      }
      val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
      ) { granted ->
        if (granted) viewModel.refreshCalendarEvents()
      }
      LaunchedEffect(Unit) {
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
          viewModel.refreshCalendarEvents()
        }
      }
      val showLockScreen by lockScreenController.visible.collectAsState()
      MyApplicationTheme(darkTheme = uiState.darkTheme, dynamicColor = false) {
        BackHandler { }
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color.Black
        ) {
          androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            HomeScreen(viewModel = viewModel, subscriptionManager = subscriptionManager, activity = this@MainActivity, onRequestWeatherPermission = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) }, onRequestCalendarPermission = { calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR) }, lockScreenEnabled = lockScreenController.enabled, onLockScreenEnabledChange = { lockScreenController.setEnabled(it) }, onOpenNotificationCenter = { showNotificationCenter = true })

            DynamicIsland(
              state = dynamicIslandState,
              onMusicPlayPause = { mediaSessionBridge.togglePlayPause() },
              onMusicNext = { mediaSessionBridge.next() },
              onMusicPrevious = { mediaSessionBridge.previous() },
              modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
            )

            if (showNotificationCenter) {
              NotificationCenter(
                notifications = notifications,
                onDismiss = { showNotificationCenter = false },
                onClearAll = { notificationCenterController.clearAll() },
                onRemove = { notificationCenterController.remove(it) },
                onOpenSettings = {
                  runCatching { startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
                },
                onOpenApp = { packageName ->
                  runCatching {
                    packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
                  }
                }
              )
            }

            if (showLockScreen) {
              LockScreenOverlay(
                timeString = uiState.timeString,
                dateString = uiState.dateString,
                weather = uiState.weatherState,
                battery = uiState.batteryState,
                onDismiss = { lockScreenController.dismiss() },
                onFlashlight = { viewModel.toggleFlashlight() },
                onCamera = {
                  runCatching {
                    startActivity(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Retained for backwards compatibility with baseline tests.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
