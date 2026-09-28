package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Lightweight state holder for the Launcher OS 27 in-app lock-screen experience.
 *
 * The host screen owns this controller and renders LockScreenOverlay when
 * [visible] is true. Keeping state outside the overlay makes the feature
 * reusable and prevents the overlay from owning launcher navigation.
 */
class LockScreenController {
  var visible by mutableStateOf(false)
    private set

  fun show() {
    visible = true
  }

  fun dismiss() {
    visible = false
  }

  fun toggle() {
    visible = !visible
  }
}
