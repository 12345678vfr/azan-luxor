package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.PrayerType
import com.example.data.preferences.PrayerPreferences
import com.example.data.repository.PrayerRepository
import java.util.Calendar

class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val repository = PrayerRepository(context)
    private val preferences = PrayerPreferences(context)

    fun scheduleAllPrayers() {
        if (alarmManager == null) return

        val timeMode = preferences.timeMode
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_MONTH).coerceIn(1, 31)

        val todayAdjusted = repository.getAdjustedDayPrayers(currentDay, timeMode)
        val tomorrowDay = if (currentDay >= 31) 1 else currentDay + 1
        val tomorrowAdjusted = repository.getAdjustedDayPrayers(tomorrowDay, timeMode)

        for (prayerType in PrayerType.obligatoryPrayers) {
            val config = preferences.getAlarmConfig(prayerType)
            if (!config.enabled) {
                cancelAlarm(prayerType)
                continue
            }

            val todayItem = todayAdjusted.find { it.type == prayerType } ?: continue
            val alarmCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, todayItem.hour24)
                set(Calendar.MINUTE, todayItem.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Subtract user offset (e.g. 10 minutes before)
                add(Calendar.MINUTE, -config.offsetMinutes)
            }

            // If time already passed for today, schedule for tomorrow
            val targetCal = if (alarmCal.timeInMillis <= now.timeInMillis) {
                val tomorrowItem = tomorrowAdjusted.find { it.type == prayerType } ?: todayItem
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, tomorrowItem.hour24)
                    set(Calendar.MINUTE, tomorrowItem.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MINUTE, -config.offsetMinutes)
                }
            } else {
                alarmCal
            }

            scheduleAlarm(prayerType, targetCal.timeInMillis, config.offsetMinutes)
        }
    }

    private fun scheduleAlarm(prayerType: PrayerType, triggerAtMillis: Long, offsetMinutes: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_ID, prayerType.id)
            putExtra(EXTRA_PRAYER_NAME, prayerType.arabicName)
            putExtra(EXTRA_OFFSET_MINUTES, offsetMinutes)
        }

        val requestCode = getRequestCode(prayerType)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let { am ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (am.canScheduleExactAlarms()) {
                        am.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    } else {
                        am.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerAtMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    am.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.w("PrayerScheduler", "Cannot schedule exact alarm: ${e.message}")
            }
        }
    }

    fun cancelAlarm(prayerType: PrayerType) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
        }
        val requestCode = getRequestCode(prayerType)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun getRequestCode(prayerType: PrayerType): Int {
        return when (prayerType) {
            PrayerType.FAJR -> 101
            PrayerType.SUNRISE -> 102
            PrayerType.DHUHR -> 103
            PrayerType.ASR -> 104
            PrayerType.SUNSET -> 105
            PrayerType.MAGHRIB -> 106
            PrayerType.ISHA -> 107
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_OFFSET_MINUTES = "extra_offset_minutes"
    }
}
