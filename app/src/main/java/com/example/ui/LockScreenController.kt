package com.example.ui

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Step 20 controller for the in-app lock screen.
 *
 * The controller owns visibility and the user preference. It does not attempt
 * to control Android's protected system lock screen.
 */
class LockScreenController(context: Context) {
  private val prefs = context.getSharedPreferences("launcher_settings", Context.MODE_PRIVATE)

  private val _visible = MutableStateFlow(
    prefs.getBoolean(KEY_ENABLED, true)
  )
  val visible: StateFlow<Boolean> = _visible.asStateFlow()

  val enabled: Boolean
    get() = prefs.getBoolean(KEY_ENABLED, true)

  fun show() {
    if (enabled) _visible.value = true
  }

  fun dismiss() {
    _visible.value = false
  }

  fun setEnabled(value: Boolean) {
    prefs.edit().putBoolean(KEY_ENABLED, value).apply()
    _visible.value = value
  }

  fun toggle() {
    setEnabled(!enabled)
  }

  companion object {
    private const val KEY_ENABLED = "in_app_lock_screen_enabled"
  }
}
