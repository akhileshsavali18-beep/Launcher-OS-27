package com.example.ui

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DynamicIslandController(context: Context) {
  private val scope = CoroutineScope(Dispatchers.Main.immediate)
  private val _state = MutableStateFlow(DynamicIslandState())
  val state: StateFlow<DynamicIslandState> = _state.asStateFlow()
  private var timerJob: Job? = null

  fun updateSystemState(charging: Boolean, batteryPercent: Int, bluetoothConnected: Boolean) {
    _state.value = _state.value.copy(
      isCharging = charging,
      batteryPercent = batteryPercent,
      bluetoothConnected = bluetoothConnected
    )
  }

  fun setMusic(title: String?, artist: String?, playing: Boolean) {
    _state.value = _state.value.copy(
      musicTitle = title,
      musicArtist = artist,
      musicPlaying = playing
    )
  }

  fun setIncomingCall(callerName: String, active: Boolean) {
    _state.value = _state.value.copy(
      callerName = callerName,
      incomingCall = active
    )
  }

  fun startTimer(seconds: Long) {
    timerJob?.cancel()
    if (seconds <= 0L) {
      _state.value = _state.value.copy(timerSeconds = 0L)
      return
    }
    _state.value = _state.value.copy(timerSeconds = seconds)
    timerJob = scope.launch {
      var remaining = seconds
      while (remaining > 0L) {
        delay(1000L)
        remaining -= 1L
        _state.value = _state.value.copy(timerSeconds = remaining)
      }
    }
  }

  fun stopTimer() {
    timerJob?.cancel()
    timerJob = null
    _state.value = _state.value.copy(timerSeconds = 0L)
  }

  fun clearCall() {
    _state.value = _state.value.copy(incomingCall = false, callerName = "")
  }
}
