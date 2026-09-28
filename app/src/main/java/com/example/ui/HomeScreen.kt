package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.ui.components.AppIconItem
import com.example.ui.components.AppLibrarySheet
import com.example.ui.components.BatteryWidget
import com.example.ui.components.CalendarWidget
import com.example.ui.components.RecentAppsWidget
import com.example.ui.components.ClockWidget
import com.example.ui.components.ControlCenterWidget
import com.example.ui.components.BottomDock
import com.example.ui.components.GlassCard
import com.example.ui.components.TopStatusBar
import com.example.ui.components.WeatherWidget
import com.example.ui.theme.CupertinoBlue
import com.example.ui.theme.CupertinoGreen
import com.example.ui.theme.CupertinoOrange
import com.example.ui.theme.CupertinoYellow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteMedium
import com.example.ui.theme.WallpaperCyanGlow
import com.example.ui.theme.WallpaperDarkNavy
import com.example.ui.theme.WallpaperDeepIndigo
import com.example.ui.theme.WallpaperSunsetMagenta
import com.example.ui.theme.WallpaperVibrantViolet
import com.example.viewmodel.LauncherViewModel
import com.example.billing.SubscriptionManager
import com.example.ads.AdsManager
import com.example.ui.components.LauncherBannerAd
import com.example.ui.components.PremiumSheet
import android.app.Activity
import android.media.AudioManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Main Launcher OS 27 Screen featuring iOS 27 glassmorphism, widgets, home grid, and app library.
 */
