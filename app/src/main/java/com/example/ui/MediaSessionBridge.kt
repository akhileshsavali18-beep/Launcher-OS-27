package com.example.ui

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager

class MediaSessionBridge(context: Context) {
  private val manager = context.getSystemService(MediaSessionManager::class.java)
  private val component = ComponentName(context, LauncherNotificationListenerService::class.java)
  private var controller: MediaController? = null

  fun refresh(onState: (title: String?, artist: String?, playing: Boolean) -> Unit) {
    val active = runCatching { manager?.getActiveSessions(component).orEmpty() }.getOrDefault(emptyList())
    controller = active.firstOrNull()
    val current = controller
    if (current == null) {
      onState(null, null, false)
      return
    }
    val metadata = current.metadata
    val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
    val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
      ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
    onState(title, artist, current.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING)
  }

  fun togglePlayPause() {
    val current = controller ?: return
    runCatching {
      if (current.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING) {
        current.transportControls.pause()
      } else {
        current.transportControls.play()
      }
    }
  }

  fun next() {
    runCatching { controller?.transportControls?.skipToNext() }
  }

  fun previous() {
    runCatching { controller?.transportControls?.skipToPrevious() }
  }
}
