package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector

enum class PrayerType(
    val id: String,
    val arabicName: String,
    val isObligatory: Boolean
) {
    FAJR("fajr", "الفجر", true),
    SUNRISE("sunrise", "الشروق", false),
    DHUHR("dhuhr", "الظهر", true),
    ASR("asr", "العصر", true),
    SUNSET("sunset", "الغروب", false),
    MAGHRIB("maghrib", "المغرب", true),
    ISHA("isha", "العشاء", true);

    val icon: ImageVector
        get() = when (this) {
            FAJR -> Icons.Default.Brightness2
            SUNRISE -> Icons.Default.WbSunny
            DHUHR -> Icons.Default.Brightness5
            ASR -> Icons.Default.Brightness6
            SUNSET -> Icons.Default.WbTwilight
            MAGHRIB -> Icons.Default.Brightness7
            ISHA -> Icons.Default.NightsStay
        }

    companion object {
        fun fromId(id: String): PrayerType = entries.find { it.id.equals(id, ignoreCase = true) } ?: FAJR
        val obligatoryPrayers = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
    }
}

enum class TimeMode(val displayName: String, val offsetMinutes: Int) {
    SUMMER("التوقيت الصيفي", 0),
    WINTER("التوقيت الشتوي", -60)
}

data class RawDayPrayer(
    val day: Int,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val sunset: String,
    val maghrib: String,
    val isha: String
) {
    fun getTimeFor(prayer: PrayerType): String = when (prayer) {
        PrayerType.FAJR -> fajr
        PrayerType.SUNRISE -> sunrise
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.SUNSET -> sunset
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
    }
}

data class AdjustedPrayerItem(
    val type: PrayerType,
    val rawTime: String,
    val adjustedTime: String,
    val hour24: Int,
    val minute: Int,
    val formatted12h: String,
    val isNext: Boolean = false,
    val isPassed: Boolean = false,
    val isCurrent: Boolean = false,
    val alarmEnabled: Boolean = false,
    val alarmOffsetMinutes: Int = 0
)

data class PrayerAlarmConfig(
    val prayerType: PrayerType,
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0 // 0 = at prayer time, 5, 10, 15, 20, 30
)

data class SoundConfig(
    val isCustom: Boolean = false,
    val customUriString: String? = null,
    val customFileName: String? = null
)