@Composable
fun HomeScreen(
  viewModel: LauncherViewModel,
  subscriptionManager: SubscriptionManager,
  activity: Activity,
  onRequestWeatherPermission: () -> Unit = {},
  onRequestCalendarPermission: () -> Unit = {},
  lockScreenEnabled: Boolean = true,
  onLockScreenEnabledChange: (Boolean) -> Unit = {},
  onOpenNotificationCenter: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val isPremium by subscriptionManager.premium.collectAsState()
  var showPremium by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  var showControlCenter by remember { mutableStateOf(false) }
  var showDeviceDashboard by remember { mutableStateOf(false) }
  var showLauncherSettings by remember { mutableStateOf(false) }
  var showWidgetSettings by remember { mutableStateOf(false) }
  var editMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
  var selectedActionApp by remember { mutableStateOf<AppInfo?>(null) }
  var pendingFolderApp by remember { mutableStateOf<AppInfo?>(null) }
  var showFolderPicker by remember { mutableStateOf(false) }
  var showCreateFolderPicker by remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.feedbackMessage) {
    uiState.feedbackMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      viewModel.clearFeedbackMessage()
    }
  }

  // Background iOS 27 Mesh Gradient
  val wallpaperBrush = when (uiState.wallpaperStyle) {
    1 -> Brush.verticalGradient(listOf(WallpaperDarkNavy, WallpaperDeepIndigo, WallpaperCyanGlow.copy(alpha = 0.75f), WallpaperDarkNavy))
    2 -> Brush.verticalGradient(listOf(Color(0xFF111111), Color(0xFF303030), Color(0xFF707070), Color(0xFF111111)))
    else -> Brush.verticalGradient(listOf(WallpaperDarkNavy, WallpaperDeepIndigo, WallpaperVibrantViolet, WallpaperSunsetMagenta.copy(alpha = 0.85f), WallpaperCyanGlow.copy(alpha = 0.5f), WallpaperDarkNavy))
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(wallpaperBrush)
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // Top Status Bar & Dynamic Island
      Box {
        TopStatusBar(
          timeString = uiState.timeString,
          battery = uiState.batteryState,
          appCount = uiState.installedApps.size,
          dateString = uiState.dateString
        )
        Row(
          modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          androidx.compose.material3.TextButton(onClick = onOpenNotificationCenter) {
            Text("NOTIF", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
          }
          androidx.compose.material3.TextButton(onClick = { showControlCenter = true }) {
            Text("CC", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
          }
          androidx.compose.material3.TextButton(onClick = { showLauncherSettings = true }) {
            Text("⚙", color = Color.White, fontSize = 14.sp)
          }
          androidx.compose.material3.TextButton(onClick = { showWidgetSettings = true }) {
            Text("WIDGETS", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
          }
          androidx.compose.material3.TextButton(onClick = { viewModel.openDefaultLauncherSettings() }) {
            Text("SET HOME", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
          }
          androidx.compose.material3.TextButton(onClick = { editMode = !editMode }) {
            Text(if (editMode) "DONE" else "EDIT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
          }
          if (!isPremium) {
            androidx.compose.material3.TextButton(onClick = { showPremium = true }) {
              Text("PREMIUM", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Main Horizontal Pager for Home Pages
      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) { pageIndex ->
        when (pageIndex) {
          0 -> WidgetsPage(
            uiState = uiState,
            onOpenWidgetSettings = { showWidgetSettings = true },
            onOpenSettings = { viewModel.openSettings() },
            onRefreshWeather = { onRequestWeatherPermission(); viewModel.refreshWeather() },
            onToggleWifi = { viewModel.toggleWifi() },
            onToggleBluetooth = { viewModel.toggleBluetooth() },
            onToggleFlashlight = { viewModel.toggleFlashlight() },
            onToggleAirplane = { viewModel.toggleAirplane() },
            onRecentAppClick = { app ->
              val showAd = !isPremium && AdsManager.registerAppLaunch(activity)
              if (showAd) AdsManager.showInterstitial(activity) { viewModel.launchApp(app) }
              else viewModel.launchApp(app)
            },
            onRequestCalendarPermission = onRequestCalendarPermission
          )
          1 -> HomeGridPage(
            uiState = uiState,
            onAppClick = { app ->
              val showAd = !isPremium && AdsManager.registerAppLaunch(activity)
              if (showAd) {
                AdsManager.showInterstitial(activity) { viewModel.launchApp(app) }
              } else {
                viewModel.launchApp(app)
              }
            },
            onOpenClock = { viewModel.openSettings() },
            onOpenWeather = { onRequestWeatherPermission(); viewModel.refreshWeather() },
            editMode = editMode,
            iconSizeDp = uiState.iconSizeDp,
            gridColumns = uiState.gridColumns,
            onReorder = { from, to -> viewModel.reorderHomeApps(from, to) },
            onAddToDock = { viewModel.addAppToDock(it) },
            onRemoveFromHome = { viewModel.removeAppFromHome(it) },
            onLongPress = { selectedActionApp = it },
            onOpenFolder = { viewModel.openFolder(it) }
          )
          2 -> AppLibrarySheet(
            apps = uiState.installedApps,
            recentApps = uiState.recentApps,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
            onAppClick = { viewModel.launchApp(it) }
          )
        }
      }

      // Page Indicator Dots
      PageIndicators(
        pageCount = 3,
        currentPage = pagerState.currentPage,
        onDotClick = { page ->
          coroutineScope.launch { pagerState.animateScrollToPage(page) }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp)
      )

      LauncherBannerAd(
        visible = !isPremium,
        modifier = Modifier.padding(horizontal = 10.dp)
      )

      // Frosted Glass Bottom Dock
      BottomDock(
        dockApps = uiState.dockApps,
        editMode = editMode,
        onAppClick = { viewModel.launchApp(it) },
        onReorder = { from, to -> viewModel.reorderDockApps(from, to) },
        onRemoveFromDock = { viewModel.removeAppFromDock(it) },
        dockOpacity = uiState.dockOpacity,
        iconSizeDp = uiState.iconSizeDp,
        onLongPress = { selectedActionApp = it },
        modifier = Modifier
          .navigationBarsPadding()
          .padding(bottom = 12.dp)
      )
    }

    uiState.openFolder?.let { folder ->
      FolderDialog(
        folder = folder,
        apps = uiState.installedApps,
        editMode = editMode,
        onDismiss = { viewModel.closeFolder() },
        onLaunch = { viewModel.launchApp(it) },
        onRename = { viewModel.renameFolder(folder.folderId, it) },
        onRemove = { viewModel.removeAppFromFolder(folder.folderId, it) },
        onAdd = { viewModel.addAppToFolder(folder.folderId, it) }
      )
    }

    selectedActionApp?.let { app ->
      AppActionDialog(
        app = app,
        onDismiss = { selectedActionApp = null },
        onOpen = { selectedActionApp = null; viewModel.launchApp(app) },
        onInfo = { selectedActionApp = null; viewModel.openAppInfo(app) },
        onUninstall = { selectedActionApp = null; viewModel.uninstallApp(app) },
        onRemoveHome = { selectedActionApp = null; viewModel.removeAppFromHome(app) },
        onAddDock = { selectedActionApp = null; viewModel.addAppToDock(app) },
        onAddFolder = { pendingFolderApp = app; selectedActionApp = null; showFolderPicker = true },
        onCreateFolder = { pendingFolderApp = app; selectedActionApp = null; showCreateFolderPicker = true },
        onRenameFolder = { selectedActionApp = null; viewModel.openFolder(app) },
        onRemoveFolder = { selectedActionApp = null; viewModel.removeFolder(app.folderId) }
      )
    }

    if (showFolderPicker) {
      FolderPickerDialog(
        folders = uiState.homeApps.filter { it.isFolder },
        onDismiss = { pendingFolderApp = null; showFolderPicker = false },
        onSelect = { folder ->
          pendingFolderApp?.let { viewModel.addAppToFolder(folder.folderId, it) }
          pendingFolderApp = null
          showFolderPicker = false
        }
      )
    }

    if (showCreateFolderPicker) {
      AppPickerDialog(
        title = "Create Folder With…",
        apps = uiState.homeApps.filter { !it.isFolder && it != pendingFolderApp },
        onDismiss = { pendingFolderApp = null; showCreateFolderPicker = false },
        onSelect = { second ->
          pendingFolderApp?.let { first -> viewModel.createFolder(first, second) }
          pendingFolderApp = null
          showCreateFolderPicker = false
        }
      )
    }

    if (showLauncherSettings) {
      LauncherSettingsDialog(
        state = uiState,
        onDismiss = { showLauncherSettings = false },
        onIconSize = viewModel::setIconSize,
        onGridColumns = viewModel::setGridColumns,
        onBlur = viewModel::setBlurStrength,
        onDockOpacity = viewModel::setDockOpacity,
        onAnimations = viewModel::setAnimationsEnabled,
        onDarkTheme = viewModel::setDarkTheme,
        onWallpaper = viewModel::setWallpaperStyle,
        onSetHome = viewModel::openDefaultLauncherSettings,
        lockScreenEnabled = lockScreenEnabled,
        onLockScreenEnabled = onLockScreenEnabledChange
      )
    }

    if (showPremium) {
      PremiumSheet(
        manager = subscriptionManager,
        activity = activity,
        onDismiss = { showPremium = false }
      )
    }

    if (showControlCenter) {
      ControlCenterOverlay(
        toggleState = uiState.quickToggles,
        onDismiss = { showControlCenter = false },
        onWifi = viewModel::toggleWifi,
        onBluetooth = viewModel::toggleBluetooth,
        onFlashlight = viewModel::toggleFlashlight,
        onAirplane = viewModel::toggleAirplane,
        onSettings = { showControlCenter = false; showLauncherSettings = true },
        activity = activity,
        onDeviceDashboard = { showControlCenter = false; showDeviceDashboard = true }
      )
    }

    if (showDeviceDashboard) {
      Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { showDeviceDashboard = false },
        contentAlignment = Alignment.Center
      ) {
        Box(Modifier.padding(14.dp).clickable(enabled = false) {}) {
          DeviceDashboard(onDismiss = { showDeviceDashboard = false })
        }
      }
    }

    if (showWidgetSettings) {
      AlertDialog(
        onDismissRequest = { showWidgetSettings = false },
        title = { Text("Home Widgets") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val options = listOf("clock" to "Clock", "weather" to "Weather", "battery" to "Battery", "calendar" to "Calendar", "recent" to "Recent Apps")
            options.forEach { (id, label) ->
              val index = uiState.enabledWidgets.indexOf(id)
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = index >= 0, onCheckedChange = { viewModel.setWidgetEnabled(id, it) })
                Text(label, modifier = Modifier.weight(1f))
                TextButton(enabled = index > 0, onClick = { viewModel.moveWidget(id, -1) }) { Text("↑") }
                TextButton(enabled = index >= 0 && index < uiState.enabledWidgets.lastIndex, onClick = { viewModel.moveWidget(id, 1) }) { Text("↓") }
              }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { viewModel.resetWidgetOrder() }, modifier = Modifier.fillMaxWidth()) {
              Text("Reset widget order")
            }
          }
        },
        confirmButton = { TextButton(onClick = { showWidgetSettings = false }) { Text("DONE") } }
      )
    }

    if (showControlCenter) {
      ControlCenterOverlay(
        toggleState = uiState.quickToggles,
        onDismiss = { showControlCenter = false },
        onWifi = viewModel::toggleWifi,
        onBluetooth = viewModel::toggleBluetooth,
        onFlashlight = viewModel::toggleFlashlight,
        onAirplane = viewModel::toggleAirplane,
        onSettings = { showControlCenter = false; showLauncherSettings = true },
        activity = activity,
        onDeviceDashboard = { showControlCenter = false; showDeviceDashboard = true }
      )
    }

    if (showDeviceDashboard) {
      Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)).clickable { showDeviceDashboard = false },
        contentAlignment = Alignment.Center
      ) {
        Box(Modifier.padding(14.dp).clickable(enabled = false) {}) {
          DeviceDashboard(onDismiss = { showDeviceDashboard = false })
        }
      }
    }

    if (showWidgetSettings) {
      AlertDialog(
        onDismissRequest = { showWidgetSettings = false },
        title = { Text("Home Widgets") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val options = listOf("clock" to "Clock", "weather" to "Weather", "battery" to "Battery", "calendar" to "Calendar", "recent" to "Recent Apps")
            options.forEach { (id, label) ->
              val index = uiState.enabledWidgets.indexOf(id)
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = index >= 0, onCheckedChange = { viewModel.setWidgetEnabled(id, it) })
                Text(label, modifier = Modifier.weight(1f))
                TextButton(enabled = index > 0, onClick = { viewModel.moveWidget(id, -1) }) { Text("↑") }
                TextButton(enabled = index >= 0 && index < uiState.enabledWidgets.lastIndex, onClick = { viewModel.moveWidget(id, 1) }) { Text("↓") }
              }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { viewModel.resetWidgetOrder() }, modifier = Modifier.fillMaxWidth()) {
              Text("Reset widget order")
            }
          }
        },
        confirmButton = { TextButton(onClick = { showWidgetSettings = false }) { Text("DONE") } }
      )
    }

    // Snackbar Host
    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 110.dp)
    )

    // Loading indicator on first launch
    AnimatedVisibility(
      visible = uiState.isLoading,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      GlassCard(
        modifier = Modifier.padding(24.dp),
        backgroundColor = GlassWhiteHigh
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator(
            color = CupertinoBlue,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Loading Launcher OS 27...",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

/**
 * Page 0: Glassmorphic Widgets Dashboard.
 */
@Composable
private fun WidgetsPage(
  uiState: com.example.viewmodel.LauncherUiState,
  onOpenWidgetSettings: () -> Unit,
  onOpenSettings: () -> Unit,
  onRefreshWeather: () -> Unit,
  onToggleWifi: () -> Unit,
  onToggleBluetooth: () -> Unit,
  onToggleFlashlight: () -> Unit,
  onToggleAirplane: () -> Unit,
  onRecentAppClick: (com.example.model.AppModel) -> Unit,
  onRequestCalendarPermission: () -> Unit
) {
  val scrollState = rememberScrollState()
  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Text("WIDGETS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
      TextButton(onClick = onOpenWidgetSettings) { Text("EDIT", color = Color.White, fontSize = 11.sp) }
    }
    if (uiState.enabledWidgets.contains("clock")) ClockWidget(uiState.timeString, uiState.dateString, uiState.dayString, onClick = onOpenSettings)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      if (uiState.enabledWidgets.contains("weather")) WeatherWidget(uiState.weatherState, Modifier.weight(1f), onRefreshWeather)
      if (uiState.enabledWidgets.contains("battery")) BatteryWidget(uiState.batteryState, Modifier.weight(1f), onOpenSettings)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      if (uiState.enabledWidgets.contains("calendar")) CalendarWidget(uiState.dateString, uiState.dayString, uiState.nextCalendarEvent, Modifier.weight(1f), onClick = {
        if (uiState.nextCalendarEvent == null) onRequestCalendarPermission() else onOpenSettings()
      })
      if (uiState.enabledWidgets.contains("recent")) RecentAppsWidget(uiState.recentApps, Modifier.weight(1f), onAppClick = onRecentAppClick)
    }
    ControlCenterWidget(uiState.quickToggles, onToggleWifi = onToggleWifi, onToggleBluetooth = onToggleBluetooth, onToggleFlashlight = onToggleFlashlight, onToggleAirplane = onToggleAirplane, onOpenSettings = onOpenSettings)
    Spacer(Modifier.height(20.dp))
  }
}

/**
 * Page 1: Main Home Screen with top widgets and 4-column app grid.
 */
@Composable
private fun HomeGridPage(
  uiState: com.example.viewmodel.LauncherUiState,
  onAppClick: (AppInfo) -> Unit,
  onOpenClock: () -> Unit,
  onOpenWeather: () -> Unit,
  editMode: Boolean,
  iconSizeDp: Int = 58,
  gridColumns: Int = 4,
  onReorder: (Int, Int) -> Unit,
  onAddToDock: (AppInfo) -> Unit,
  onRemoveFromHome: (AppInfo) -> Unit,
  onLongPress: (AppInfo) -> Unit,
  onOpenFolder: (AppInfo) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("home_grid_page")
  ) {
    // Top Compact Glance Widgets: Clock & Weather Side-by-Side
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      GlassCard(
        modifier = Modifier
          .weight(1f)
          .height(84.dp),
        backgroundColor = GlassWhiteMedium,
        shape = RoundedCornerShape(22.dp),
        onClick = onOpenClock
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = uiState.dayString.uppercase(),
            color = CupertinoOrange,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
          Text(
            text = uiState.timeString,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      GlassCard(
        modifier = Modifier
          .weight(1f)
          .height(84.dp),
        backgroundColor = GlassWhiteMedium,
        shape = RoundedCornerShape(22.dp),
        onClick = onOpenWeather
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = uiState.weatherState.location,
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${uiState.weatherState.temperatureCelsius}°",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Text(
            text = uiState.weatherState.condition,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp
          )
        }
      }
    }

    // Cache folder previews once per installed-app snapshot instead of filtering the full list for every grid item.
    val installedByKey = remember(uiState.installedApps) {
      uiState.installedApps.associateBy { it.packageName + "|" + it.activityName }
    }

    // 4-Column iOS Style App Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(gridColumns),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(uiState.homeApps, key = { it.packageName + it.activityName }) { app ->
        val index = uiState.homeApps.indexOf(app)
        var dragOffset by androidx.compose.runtime.remember(app.packageName) { androidx.compose.runtime.mutableFloatStateOf(0f) }
        AppIconItem(
          app = app,
          iconSize = iconSizeDp.dp,
          showLabel = true,
          animatePress = uiState.animationsEnabled,
          folderPreviewApps = if (app.isFolder) app.folderAppKeys.mapNotNull { installedByKey[it] }.take(4) else emptyList(),
          modifier = if (editMode) Modifier.pointerInput(app.packageName, uiState.homeApps) {
            detectDragGesturesAfterLongPress(
              onDragStart = { dragOffset = 0f },
              onDragCancel = { dragOffset = 0f },
              onDragEnd = { dragOffset = 0f },
              onDrag = { change, amount ->
                change.consume()
                dragOffset += amount.y
                if (dragOffset > 70f && index < uiState.homeApps.lastIndex) {
                  onReorder(index, index + 1)
                  dragOffset = 0f
                } else if (dragOffset < -70f && index > 0) {
                  onReorder(index, index - 1)
                  dragOffset = 0f
                }
              }
            )
          } else Modifier,
          onClick = {
            if (app.isFolder) onOpenFolder(app)
            else if (editMode) onAddToDock(app) else onAppClick(app)
          },
          onLongClick = { onLongPress(app) }
        )
      }
    }
  }
}


