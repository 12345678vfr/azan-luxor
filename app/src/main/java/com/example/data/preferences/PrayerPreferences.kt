package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppThemePalette
import com.example.data.model.PrayerAlarmConfig
import com.example.data.model.PrayerType
import com.example.data.model.SoundConfig
import com.example.data.model.TimeMode

class PrayerPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("luxor_prayer_prefs", Context.MODE_PRIVATE)

    var timeMode: TimeMode
        get() {
            val name = prefs.getString(KEY_TIME_MODE, TimeMode.WINTER.name)
            return try {
                TimeMode.valueOf(name ?: TimeMode.WINTER.name)
            } catch (e: Exception) {
                TimeMode.WINTER
            }
        }
        set(value) {
            prefs.edit().putString(KEY_TIME_MODE, value.name).apply()
        }

    var appTheme: AppThemePalette
        get() {
            val id = prefs.getString(KEY_APP_THEME, AppThemePalette.EMERALD.id) ?: AppThemePalette.EMERALD.id
            return AppThemePalette.fromId(id)
        }
        set(value) {
            prefs.edit().putString(KEY_APP_THEME, value.id).apply()
        }

    fun getAlarmConfig(prayer: PrayerType): PrayerAlarmConfig {
        val enabledKey = "alarm_enabled_${prayer.id}"
        val offsetKey = "alarm_offset_${prayer.id}"

        val defaultOffset = when (prayer) {
            PrayerType.FAJR -> 10
            PrayerType.DHUHR -> 5
            PrayerType.ASR -> 10
            PrayerType.MAGHRIB -> 10
            PrayerType.ISHA -> 15
            else -> 0
        }

        val enabled = prefs.getBoolean(enabledKey, true)
        val offset = prefs.getInt(offsetKey, defaultOffset)
        return PrayerAlarmConfig(prayer, enabled, offset)
    }

    fun setAlarmConfig(prayer: PrayerType, enabled: Boolean, offsetMinutes: Int) {
        prefs.edit()
            .putBoolean("alarm_enabled_${prayer.id}", enabled)
            .putInt("alarm_offset_${prayer.id}", offsetMinutes)
            .apply()
    }

    var soundConfig: SoundConfig
        get() {
            val isCustom = prefs.getBoolean(KEY_IS_CUSTOM_SOUND, false)
            val uri = prefs.getString(KEY_CUSTOM_SOUND_URI, null)
            val name = prefs.getString(KEY_CUSTOM_SOUND_NAME, null)
            return SoundConfig(isCustom, uri, name)
        }
        set(value) {
            prefs.edit()
                .putBoolean(KEY_IS_CUSTOM_SOUND, value.isCustom)
                .putString(KEY_CUSTOM_SOUND_URI, value.customUriString)
                .putString(KEY_CUSTOM_SOUND_NAME, value.customFileName)
                .apply()
        }

    companion object {
        private const val KEY_TIME_MODE = "time_mode"
        private const val KEY_APP_THEME = "app_theme_palette"
        private const val KEY_IS_CUSTOM_SOUND = "is_custom_sound"
        private const val KEY_CUSTOM_SOUND_URI = "custom_sound_uri"
        private const val KEY_CUSTOM_SOUND_NAME = "custom_sound_name"
    }
}
