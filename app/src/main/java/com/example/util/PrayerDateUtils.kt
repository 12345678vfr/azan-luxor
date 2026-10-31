package com.example.util

import android.os.Build
import com.example.data.model.AdjustedPrayerItem
import com.example.data.model.IqamaState
import com.example.data.model.PrayerType
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.util.Calendar
import java.util.Locale

object PrayerDateUtils {

    fun getDayOfWeekArabic(calendar: Calendar = Calendar.getInstance()): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "السبت"
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الإثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            else -> ""
        }
    }

    fun getGregorianDateArabic(calendar: Calendar = Calendar.getInstance()): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        val monthName = when (month) {
            1 -> "يناير"
            2 -> "فبراير"
            3 -> "مارس"
            4 -> "أبريل"
            5 -> "مايو"
            6 -> "يونيو"
            7 -> "يوليو"
            8 -> "أغسطس"
            9 -> "سبتمبر"
            10 -> "أكتوبر"
            11 -> "نوفمبر"
            12 -> "ديسمبر"
            else -> ""
        }
        return "$day $monthName $year م"
    }

    fun getHijriDateArabic(calendar: Calendar = Calendar.getInstance()): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val localDate = LocalDate.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                val hijrahDate = HijrahChronology.INSTANCE.date(localDate)
                val hDay = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                val hMonth = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
                val hYear = hijrahDate.get(java.time.temporal.ChronoField.YEAR)

                val hMonthName = when (hMonth) {
                    1 -> "محرم"
                    2 -> "صفر"
                    3 -> "ربيع الأول"
                    4 -> "ربيع الآخر"
                    5 -> "جمادى الأولى"
                    6 -> "جمادى الآخرة"
                    7 -> "رجب"
                    8 -> "شعبان"
                    9 -> "رمضان"
                    10 -> "شوال"
                    11 -> "ذو القعدة"
                    12 -> "ذو الحجة"
                    else -> ""
                }
                "$hDay $hMonthName $hYear هـ"
            } catch (e: Exception) {
                fallbackHijri(calendar)
            }
        } else {
            fallbackHijri(calendar)
        }
    }

    private fun fallbackHijri(calendar: Calendar): String {
        return "جمادى الأولى ١٤٤٨ هـ"
    }

    fun formatSecondsToCountdown(seconds: Long): String {
        if (seconds <= 0) return "00:00:00"
        val hours = seconds / 3600
        val remainder = seconds % 3600
        val minutes = remainder / 60
        val secs = remainder % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
    }

    fun formatSecondsToMinutesSeconds(seconds: Long): String {
        if (seconds <= 0) return "00:00"
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, secs)
    }

    /**
     * Finds the next upcoming obligatory prayer from today's list given the current hour, minute, second.
     * Excludes Sunrise (الشروق) and Sunset (الغروب) completely as they are not prayers.
     */
    fun findNextPrayer(
        todayPrayers: List<AdjustedPrayerItem>,
        currentHour: Int,
        currentMinute: Int,
        currentSecond: Int
    ): Pair<AdjustedPrayerItem, Long>? {
        val currentTotalSeconds = currentHour * 3600L + currentMinute * 60L + currentSecond

        for (item in todayPrayers) {
            // Only the 5 obligatory prayers (Fajr, Dhuhr, Asr, Maghrib, Isha)
            if (!item.type.isObligatory) continue

            val prayerTotalSeconds = item.hour24 * 3600L + item.minute * 60L
            if (prayerTotalSeconds > currentTotalSeconds) {
                val diff = prayerTotalSeconds - currentTotalSeconds
                return Pair(item, diff)
            }
        }
        return null
    }

    /**
     * Computes whether an Iqama countdown is active (between Adhan start and Iqama duration).
     * Fajr: 15m, Dhuhr: 15m, Asr: 15m, Maghrib: 5m, Isha: 15m.
     */
    fun findActiveIqama(
        todayPrayers: List<AdjustedPrayerItem>,
        currentHour: Int,
        currentMinute: Int,
        currentSecond: Int
    ): IqamaState? {
        val currentTotalSeconds = currentHour * 3600L + currentMinute * 60L + currentSecond

        for (item in todayPrayers) {
            if (!item.type.isObligatory) continue
            val prayerSeconds = item.hour24 * 3600L + item.minute * 60L
            val iqamaWindowSeconds = item.type.iqamaMinutes * 60L
            val iqamaEndSeconds = prayerSeconds + iqamaWindowSeconds

            if (currentTotalSeconds >= prayerSeconds && currentTotalSeconds < iqamaEndSeconds) {
                val remaining = iqamaEndSeconds - currentTotalSeconds
                return IqamaState(
                    prayer = item.type,
                    remainingSeconds = remaining,
                    formattedRemaining = formatSecondsToMinutesSeconds(remaining),
                    totalIqamaMinutes = item.type.iqamaMinutes
                )
            }
        }
        return null
    }
}