@Composable
private fun LauncherSettingsDialog(
  state: com.example.viewmodel.LauncherUiState,
  onDismiss: () -> Unit,
  onIconSize: (Int) -> Unit,
  onGridColumns: (Int) -> Unit,
  onBlur: (Float) -> Unit,
  onDockOpacity: (Float) -> Unit,
  onAnimations: (Boolean) -> Unit,
  onDarkTheme: (Boolean) -> Unit,
  onWallpaper: (Int) -> Unit,
  onSetHome: () -> Unit,
  lockScreenEnabled: Boolean,
  onLockScreenEnabled: (Boolean) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Launcher Settings") },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Home Screen", fontWeight = FontWeight.Bold)
        Text("Icon size: ${state.iconSizeDp} dp")
        Slider(value = state.iconSizeDp.toFloat(), onValueChange = { onIconSize(it.toInt()) }, valueRange = 44f..76f, steps = 7)
        Text("Grid: ${state.gridColumns} columns")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf(3, 4, 5).forEach { cols ->
            TextButton(onClick = { onGridColumns(cols) }) { Text(if (state.gridColumns == cols) "[$cols]" else "$cols") }
          }
        }
        Divider()
        Text("Appearance", fontWeight = FontWeight.Bold)
        Text("Glass / blur strength")
        Slider(value = state.blurStrength, onValueChange = onBlur, valueRange = 0.2f..1f)
        Text("Dock opacity")
        Slider(value = state.dockOpacity, onValueChange = onDockOpacity, valueRange = 0.25f..0.9f)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Animations")
          Switch(checked = state.animationsEnabled, onCheckedChange = onAnimations)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Dark theme")
          Switch(checked = state.darkTheme, onCheckedChange = onDarkTheme)
        }
        Text("Lock Screen", fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("In-app Lock Screen")
          Switch(checked = lockScreenEnabled, onCheckedChange = onLockScreenEnabled)
        }
        Divider()
        Text("Wallpaper", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf("Aurora", "Cyan", "Mono").forEachIndexed { index, label ->
            TextButton(onClick = { onWallpaper(index) }) { Text(if (state.wallpaperStyle == index) "✓ $label" else label) }
          }
        }
        Divider()
        Text("System", fontWeight = FontWeight.Bold)
        TextButton(onClick = onSetHome, modifier = Modifier.fillMaxWidth()) { Text("Set as Default Launcher") }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
  )
}

