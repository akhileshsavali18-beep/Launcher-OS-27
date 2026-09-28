package com.example.viewmodel

import android.app.Application
import android.Manifest
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.model.AppCategory
import com.example.model.AppModel
import com.example.model.BatteryState
import com.example.model.QuickToggleState
import com.example.model.WeatherState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalendarEvent(val title: String, val startMillis: Long, val allDay: Boolean = false)

data class LauncherUiState(
  val installedApps: List<AppModel> = emptyList(),
  val recentApps: List<AppModel> = emptyList(),
  val dockApps: List<AppModel> = emptyList(),
  val homeApps: List<AppModel> = emptyList(),
  val searchQuery: String = "",
  val timeString: String = "9:41",
  val dateString: String = "Sunday, Sep 20",
  val dayString: String = "Sunday",
  val batteryState: BatteryState = BatteryState(),
  val weatherState: WeatherState = WeatherState(),
  val quickToggles: QuickToggleState = QuickToggleState(),
  val isLoading: Boolean = true,
  val feedbackMessage: String? = null,
  val openFolder: AppModel? = null,
  val iconSizeDp: Int = 58,
  val gridColumns: Int = 4,
  val blurStrength: Float = 0.72f,
  val dockOpacity: Float = 0.55f,
  val animationsEnabled: Boolean = true,
  val darkTheme: Boolean = true,
  val wallpaperStyle: Int = 0,
  val enabledWidgets: List<String> = listOf("clock", "weather", "battery", "calendar", "recent"),
  val nextCalendarEvent: CalendarEvent? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("launcher_layout", android.content.Context.MODE_PRIVATE)
  private val repository = AppRepository(application)
  private val weatherRepository = com.example.data.WeatherRepository(application)

  private val settingsPrefs = application.getSharedPreferences("launcher_settings", android.content.Context.MODE_PRIVATE)

  // StateFlow holding the list of installed applications
  private val _installedApps = MutableStateFlow<List<AppModel>>(emptyList())
  val installedApps: StateFlow<List<AppModel>> = _installedApps.asStateFlow()

  private val _uiState = MutableStateFlow(LauncherUiState())
  val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

  init {
    loadApps()
    startTimeUpdates()
    updateBatteryStatus()
    loadLauncherSettings()
    refreshQuickToggleState()
    restoreWidgetSettings()
    refreshCalendarEvents()
  }


  private fun loadLauncherSettings() {
    _uiState.update { it.copy(
      iconSizeDp = settingsPrefs.getInt("icon_size", 58).coerceIn(44, 76),
      gridColumns = settingsPrefs.getInt("grid_columns", 4).coerceIn(3, 5),
      blurStrength = settingsPrefs.getFloat("blur_strength", 0.72f).coerceIn(0.2f, 1f),
      dockOpacity = settingsPrefs.getFloat("dock_opacity", 0.55f).coerceIn(0.25f, 0.9f),
      animationsEnabled = settingsPrefs.getBoolean("animations", true),
      darkTheme = settingsPrefs.getBoolean("dark_theme", true),
      wallpaperStyle = settingsPrefs.getInt("wallpaper_style", 0).coerceIn(0, 2)
    ) }
  }

  fun setIconSize(value: Int) { val v = value.coerceIn(44, 76); settingsPrefs.edit().putInt("icon_size", v).apply(); _uiState.update { it.copy(iconSizeDp = v) } }
  fun setGridColumns(value: Int) { val v = value.coerceIn(3, 5); settingsPrefs.edit().putInt("grid_columns", v).apply(); _uiState.update { it.copy(gridColumns = v) } }
  fun setBlurStrength(value: Float) { val v = value.coerceIn(0.2f, 1f); settingsPrefs.edit().putFloat("blur_strength", v).apply(); _uiState.update { it.copy(blurStrength = v) } }
  fun setDockOpacity(value: Float) { val v = value.coerceIn(0.25f, 0.9f); settingsPrefs.edit().putFloat("dock_opacity", v).apply(); _uiState.update { it.copy(dockOpacity = v) } }
  fun setAnimationsEnabled(value: Boolean) { settingsPrefs.edit().putBoolean("animations", value).apply(); _uiState.update { it.copy(animationsEnabled = value) } }
  fun setDarkTheme(value: Boolean) { settingsPrefs.edit().putBoolean("dark_theme", value).apply(); _uiState.update { it.copy(darkTheme = value) } }
  fun setWallpaperStyle(value: Int) { val v = value.coerceIn(0, 2); settingsPrefs.edit().putInt("wallpaper_style", v).apply(); _uiState.update { it.copy(wallpaperStyle = v) } }
  fun refreshApps() {
    loadApps()
  }

  fun refreshWeather() {
    viewModelScope.launch {
      val weather = runCatching { weatherRepository.fetchCurrentWeather() }.getOrNull()
      if (weather != null) {
        _uiState.update { it.copy(weatherState = weather, feedbackMessage = null) }
      } else {
        _uiState.update { it.copy(feedbackMessage = "Allow location access and try weather refresh again.") }
      }
    }
  }

  private fun loadApps() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      
      // Query real installed apps via PackageManager
      val realApps = repository.loadInstalledApps().map { appInfo ->
        AppModel(
          appName = appInfo.label,
          packageName = appInfo.packageName,
          icon = appInfo.iconBitmap,
          activityName = appInfo.activityName,
          category = appInfo.category
        )
      }

      // Fallback to mock data for preview/testing environments if no real packages found
      val apps = if (realApps.isEmpty()) {
        getMockApps()
      } else {
        realApps
      }

      _installedApps.value = apps

      // Reconcile persisted state against the current package list. This
      // automatically drops uninstalled apps from Home/Dock/Folders while
      // preserving the user's order for everything that still exists.
      reconcilePersistedLayout(apps)

      // Restore the user's saved dock/home order. New apps are appended automatically.
      val dockApps = restoreDockApps(apps)
      val homeApps = restoreHomeApps(apps, dockApps)

      // Persist the reconciled state so stale package/activity keys cannot
      // accumulate across repeated install/uninstall cycles.
      saveDockOrder(dockApps)
      saveHomeOrder(homeApps)
      saveFolders(restoreFolders(apps, dockApps))

      _uiState.update {
        it.copy(
          installedApps = apps,
          recentApps = restoreRecentApps(apps),
          dockApps = dockApps,
          homeApps = homeApps,
          isLoading = false
        )
      }
    }
  }

  private fun reconcilePersistedLayout(apps: List<AppModel>) {
    if (apps.isEmpty()) return
    val validKeys = apps.map(::appKey).toSet()

    val cleanDock = prefs.getString("dock_order", null)?.split("\n")
      .orEmpty().filter { it.isNotBlank() && validKeys.contains(it) }.distinct()
    prefs.edit().putString("dock_order", cleanDock.take(4).joinToString("\n")).apply()

    val cleanHome = prefs.getString("home_order", null)?.split("\n")
      .orEmpty().filter { it.isNotBlank() && (it.startsWith("folder://") || validKeys.contains(it)) }.distinct()
    prefs.edit().putString("home_order", cleanHome.joinToString("\n")).apply()

    val hidden = prefs.getStringSet("hidden_home", emptySet()).orEmpty()
      .filter { validKeys.contains(it) }.toSet()
    prefs.edit().putStringSet("hidden_home", hidden).apply()

    // Folder records are validated by restoreFolders(); rewrite them here so
    // removed apps disappear from persisted folder membership as well.
    val restoredFolders = restoreFolders(apps, cleanDock.mapNotNull { key -> apps.firstOrNull { appKey(it) == key } })
    saveFolders(restoredFolders)
  }

  private fun restoreRecentApps(apps: List<AppModel>): List<AppModel> {
    val byKey = apps.associateBy(::appKey)
    val keys = prefs.getString("recent_apps", null)?.split("\n").orEmpty().filter { it.isNotBlank() }
    return keys.mapNotNull { byKey[it] }.distinctBy(::appKey).take(12)
  }

  private fun saveRecentApps(apps: List<AppModel>) {
    prefs.edit().putString("recent_apps", apps.map(::appKey).distinct().take(12).joinToString("\n")).apply()
  }

  private fun getMockApps(): List<AppModel> {
    return listOf(
      AppModel("Phone", "com.android.dialer", null, "", AppCategory.SOCIAL),
      AppModel("Messages", "com.android.messaging", null, "", AppCategory.SOCIAL),
      AppModel("Chrome", "com.android.chrome", null, "", AppCategory.UTILITIES),
      AppModel("Camera", "com.android.camera2", null, "", AppCategory.CREATIVE),
      AppModel("Photos", "com.google.android.apps.photos", null, "", AppCategory.CREATIVE),
      AppModel("Calendar", "com.google.android.calendar", null, "", AppCategory.PRODUCTIVITY),
      AppModel("Notes", "com.apple.notes", null, "", AppCategory.PRODUCTIVITY),
      AppModel("Spotify", "com.spotify.music", null, "", AppCategory.ENTERTAINMENT),
      AppModel("YouTube", "com.google.android.youtube", null, "", AppCategory.ENTERTAINMENT),
      AppModel("Settings", "com.android.settings", null, "", AppCategory.SYSTEM)
    )
  }


  private fun appKey(app: AppModel): String = "${app.packageName}|${app.activityName}"

  private fun restoreDockApps(apps: List<AppModel>): List<AppModel> {
    val saved = prefs.getString("dock_order", null)?.split("\n").orEmpty().filter { it.isNotBlank() }
    val byKey = apps.associateBy(::appKey)
    val restored = saved.mapNotNull { byKey[it] }.toMutableList()
    if (restored.isEmpty()) restored.addAll(selectDockApps(apps))
    apps.forEach { app ->
      if (restored.size < 4 && !restored.contains(app)) restored.add(app)
    }
    return restored.take(4)
  }

  private fun restoreHomeApps(apps: List<AppModel>, dockApps: List<AppModel>): List<AppModel> {
    val savedOrder = prefs.getString("home_order", null)?.split("\n").orEmpty().filter { it.isNotBlank() }
    val hidden = prefs.getStringSet("hidden_home", emptySet()).orEmpty()
    val byKey = apps.associateBy(::appKey)
    val available = apps.filterNot { dockApps.contains(it) || hidden.contains(appKey(it)) }
    val folderItems = restoreFolders(apps, dockApps)
    val folderContainedKeys = folderItems.flatMap { it.folderAppKeys }.toSet()
    val ordered = savedOrder.mapNotNull { key ->
      folderItems.firstOrNull { appKey(it) == key } ?: byKey[key]
    }.filterNot { item ->
      (!item.isFolder && (dockApps.contains(item) || hidden.contains(appKey(item)) || folderContainedKeys.contains(appKey(item))))
    }.toMutableList()
    available.filterNot { folderContainedKeys.contains(appKey(it)) }.forEach { if (!ordered.contains(it)) ordered.add(it) }
    folderItems.forEach { folder -> if (!ordered.contains(folder)) ordered.add(folder) }
    return ordered
  }

  private fun restoreFolders(apps: List<AppModel>, dockApps: List<AppModel>): List<AppModel> {
    val raw = prefs.getString("folders", null).orEmpty()
    if (raw.isBlank()) return emptyList()
    val byKey = apps.associateBy(::appKey)
    return raw.split("\n").mapNotNull { line ->
      val parts = line.split("\t")
      if (parts.size < 3) return@mapNotNull null
      val id = parts[0]
      val name = parts[1].ifBlank { "Folder" }
      val keys = parts[2].split("|").filter { it.isNotBlank() }.filter { byKey.containsKey(it) && !dockApps.any { d -> appKey(d) == it } }
      if (keys.isEmpty()) null else AppModel(
        appName = name, packageName = "folder://$id", activityName = id,
        isFolder = true, folderId = id, folderName = name, folderAppKeys = keys
      )
    }
  }

  private fun saveFolders(folders: List<AppModel>) {
    val value = folders.filter { it.isFolder && it.folderAppKeys.isNotEmpty() }.joinToString("\n") {
      "${it.folderId}\t${it.folderName.replace("\t", " ").replace("\n", " ")}\t${it.folderAppKeys.joinToString("|")}"
    }
    prefs.edit().putString("folders", value).apply()
  }

  private fun currentFolders(): List<AppModel> = restoreFolders(_installedApps.value, _uiState.value.dockApps)

  fun createFolder(first: AppModel, second: AppModel, name: String = "Folder") {
    if (first.isFolder || second.isFolder || first == second) return
    val folderId = "${System.currentTimeMillis()}_${first.packageName.hashCode()}"
    val folder = AppModel(
      appName = name, packageName = "folder://$folderId", activityName = folderId,
      isFolder = true, folderId = folderId, folderName = name,
      folderAppKeys = listOf(appKey(first), appKey(second))
    )
    val home = _uiState.value.homeApps.toMutableList()
    val firstIndex = home.indexOf(first).takeIf { it >= 0 } ?: home.size
    home.remove(first); home.remove(second)
    home.add(firstIndex.coerceAtMost(home.size), folder)
    val folders = currentFolders().filterNot { it.folderAppKeys.contains(appKey(first)) || it.folderAppKeys.contains(appKey(second)) } + folder
    _uiState.update { it.copy(homeApps = home, feedbackMessage = "Folder created") }
    saveFolders(folders); saveHomeOrder(home)
  }

  fun openFolder(folder: AppModel) {
    if (folder.isFolder) _uiState.update { it.copy(openFolder = folder) }
  }

  fun closeFolder() { _uiState.update { it.copy(openFolder = null) } }

  fun renameFolder(folderId: String, name: String) {
    val trimmed = name.trim().ifBlank { "Folder" }.take(24)
    val updated = currentFolders().map { if (it.folderId == folderId) it.copy(appName = trimmed, folderName = trimmed) else it }
    val home = _uiState.value.homeApps.map { if (it.isFolder && it.folderId == folderId) it.copy(appName = trimmed, folderName = trimmed) else it }
    _uiState.update { it.copy(homeApps = home, openFolder = home.firstOrNull { it.isFolder && it.folderId == folderId }) }
    saveFolders(updated); saveHomeOrder(home)
  }

  fun removeAppFromFolder(folderId: String, app: AppModel) {
    val key = appKey(app)
    val updatedFolders = currentFolders().mapNotNull { folder ->
      if (folder.folderId != folderId) folder else {
        val keys = folder.folderAppKeys.filterNot { it == key }
        if (keys.isEmpty()) null else folder.copy(folderAppKeys = keys)
      }
    }
    val folder = updatedFolders.firstOrNull { it.folderId == folderId }
    var home = _uiState.value.homeApps.toMutableList()
    if (folder == null) {
      home = home.filterNot { it.isFolder && it.folderId == folderId }.toMutableList()
    } else {
      home = home.map { if (it.isFolder && it.folderId == folderId) folder else it }.toMutableList()
    }
    val contained = updatedFolders.flatMap { it.folderAppKeys }.toSet()
    if (!contained.contains(key) && !_uiState.value.dockApps.contains(app)) home.add(app)
    _uiState.update { it.copy(homeApps = home, openFolder = folder) }
    saveFolders(updatedFolders); saveHomeOrder(home)
  }

  fun addAppToFolder(folderId: String, app: AppModel) {
    if (app.isFolder) return
    val updatedFolders = currentFolders().map { folder ->
      if (folder.folderId == folderId && !folder.folderAppKeys.contains(appKey(app))) folder.copy(folderAppKeys = folder.folderAppKeys + appKey(app)) else folder
    }
    val folder = updatedFolders.firstOrNull { it.folderId == folderId }
    val home = _uiState.value.homeApps.filterNot { it == app }.map { if (it.isFolder && it.folderId == folderId) folder ?: it else it }
    _uiState.update { it.copy(homeApps = home, openFolder = folder) }
    saveFolders(updatedFolders); saveHomeOrder(home)
  }

  private fun saveHomeOrder(apps: List<AppModel>) {
    prefs.edit().putString("home_order", apps.joinToString("\n", transform = ::appKey)).apply()
  }

  private fun saveDockOrder(apps: List<AppModel>) {
    prefs.edit().putString("dock_order", apps.take(4).joinToString("\n", transform = ::appKey)).apply()
  }

  fun reorderHomeApps(fromIndex: Int, toIndex: Int) {
    val list = _uiState.value.homeApps.toMutableList()
    if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) return
    val item = list.removeAt(fromIndex)
    list.add(toIndex, item)
    _uiState.update { it.copy(homeApps = list) }
    saveHomeOrder(list)
  }

  fun reorderDockApps(fromIndex: Int, toIndex: Int) {
    val list = _uiState.value.dockApps.toMutableList()
    if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) return
    val item = list.removeAt(fromIndex)
    list.add(toIndex, item)
    _uiState.update { it.copy(dockApps = list) }
    saveDockOrder(list)
  }

  fun addAppToDock(app: AppModel) {
    val dock = _uiState.value.dockApps.toMutableList()
    if (dock.contains(app)) return
    if (dock.size >= 4) {
      _uiState.update { it.copy(feedbackMessage = "Dock is full. Remove an app first.") }
      return
    }
    dock.add(app)
    val home = _uiState.value.homeApps.filterNot { it == app }
    _uiState.update { it.copy(dockApps = dock, homeApps = home) }
    saveDockOrder(dock)
    saveHomeOrder(home)
  }

  fun removeAppFromDock(app: AppModel) {
    val dock = _uiState.value.dockApps.filterNot { it == app }
    val home = _uiState.value.homeApps.toMutableList().apply { if (!contains(app)) add(app) }
    _uiState.update { it.copy(dockApps = dock.take(4), homeApps = home) }
    saveDockOrder(dock.take(4))
    saveHomeOrder(home)
  }

  fun removeAppFromHome(app: AppModel) {
    val hidden = prefs.getStringSet("hidden_home", emptySet()).orEmpty().toMutableSet()
    hidden.add(appKey(app))
    val home = _uiState.value.homeApps.filterNot { it == app }
    _uiState.update { it.copy(homeApps = home) }
    prefs.edit().putStringSet("hidden_home", hidden).apply()
    saveHomeOrder(home)
  }

  fun restoreAppToHome(app: AppModel) {
    val hidden = prefs.getStringSet("hidden_home", emptySet()).orEmpty().toMutableSet()
    hidden.remove(appKey(app))
    val home = _uiState.value.homeApps.toMutableList().apply { if (!contains(app) && !_uiState.value.dockApps.contains(app)) add(app) }
    _uiState.update { it.copy(homeApps = home) }
    prefs.edit().putStringSet("hidden_home", hidden).apply()
    saveHomeOrder(home)
  }

  private fun selectDockApps(apps: List<AppModel>): List<AppModel> {
    val dockList = mutableListOf<AppModel>()

    apps.find { it.packageName.contains("dialer") || it.packageName.contains("phone") || it.appName.contains("Phone", ignoreCase = true) }
      ?.let { dockList.add(it) }

    apps.find { !dockList.contains(it) && (it.packageName.contains("messaging") || it.packageName.contains("mms") || it.appName.contains("Messages", ignoreCase = true)) }
      ?.let { dockList.add(it) }

    apps.find { !dockList.contains(it) && (it.packageName.contains("chrome") || it.packageName.contains("browser") || it.appName.contains("Chrome", ignoreCase = true)) }
      ?.let { dockList.add(it) }

    apps.find { !dockList.contains(it) && (it.packageName.contains("camera") || it.appName.contains("Camera", ignoreCase = true)) }
      ?.let { dockList.add(it) }

    if (dockList.size < 4) {
      for (app in apps) {
        if (!dockList.contains(app)) {
          dockList.add(app)
        }
        if (dockList.size == 4) break
      }
    }

    return dockList
  }

  private fun startTimeUpdates() {
    viewModelScope.launch {
      val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
      val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
      val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

      while (isActive) {
        val now = Date()
        _uiState.update {
          it.copy(
            timeString = timeFormat.format(now),
            dateString = dateFormat.format(now),
            dayString = dayFormat.format(now)
          )
        }
        delay(1000L)
      }
    }
  }

  fun updateBatteryStatus() {
    val battery = repository.getBatteryStatus()
    _uiState.update { it.copy(batteryState = battery) }
  }

  fun onSearchQueryChanged(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun launchApp(app: AppModel) {
    if (app.isFolder) { openFolder(app); return }
    val current = restoreRecentApps(_installedApps.value).filterNot { appKey(it) == appKey(app) }
    val updated = (listOf(app) + current).take(12)
    saveRecentApps(updated)
    _uiState.update { it.copy(recentApps = updated) }
    val success = repository.launchApp(app)
    if (!success) {
      _uiState.update { it.copy(feedbackMessage = "Could not open ${app.appName}") }
    }
  }

  private fun refreshQuickToggleState() {
    _uiState.update { it.copy(quickToggles = it.quickToggles.copy(
      isWifiEnabled = repository.getWifiEnabled(),
      isBluetoothEnabled = repository.getBluetoothEnabled(),
      isFlashlightOn = repository.isFlashlightOn(),
      isAirplaneMode = repository.getAirplaneModeEnabled()
    )) }
  }

  fun openSettings() {
    repository.openSystemSettings()
  }

  fun openWifiSettings() {
    repository.openWifiSettings()
  }

  fun openBluetoothSettings() {
    repository.openBluetoothSettings()
  }

  fun openAppInfo(app: AppModel) {
    repository.openAppInfo(app)
  }
  fun uninstallApp(app: AppModel) {
    if (app.isFolder) return
    repository.uninstallApp(app)
  }

  fun removeFolder(folderId: String) {
    val folder = currentFolders().firstOrNull { it.folderId == folderId } ?: return
    val remainingFolders = currentFolders().filterNot { it.folderId == folderId }
    val containedKeys = remainingFolders.flatMap { it.folderAppKeys }.toSet()
    val appsByKey = _installedApps.value.associateBy(::appKey)
    val home = _uiState.value.homeApps.filterNot { it.isFolder && it.folderId == folderId }.toMutableList()
    folder.folderAppKeys.forEach { key ->
      val app = appsByKey[key]
      if (app != null && !containedKeys.contains(key) && !_uiState.value.dockApps.contains(app) && !home.contains(app)) home.add(app)
    }
    _uiState.update { it.copy(homeApps = home, openFolder = null, feedbackMessage = "Folder removed") }
    saveFolders(remainingFolders)
    saveHomeOrder(home)
  }


  fun openDefaultLauncherSettings() {
    repository.openDefaultLauncherSettings()
  }

  // Android does not allow a normal third-party launcher to silently change
  // several protected system toggles. Open the real system panels instead of
  // showing a fake toggle state.
  fun toggleWifi() {
    repository.openWifiSettings()
    viewModelScope.launch { delay(350); refreshQuickToggleState() }
  }
  fun toggleBluetooth() {
    repository.openBluetoothSettings()
    viewModelScope.launch { delay(350); refreshQuickToggleState() }
  }
  fun toggleFlashlight() {
    val success = repository.toggleFlashlight()
    if (success) refreshQuickToggleState()
    else _uiState.update { it.copy(feedbackMessage = "Torch is unavailable on this device") }
  }
  fun toggleAirplane() {
    repository.openSystemSettings()
    _uiState.update { it.copy(feedbackMessage = "Airplane Mode is controlled by Android system settings") }
  }

  fun clearFeedbackMessage() {
    _uiState.update { it.copy(feedbackMessage = null) }
  }

  fun refreshCalendarEvents() {
    if (androidx.core.content.ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
      _uiState.update { it.copy(nextCalendarEvent = null) }
      return
    }
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
      val now = System.currentTimeMillis()
      val end = now + 7L * 24L * 60L * 60L * 1000L
      val events = mutableListOf<CalendarEvent>()
      val projection = arrayOf(
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.ALL_DAY
      )
      try {
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        android.content.ContentUris.appendId(builder, now)
        android.content.ContentUris.appendId(builder, end)
        getApplication<Application>().contentResolver.query(
          builder.build(), projection, null, null, CalendarContract.Instances.BEGIN + " ASC"
        )?.use { c ->
          val titleIdx = c.getColumnIndex(CalendarContract.Instances.TITLE)
          val beginIdx = c.getColumnIndex(CalendarContract.Instances.BEGIN)
          val allDayIdx = c.getColumnIndex(CalendarContract.Instances.ALL_DAY)
          while (c.moveToNext() && events.size < 3) {
            val title = if (titleIdx >= 0) c.getString(titleIdx).orEmpty() else "Calendar event"
            val begin = if (beginIdx >= 0) c.getLong(beginIdx) else now
            val allDay = allDayIdx >= 0 && c.getInt(allDayIdx) != 0
            if (title.isNotBlank()) events += CalendarEvent(title, begin, allDay)
          }
        }
      } catch (_: Exception) {
        // Calendar access can fail on restricted/OEM devices; keep the launcher usable.
      }
      _uiState.update { it.copy(nextCalendarEvent = events.firstOrNull()) }
    }
  }

  fun setWidgetEnabled(id: String, enabled: Boolean) {
    val current = _uiState.value.enabledWidgets.toMutableList()
    if (enabled && !current.contains(id)) current.add(id)
    if (!enabled) current.remove(id)
    val value = current.joinToString(",")
    settingsPrefs.edit().putString("enabled_widgets", value).apply()
    _uiState.update { it.copy(enabledWidgets = current) }
  }

  fun restoreWidgetSettings() {
    val saved = settingsPrefs.getString("enabled_widgets", null)
    if (saved != null) {
      val widgets = saved.split(',').filter { it.isNotBlank() }.distinct()
      _uiState.update { it.copy(enabledWidgets = widgets.ifEmpty { defaultWidgetOrder() }) }
    }
  }

  private fun defaultWidgetOrder(): List<String> = listOf("clock", "weather", "battery", "calendar", "recent")

  fun moveWidget(id: String, direction: Int) {
    val current = _uiState.value.enabledWidgets.toMutableList()
    val index = current.indexOf(id)
    if (index < 0) return
    val target = index + direction
    if (target !in current.indices) return
    val item = current.removeAt(index)
    current.add(target, item)
    settingsPrefs.edit().putString("enabled_widgets", current.joinToString(",")).apply()
    _uiState.update { it.copy(enabledWidgets = current) }
  }

  fun resetWidgetOrder() {
    val order = defaultWidgetOrder()
    settingsPrefs.edit().putString("enabled_widgets", order.joinToString(",")).apply()
    _uiState.update { it.copy(enabledWidgets = order) }
  }

}
