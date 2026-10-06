package com.example.data.repository

import android.content.Context
import com.example.data.model.AdjustedPrayerItem
import com.example.data.model.PrayerType
import com.example.data.model.RawDayPrayer
import com.example.data.model.TimeMode
import org.json.JSONObject
import java.util.Locale

class PrayerRepository(private val context: Context) {

    private var cachedDays: List<RawDayPrayer>? = null

    fun getAllDays(): List<RawDayPrayer> {
        cachedDays?.let { return it }
        val list = mutableListOf<RawDayPrayer>()
        try {
            val jsonString = context.assets.open("prayer_times_luxor_october.json")
                .bufferedReader()
                .use { it.readText() }

            val root = JSONObject(jsonString)
            val daysArray = root.getJSONArray("days")
            for (i in 0 until daysArray.length()) {
                val obj = daysArray.getJSONObject(i)
                list.add(
                    RawDayPrayer(
                        day = obj.getInt("day"),
                        fajr = obj.getString("fajr"),
                        sunrise = obj.getString("sunrise"),
                        dhuhr = obj.getString("dhuhr"),
                        asr = obj.getString("asr"),
                        sunset = obj.getString("sunset"),
                        maghrib = obj.getString("maghrib"),
                        isha = obj.getString("isha")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedDays = list
        return list
    }

    fun getDayPrayer(dayNumber: Int): RawDayPrayer {
        val days = getAllDays()
        return days.find { it.day == dayNumber }
            ?: days.firstOrNull()
            ?: RawDayPrayer(1, "05:24", "06:44", "12:49", "16:11", "18:33", "18:43", "19:57")
    }

    /**
     * Applies the timezone mode offset (e.g. -60 min for Winter Time)
     * without modifying the original raw storage.
     */
    fun computeAdjustedTime(rawTime: String, timeMode: TimeMode): AdjustedTimeResult {
        val parts = rawTime.trim().split(":")
        val rawHour = parts[0].toIntOrNull() ?: 0
        val rawMinute = parts[1].toIntOrNull() ?: 0

        var totalMinutes = rawHour * 60 + rawMinute + timeMode.offsetMinutes
        // Wrap around 24 hours if needed
        while (totalMinutes < 0) totalMinutes += 24 * 60
        totalMinutes %= (24 * 60)

        val adjustedHour = totalMinutes / 60
        val adjustedMinute = totalMinutes % 60

        val adjustedTime24 = String.format(Locale.US, "%02d:%02d", adjustedHour, adjustedMinute)

        // 12-hour formatted with Arabic period (ص / م)
        val period = if (adjustedHour < 12) "ص" else "م"
        val hour12 = when {
            adjustedHour == 0 -> 12
            adjustedHour > 12 -> adjustedHour - 12
            else -> adjustedHour
        }
        val formatted12h = String.format(Locale.US, "%02d:%02d %s", hour12, adjustedMinute, period)

        return AdjustedTimeResult(
            hour24 = adjustedHour,
            minute = adjustedMinute,
            time24 = adjustedTime24,
            formatted12h = formatted12h
        )
    }

    fun getAdjustedDayPrayers(
        dayNumber: Int,
        timeMode: TimeMode
    ): List<AdjustedPrayerItem> {
        val raw = getDayPrayer(dayNumber)
        return PrayerType.entries.map { type ->
            val rawTime = raw.getTimeFor(type)
            val adjusted = computeAdjustedTime(rawTime, timeMode)
            AdjustedPrayerItem(
                type = type,
                rawTime = rawTime,
                adjustedTime = adjusted.time24,
                hour24 = adjusted.hour24,
                minute = adjusted.minute,
                formatted12h = adjusted.formatted12h
            )
        }
    }
}

data class AdjustedTimeResult(
    val hour24: Int,
    val minute: Int,
    val time24: String,
    val formatted12h: String
)