@Composable
private fun AppActionDialog(
  app: AppInfo,
  onDismiss: () -> Unit,
  onOpen: () -> Unit,
  onInfo: () -> Unit,
  onUninstall: () -> Unit,
  onRemoveHome: () -> Unit,
  onAddDock: () -> Unit,
  onAddFolder: () -> Unit,
  onCreateFolder: () -> Unit,
  onRenameFolder: () -> Unit,
  onRemoveFolder: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (app.isFolder) app.folderName else app.label) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (app.isFolder) {
          TextButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Open Folder") }
          TextButton(onClick = onRenameFolder, modifier = Modifier.fillMaxWidth()) { Text("Rename Folder") }
          TextButton(onClick = onRemoveFolder, modifier = Modifier.fillMaxWidth()) { Text("Remove Folder") }
        } else {
          TextButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Open") }
          TextButton(onClick = onInfo, modifier = Modifier.fillMaxWidth()) { Text("App Info") }
          TextButton(onClick = onAddDock, modifier = Modifier.fillMaxWidth()) { Text("Add to Dock") }
          TextButton(onClick = onAddFolder, modifier = Modifier.fillMaxWidth()) { Text("Add to Folder") }
          TextButton(onClick = onCreateFolder, modifier = Modifier.fillMaxWidth()) { Text("Create Folder") }
          TextButton(onClick = onRemoveHome, modifier = Modifier.fillMaxWidth()) { Text("Remove from Home Screen") }
          TextButton(onClick = onUninstall, modifier = Modifier.fillMaxWidth()) { Text("Uninstall") }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
  )
}

