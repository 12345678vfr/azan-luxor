package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import com.example.R
import com.example.data.model.SoundConfig

object AudioPlayerHelper {

    private var activePlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private fun acquireWakeLock(context: Context) {
        try {
            if (wakeLock == null) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "LuxorPrayer:AdhanAudioWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            wakeLock?.acquire(3 * 60 * 1000L) // 3 minutes max safety timeout
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(context: Context, soundConfig: SoundConfig, onComplete: () -> Unit = {}) {
        stopSound()
        acquireWakeLock(context)

        try {
            val player = if (soundConfig.isCustom && !soundConfig.customUriString.isNullOrEmpty()) {
                val uri = Uri.parse(soundConfig.customUriString)
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                    setDataSource(context, uri)
                    prepare()
                }
            } else if (soundConfig.soundType == SoundConfig.SOUND_TYPE_MADINAH) {
                MediaPlayer.create(context, R.raw.adhan_madinah)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                }
            } else {
                // Default: Takbeer
                MediaPlayer.create(context, R.raw.takbeer)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                }
            }

            if (player != null) {
                activePlayer = player
                player.setOnCompletionListener {
                    stopSound()
                    onComplete()
                }
                player.setOnErrorListener { _, _, _ ->
                    stopSound()
                    onComplete()
                    true
                }
                player.start()
            } else {
                releaseWakeLock()
                onComplete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to takbeer sound
            try {
                val fallback = MediaPlayer.create(context, R.raw.takbeer)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                }
                activePlayer = fallback
                fallback?.setOnCompletionListener {
                    stopSound()
                    onComplete()
                }
                fallback?.setOnErrorListener { _, _, _ ->
                    stopSound()
                    onComplete()
                    true
                }
                fallback?.start()
            } catch (ex: Exception) {
                ex.printStackTrace()
                releaseWakeLock()
                onComplete()
            }
        }
    }

    fun stopSound() {
        try {
            activePlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            activePlayer = null
            releaseWakeLock()
        }
    }

    fun isPlaying(): Boolean = activePlayer?.isPlaying == true
}
