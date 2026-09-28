package com.example.ui

data class DynamicIslandState(
  val musicTitle: String? = null,
  val musicArtist: String? = null,
  val musicPlaying: Boolean = false,
  val isCharging: Boolean = false,
  val batteryPercent: Int = 0,
  val incomingCall: Boolean = false,
  val callerName: String = "",
  val timerSeconds: Long = 0L,
  val bluetoothConnected: Boolean = false
) {
  val hasTimer: Boolean get() = timerSeconds > 0L
  val hasMusic: Boolean get() = !musicTitle.isNullOrBlank()
  val hasCall: Boolean get() = incomingCall
}
