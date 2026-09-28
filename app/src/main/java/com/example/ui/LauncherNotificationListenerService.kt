package com.example.ui

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class LauncherNotificationListenerService : NotificationListenerService() {
  override fun onNotificationPosted(sbn: StatusBarNotification) {
    val extras = sbn.notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
    if (title.isBlank() && text.isBlank()) return

    NotificationCenterController(applicationContext).add(
      LauncherNotification(
        id = sbn.packageName + ":" + sbn.id + ":" + sbn.postTime,
        appName = sbn.packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() },
        title = title,
        message = text,
        timestamp = sbn.postTime,
        packageName = sbn.packageName
      )
    )
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification) {}
}