@Composable
private fun FolderPickerDialog(
  folders: List<AppInfo>,
  onDismiss: () -> Unit,
  onSelect: (AppInfo) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add to Folder") },
    text = {
      if (folders.isEmpty()) {
        Text("No folders yet. Use Create Folder first.")
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          folders.forEach { folder ->
            TextButton(onClick = { onSelect(folder) }, modifier = Modifier.fillMaxWidth()) {
              Text(folder.folderName)
            }
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
  )
}

@Composable
private fun AppPickerDialog(
  title: String,
  apps: List<AppInfo>,
  onDismiss: () -> Unit,
  onSelect: (AppInfo) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        apps.take(40).forEach { app ->
          TextButton(onClick = { onSelect(app) }, modifier = Modifier.fillMaxWidth()) {
            Text(app.label, maxLines = 1)
          }
        }
        if (apps.isEmpty()) Text("No compatible apps available.")
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
  )
}

@Composable
private fun FolderDialog(
  folder: AppInfo,
  apps: List<AppInfo>,
  editMode: Boolean,
  onDismiss: () -> Unit,
  onLaunch: (AppInfo) -> Unit,
  onRename: (String) -> Unit,
  onRemove: (AppInfo) -> Unit,
  onAdd: (AppInfo) -> Unit
) {
  var renameMode by remember { mutableStateOf(false) }
  var name by remember(folder.folderName) { mutableStateOf(folder.folderName) }
  val contained = folder.folderAppKeys.mapNotNull { key -> apps.firstOrNull { "${it.packageName}|${it.activityName}" == key } }
  val available = apps.filter { app -> !app.isFolder && !folder.folderAppKeys.contains("${app.packageName}|${app.activityName}") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(folder.folderName) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (renameMode) {
          OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("Folder name") })
        }
        contained.forEach { app ->
          Row(modifier = Modifier.fillMaxWidth().clickable { if (!editMode) { onLaunch(app); onDismiss() } }, verticalAlignment = Alignment.CenterVertically) {
            AppIconItem(app = app, iconSize = 42.dp, showLabel = true, onClick = { if (!editMode) { onLaunch(app); onDismiss() } }, onLongClick = null)
            if (editMode) TextButton(onClick = { onRemove(app) }) { Text("Remove") }
          }
        }
        if (editMode && available.isNotEmpty()) {
          Text("Add app", fontWeight = FontWeight.Bold, color = Color.Gray)
          available.take(8).forEach { app ->
            TextButton(onClick = { onAdd(app) }) { Text("+ ${app.label}") }
          }
        }
      }
    },
    confirmButton = {
      if (renameMode) TextButton(onClick = { onRename(name); renameMode = false }) { Text("Save") }
      else if (editMode) TextButton(onClick = { renameMode = true }) { Text("Rename") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
  )
}

