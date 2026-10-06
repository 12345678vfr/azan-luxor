package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.R
import com.example.data.model.SoundConfig

object AudioPlayerHelper {

    private var activePlayer: MediaPlayer? = null

    fun playSound(context: Context, soundConfig: SoundConfig, onComplete: () -> Unit = {}) {
        stopSound()
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
                    setDataSource(context, uri)
                    prepare()
                }
            } else {
                MediaPlayer.create(context, R.raw.adhan_tone)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                }
            }

            if (player != null) {
                activePlayer = player
                player.setOnCompletionListener {
                    stopSound()
                    onComplete()
                }
                player.start()
            } else {
                onComplete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to bundled sound
            try {
                val fallback = MediaPlayer.create(context, R.raw.adhan_tone)
                activePlayer = fallback
                fallback?.setOnCompletionListener {
                    stopSound()
                    onComplete()
                }
                fallback?.start()
            } catch (ex: Exception) {
                ex.printStackTrace()
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
        }
    }

    fun isPlaying(): Boolean = activePlayer?.isPlaying == true
}
