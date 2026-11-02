package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.PrayerType
import com.example.data.preferences.PrayerPreferences
import com.example.data.repository.PrayerRepository
import com.example.util.PrayerDateUtils
import java.util.Calendar
import java.util.Locale

class PrayerAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        var minNextEventMillis = Long.MAX_VALUE
        for (appWidgetId in appWidgetIds) {
            val nextEvent = updateAppWidget(context, appWidgetManager, appWidgetId)
            if (nextEvent in 1 until minNextEventMillis) {
                minNextEventMillis = nextEvent
            }
        }
        scheduleNextWidgetUpdate(context, minNextEventMillis)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_UPDATE_WIDGET ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_BOOT_COMPLETED
        ) {
            updateAllWidgets(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAllWidgets(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelWidgetUpdates(context)
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.ACTION_UPDATE_WIDGET"
        private const val REQUEST_CODE_WIDGET_ALARM = 888

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ): Long {
            val repository = PrayerRepository(context)
            val preferences = PrayerPreferences(context)

            val now = Calendar.getInstance()
            val currentCalMonth = (now.get(Calendar.MONTH) + 1).let { if (it == 11) 11 else 10 }
            val maxDays = if (currentCalMonth == 11) 30 else 31
            val currentCalDay = now.get(Calendar.DAY_OF_MONTH).coerceIn(1, maxDays)
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            val currentSecond = now.get(Calendar.SECOND)

            val monthLabel = if (currentCalMonth == 11) "نوفمبر" else "أكتوبر"
            val dateChipText = "$currentCalDay $monthLabel"

            val todayAdjusted = repository.getAdjustedDayPrayers(
                currentCalMonth,
                currentCalDay,
                preferences.timeMode
            )

            // Check if active in the window between Adhan and Iqama
            val activeIqama = PrayerDateUtils.findActiveIqama(
                todayAdjusted,
                currentHour,
                currentMinute,
                currentSecond
            )

            val nextPair = PrayerDateUtils.findNextPrayer(
                todayAdjusted,
                currentHour,
                currentMinute,
                currentSecond
            )

            val prayerLabel: String
            val prayerName: String
            val prayerTimeFormatted: String
            val countdownTitle: String
            val isIqamaActive: Boolean
            val nextEventEpochMillis: Long

            val views = RemoteViews(context.packageName, R.layout.widget_prayer_next)

            if (activeIqama != null) {
                // ACTIVE IQAMA STATE (Directly following the Adhan)
                isIqamaActive = true
                prayerLabel = "🕌 جارية الآن"
                prayerName = activeIqama.prayer.arabicName
                val currentPrayerItem = todayAdjusted.find { it.type == activeIqama.prayer }
                prayerTimeFormatted = currentPrayerItem?.adjustedTime ?: "--:--"
                countdownTitle = "الأذان:"

                // Row 1: Adhan has already sounded ("تم رفعه")
                views.setViewVisibility(R.id.widget_remaining_time, View.GONE)
                views.setViewVisibility(R.id.widget_remaining_static, View.VISIBLE)
                views.setTextViewText(R.id.widget_remaining_static, "تم رفعه")

                // Row 2: Live Chronometer counting down to Iqama second-by-second
                views.setViewVisibility(R.id.widget_iqama_container, View.GONE)
                views.setViewVisibility(R.id.widget_iqama_active_container, View.VISIBLE)
                views.setTextViewText(R.id.widget_iqama_active_title, "⏳ باقي للإقامة:")

                val remainingMillis = (activeIqama.remainingSeconds * 1000L).coerceAtLeast(0L)
                val baseElapsed = SystemClock.elapsedRealtime() + remainingMillis
                views.setChronometer(R.id.widget_iqama_active_time, baseElapsed, null, true)
                views.setChronometerCountDown(R.id.widget_iqama_active_time, true)

                views.setTextColor(R.id.widget_prayer_label, Color.parseColor("#F59E0B"))

                nextEventEpochMillis = System.currentTimeMillis() + remainingMillis
            } else if (nextPair != null) {
                // NORMAL UPCOMING PRAYER
                isIqamaActive = false
                val nextPrayer = nextPair.first
                val remainingSecs = nextPair.second
                val remainingMillis = (remainingSecs * 1000L).coerceAtLeast(0L)

                prayerLabel = "الصلاة القادمة"
                prayerName = nextPrayer.type.arabicName
                prayerTimeFormatted = nextPrayer.adjustedTime
                countdownTitle = "باقي للأذان:"

                // Row 1: Live Chronometer counting down to Adhan second-by-second
                views.setViewVisibility(R.id.widget_remaining_time, View.VISIBLE)
                views.setViewVisibility(R.id.widget_remaining_static, View.GONE)
                val baseElapsed = SystemClock.elapsedRealtime() + remainingMillis
                views.setChronometer(R.id.widget_remaining_time, baseElapsed, null, true)
                views.setChronometerCountDown(R.id.widget_remaining_time, true)

                // Row 2: Normal upcoming Iqama schedule
                views.setViewVisibility(R.id.widget_iqama_container, View.VISIBLE)
                views.setViewVisibility(R.id.widget_iqama_active_container, View.GONE)

                val iqamaMinuteTotal = nextPrayer.hour24 * 60 + nextPrayer.minute + nextPrayer.type.iqamaMinutes
                val iqamaHour = (iqamaMinuteTotal / 60) % 24
                val iqamaMin = iqamaMinuteTotal % 60
                val iqamaTimeStr = String.format(Locale.US, "%02d:%02d", iqamaHour, iqamaMin)
                views.setTextViewText(R.id.widget_iqama_title, "موعد الإقامة:")
                views.setTextViewText(R.id.widget_iqama_time, "$iqamaTimeStr (${nextPrayer.type.iqamaMinutes} د)")

                views.setTextColor(R.id.widget_prayer_label, Color.parseColor("#80B3A2"))

                nextEventEpochMillis = System.currentTimeMillis() + remainingMillis
            } else {
                // After Isha -> Tomorrow's Fajr
                isIqamaActive = false
                val (tomorrowMonth, tomorrowDay) = if (currentCalDay >= maxDays) {
                    val nextMonth = if (currentCalMonth == 10) 11 else 10
                    Pair(nextMonth, 1)
                } else {
                    Pair(currentCalMonth, currentCalDay + 1)
                }
                val tomorrowAdjusted = repository.getAdjustedDayPrayers(
                    tomorrowMonth,
                    tomorrowDay,
                    preferences.timeMode
                )
                val tomorrowFajr = tomorrowAdjusted.find { it.type == PrayerType.FAJR }

                if (tomorrowFajr != null) {
                    prayerLabel = "صلاة الغد"
                    prayerName = tomorrowFajr.type.arabicName
                    prayerTimeFormatted = tomorrowFajr.adjustedTime
                    countdownTitle = "باقي للأذان:"

                    val currentTotalSec = currentHour * 3600L + currentMinute * 60L + currentSecond
                    val tomorrowFajrTotalSec = (24 * 3600L) + (tomorrowFajr.hour24 * 3600L + tomorrowFajr.minute * 60L)
                    val remainingSecs = (tomorrowFajrTotalSec - currentTotalSec).coerceAtLeast(0L)
                    val remainingMillis = remainingSecs * 1000L

                    // Row 1: Live Chronometer counting down to Fajr second-by-second
                    views.setViewVisibility(R.id.widget_remaining_time, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_remaining_static, View.GONE)
                    val baseElapsed = SystemClock.elapsedRealtime() + remainingMillis
                    views.setChronometer(R.id.widget_remaining_time, baseElapsed, null, true)
                    views.setChronometerCountDown(R.id.widget_remaining_time, true)

                    // Row 2: Normal Iqama schedule for tomorrow's Fajr
                    views.setViewVisibility(R.id.widget_iqama_container, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_iqama_active_container, View.GONE)

                    val iqamaMinuteTotal = tomorrowFajr.hour24 * 60 + tomorrowFajr.minute + tomorrowFajr.type.iqamaMinutes
                    val iqamaHour = (iqamaMinuteTotal / 60) % 24
                    val iqamaMin = iqamaMinuteTotal % 60
                    val iqamaTimeStr = String.format(Locale.US, "%02d:%02d", iqamaHour, iqamaMin)
                    views.setTextViewText(R.id.widget_iqama_title, "موعد الإقامة:")
                    views.setTextViewText(R.id.widget_iqama_time, "$iqamaTimeStr (${tomorrowFajr.type.iqamaMinutes} د)")

                    views.setTextColor(R.id.widget_prayer_label, Color.parseColor("#80B3A2"))

                    // Midnight rollover or Fajr time, whichever is earlier
                    val midnightCal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 1)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val fajrMillis = System.currentTimeMillis() + remainingMillis
                    nextEventEpochMillis = minOf(midnightCal.timeInMillis, fajrMillis)
                } else {
                    prayerLabel = "الصلاة القادمة"
                    prayerName = "الصلاة"
                    prayerTimeFormatted = "--:--"
                    countdownTitle = "باقي للأذان:"
                    views.setViewVisibility(R.id.widget_remaining_time, View.GONE)
                    views.setViewVisibility(R.id.widget_remaining_static, View.VISIBLE)
                    views.setTextViewText(R.id.widget_remaining_static, "--:--")

                    views.setViewVisibility(R.id.widget_iqama_container, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_iqama_active_container, View.GONE)
                    views.setTextViewText(R.id.widget_iqama_title, "موعد الإقامة:")
                    views.setTextViewText(R.id.widget_iqama_time, "--:--")
                    nextEventEpochMillis = System.currentTimeMillis() + 60000L
                }
            }

            views.setTextViewText(R.id.widget_date_chip, dateChipText)
            views.setTextViewText(R.id.widget_prayer_label, prayerLabel)
            views.setTextViewText(R.id.widget_prayer_name, prayerName)
            views.setTextViewText(R.id.widget_prayer_time, prayerTimeFormatted)
            views.setTextViewText(R.id.widget_countdown_title, countdownTitle)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            try {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return nextEventEpochMillis
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, PrayerAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                var minNextEventMillis = Long.MAX_VALUE
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (id in appWidgetIds) {
                        val nextEvent = updateAppWidget(context, appWidgetManager, id)
                        if (nextEvent in 1 until minNextEventMillis) {
                            minNextEventMillis = nextEvent
                        }
                    }
                    scheduleNextWidgetUpdate(context, minNextEventMillis)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun scheduleNextWidgetUpdate(context: Context, nextEventEpochMillis: Long = Long.MAX_VALUE) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // Trigger when next event happens (Adhan, Iqama, or Midnight),
            // or at the top of the next minute (every 60s) for continuous background refresh
            val nextMinuteEpochMillis = ((System.currentTimeMillis() / 60000L) + 1L) * 60000L
            val targetTriggerMillis = minOf(nextEventEpochMillis, nextMinuteEpochMillis)
                .coerceAtLeast(System.currentTimeMillis() + 1000L)

            val intent = Intent(context, PrayerAppWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_WIDGET_ALARM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetTriggerMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            targetTriggerMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetTriggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        targetTriggerMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    targetTriggerMillis,
                    pendingIntent
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancelWidgetUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, PrayerAppWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_WIDGET_ALARM,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
