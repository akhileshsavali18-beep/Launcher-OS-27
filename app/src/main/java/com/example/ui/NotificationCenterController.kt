package com.example.ui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class NotificationCenterController(context: Context) {
  private val prefs = context.getSharedPreferences("launcher_notifications", Context.MODE_PRIVATE)
  private val _notifications = MutableStateFlow(load())
  val notifications: StateFlow<List<LauncherNotification>> = _notifications.asStateFlow()

  fun add(notification: LauncherNotification) {
    val updated = (listOf(notification) + _notifications.value.filterNot { it.id == notification.id }).take(50)
    _notifications.value = updated
    save(updated)
  }

  fun remove(id: String) {
    val updated = _notifications.value.filterNot { it.id == id }
    _notifications.value = updated
    save(updated)
  }

  fun clearAll() {
    _notifications.value = emptyList()
    prefs.edit().remove(KEY).apply()
  }

  private fun load(): List<LauncherNotification> = runCatching {
    val array = JSONArray(prefs.getString(KEY, "[]"))
    buildList {
      for (i in 0 until array.length()) {
        val o = array.getJSONObject(i)
        add(LauncherNotification(o.optString("id"), o.optString("appName"), o.optString("title"), o.optString("message"), o.optLong("timestamp"), o.optString("packageName")))
      }
    }
  }.getOrDefault(emptyList())

  private fun save(items: List<LauncherNotification>) {
    val array = JSONArray()
    items.forEach {
      array.put(JSONObject().apply {
        put("id", it.id); put("appName", it.appName); put("title", it.title)
        put("message", it.message); put("timestamp", it.timestamp); put("packageName", it.packageName)
      })
    }
    prefs.edit().putString(KEY, array.toString()).apply()
  }

  companion object { private const val KEY = "items" }
}
