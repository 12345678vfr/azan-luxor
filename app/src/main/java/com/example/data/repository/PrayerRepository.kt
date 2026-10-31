package com.example.data.repository

import android.content.Context
import com.example.data.model.AdjustedPrayerItem
import com.example.data.model.PrayerType
import com.example.data.model.RawDayPrayer
import com.example.data.model.TimeMode
import org.json.JSONObject
import java.util.Locale

data class MonthSchedule(
    val monthNumber: Int,
    val monthName: String,
    val baseTimezone: String,
    val days: List<RawDayPrayer>
)

class PrayerRepository(private val context: Context) {

    private val cachedMonthSchedules = mutableMapOf<Int, MonthSchedule>()

    fun getSupportedMonths(): List<Pair<Int, String>> = listOf(
        10 to "أكتوبر",
        11 to "نوفمبر"
    )

    fun getMonthSchedule(monthNumber: Int): MonthSchedule {
        val targetMonth = if (monthNumber == 11) 11 else 10
        cachedMonthSchedules[targetMonth]?.let { return it }

        val assetFile = if (targetMonth == 11) {
            "prayer_times_luxor_november.json"
        } else {
            "prayer_times_luxor_october.json"
        }

        val days = mutableListOf<RawDayPrayer>()
        var monthName = if (targetMonth == 11) "نوفمبر" else "أكتوبر"
        var baseTimezone = if (targetMonth == 11) "Winter" else "Summer"

        try {
            val jsonString = context.assets.open(assetFile)
                .bufferedReader()
                .use { it.readText() }

            val root = JSONObject(jsonString)
            monthName = root.optString("month_name", monthName)
            baseTimezone = root.optString("base_timezone", baseTimezone)
            val daysArray = root.getJSONArray("days")

            for (i in 0 until daysArray.length()) {
                val obj = daysArray.getJSONObject(i)
                days.add(
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

        val schedule = MonthSchedule(targetMonth, monthName, baseTimezone, days)
        cachedMonthSchedules[targetMonth] = schedule
        return schedule
    }

    fun getAllDays(monthNumber: Int): List<RawDayPrayer> {
        return getMonthSchedule(monthNumber).days
    }

    fun getDayPrayer(monthNumber: Int, dayNumber: Int): RawDayPrayer {
        val days = getAllDays(monthNumber)
        return days.find { it.day == dayNumber }
            ?: days.firstOrNull()
            ?: RawDayPrayer(1, "04:41", "06:03", "11:46", "14:52", "17:06", "17:15", "18:30")
    }

    /**
     * Applies the timezone mode offset without modifying the original raw storage.
     * If base data is Summer:
     *   - Summer mode: offset = 0
     *   - Winter mode: offset = -60 min
     * If base data is Winter:
     *   - Winter mode: offset = 0
     *   - Summer mode: offset = +60 min
     */
    fun computeAdjustedTime(rawTime: String, timeMode: TimeMode, baseTimezone: String = "Summer"): AdjustedTimeResult {
        val parts = rawTime.trim().split(":")
        val rawHour = parts[0].toIntOrNull() ?: 0
        val rawMinute = parts[1].toIntOrNull() ?: 0

        val effectiveOffset = if (baseTimezone.equals("Winter", ignoreCase = true)) {
            if (timeMode == TimeMode.SUMMER) 60 else 0
        } else {
            if (timeMode == TimeMode.WINTER) -60 else 0
        }

        var totalMinutes = rawHour * 60 + rawMinute + effectiveOffset
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
        monthNumber: Int,
        dayNumber: Int,
        timeMode: TimeMode
    ): List<AdjustedPrayerItem> {
        val schedule = getMonthSchedule(monthNumber)
        val raw = getDayPrayer(monthNumber, dayNumber)
        return PrayerType.entries.map { type ->
            val rawTime = raw.getTimeFor(type)
            val adjusted = computeAdjustedTime(rawTime, timeMode, schedule.baseTimezone)
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