/**
 * iOS 27 Page Indicators with active capsule indicator.
 */
@Composable
private fun PageIndicators(
  pageCount: Int,
  currentPage: Int,
  onDotClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .wrapContentWidth()
      .clip(RoundedCornerShape(50))
      .background(Color.Black.copy(alpha = 0.22f))
      .border(0.6.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
      .padding(horizontal = 9.dp, vertical = 5.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    repeat(pageCount) { index ->
      val isSelected = index == currentPage
      Box(
        modifier = Modifier
          .padding(horizontal = 4.dp)
          .size(
            width = if (isSelected) 18.dp else 7.dp,
            height = 7.dp
          )
          .clip(if (isSelected) RoundedCornerShape(4.dp) else CircleShape)
          .background(
            if (isSelected) Color.White else Color.White.copy(alpha = 0.35f)
          )
          .clickable { onDotClick(index) }
      )
    }


  }
}


@Composable
private fun ControlCenterOverlay(
  toggleState: com.example.model.QuickToggleState,
  onDismiss: () -> Unit,
  onWifi: () -> Unit,
  onBluetooth: () -> Unit,
  onFlashlight: () -> Unit,
  onAirplane: () -> Unit,
  onSettings: () -> Unit,
  activity: Activity,
  onDeviceDashboard: () -> Unit = {}
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val audio = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager }
  val maxVolume = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
  var volume by remember { mutableStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume) }
  val initialBrightness = remember {
    runCatching { activity.window.attributes.screenBrightness.takeIf { it >= 0f } ?: 0.5f }.getOrDefault(0.5f)
  }
  var brightness by remember { mutableStateOf(initialBrightness) }
  Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .45f)).clickable(onClick = onDismiss)) {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 58.dp).clickable(onClick = {}), backgroundColor = GlassWhiteHigh) {
      Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Column {
            Text("CONTROL CENTER", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Launcher OS 27", color = Color.White.copy(alpha = .65f), fontSize = 11.sp)
          }
          TextButton(onClick = onSettings) { Text("⚙", color = Color.White, fontSize = 18.sp) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          CCButton("Wi-Fi", toggleState.isWifiEnabled, CupertinoBlue, onWifi, Modifier.weight(1f))
          CCButton("Bluetooth", toggleState.isBluetoothEnabled, CupertinoBlue, onBluetooth, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          CCButton("Torch", toggleState.isFlashlightOn, CupertinoYellow, onFlashlight, Modifier.weight(1f))
          CCButton("Airplane", toggleState.isAirplaneMode, CupertinoOrange, onAirplane, Modifier.weight(1f))
        }
        Text("☀  Brightness", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Slider(
          value = brightness,
          onValueChange = {
            brightness = it
            activity.window.attributes = activity.window.attributes.apply { screenBrightness = it.coerceIn(0.01f, 1f) }
          },
          valueRange = 0.01f..1f,
          modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = onDeviceDashboard, modifier = Modifier.fillMaxWidth()) { Text("🔋 Battery & Device", color = Color.White, fontWeight = FontWeight.Bold) }
        Text("🔊  Volume", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Slider(value = volume, onValueChange = { volume = it; audio.setStreamVolume(AudioManager.STREAM_MUSIC, (it * maxVolume).roundToInt(), 0) }, modifier = Modifier.fillMaxWidth())
        Text("Tap outside to close", color = Color.White.copy(alpha = .55f), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
      }
    }
  }
}

@Composable
private fun CCButton(label: String, active: Boolean, activeColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(modifier.height(56.dp).clip(RoundedCornerShape(18.dp)).background(if (active) activeColor.copy(alpha = .78f) else Color.White.copy(alpha = .10f)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
    Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}
