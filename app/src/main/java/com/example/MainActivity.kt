package com.example

import android.os.Bundle
import android.content.Intent
import android.provider.MediaStore
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.HomeScreen
import com.example.ui.LockScreenOverlay
import com.example.ui.LockScreenController
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
    setContent {
      val viewModel: LauncherViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsState()
      val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
      ) { result ->
        if (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true || result[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
          viewModel.refreshWeather()
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
            HomeScreen(viewModel = viewModel, subscriptionManager = subscriptionManager, activity = this@MainActivity, onRequestWeatherPermission = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) }, onRequestCalendarPermission = { calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR) })

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
