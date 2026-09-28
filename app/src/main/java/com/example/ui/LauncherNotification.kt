package com.example.ui

data class LauncherNotification(
  val id: String,
  val appName: String,
  val title: String,
  val message: String,
  val timestamp: Long,
  val packageName: String = ""
)
