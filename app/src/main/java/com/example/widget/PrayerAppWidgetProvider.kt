package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PrayerAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.ACTION_UPDATE_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
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
            val remainingFormatted: String
            val iqamaTitle: String
            val iqamaFormatted: String
            val isIqamaActive: Boolean

            if (activeIqama != null) {
                // ACTIVE IQAMA STATE (Directly following the Adhan)
                isIqamaActive = true
                prayerLabel = "🕌 جارية الآن"
                prayerName = activeIqama.prayer.arabicName
                val currentPrayerItem = todayAdjusted.find { it.type == activeIqama.prayer }
                prayerTimeFormatted = currentPrayerItem?.adjustedTime ?: "--:--"
                countdownTitle = "الأذان:"
                remainingFormatted = "تم رفعه"
                iqamaTitle = "⏳ باقي للإقامة:"
                val mins = activeIqama.remainingSeconds / 60
                val secs = activeIqama.remainingSeconds % 60
                iqamaFormatted = String.format(Locale.US, "%02d:%02d د", mins, secs)
            } else if (nextPair != null) {
                // NORMAL UPCOMING PRAYER
                isIqamaActive = false
                val nextPrayer = nextPair.first
                val remainingSecs = nextPair.second

                prayerLabel = "الصلاة القادمة"
                prayerName = nextPrayer.type.arabicName
                prayerTimeFormatted = nextPrayer.adjustedTime

                countdownTitle = "باقي للأذان:"
                val hours = remainingSecs / 3600
                val minutes = (remainingSecs % 3600) / 60
                remainingFormatted = if (hours > 0) {
                    "$hours س $minutes د"
                } else {
                    "$minutes د"
                }

                // Compute Iqama time for this prayer
                val iqamaMinuteTotal = nextPrayer.hour24 * 60 + nextPrayer.minute + nextPrayer.type.iqamaMinutes
                val iqamaHour = (iqamaMinuteTotal / 60) % 24
                val iqamaMin = iqamaMinuteTotal % 60
                val iqamaTimeStr = String.format(Locale.US, "%02d:%02d", iqamaHour, iqamaMin)
                iqamaTitle = "موعد الإقامة:"
                iqamaFormatted = "$iqamaTimeStr (${nextPrayer.type.iqamaMinutes} د)"
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
                    val currentTotalSec = currentHour * 3600L + currentMinute * 60L + currentSecond
                    val tomorrowFajrTotalSec = (24 * 3600L) + (tomorrowFajr.hour24 * 3600L + tomorrowFajr.minute * 60L)
                    val remainingSecs = (tomorrowFajrTotalSec - currentTotalSec).coerceAtLeast(0L)
                    val hours = remainingSecs / 3600
                    val minutes = (remainingSecs % 3600) / 60

                    countdownTitle = "باقي للأذان:"
                    remainingFormatted = if (hours > 0) {
                        "$hours س $minutes د"
                    } else {
                        "$minutes د"
                    }

                    val iqamaMinuteTotal = tomorrowFajr.hour24 * 60 + tomorrowFajr.minute + tomorrowFajr.type.iqamaMinutes
                    val iqamaHour = (iqamaMinuteTotal / 60) % 24
                    val iqamaMin = iqamaMinuteTotal % 60
                    val iqamaTimeStr = String.format(Locale.US, "%02d:%02d", iqamaHour, iqamaMin)
                    iqamaTitle = "موعد الإقامة:"
                    iqamaFormatted = "$iqamaTimeStr (${tomorrowFajr.type.iqamaMinutes} د)"
                } else {
                    prayerLabel = "الصلاة القادمة"
                    prayerName = "الصلاة"
                    prayerTimeFormatted = "--:--"
                    countdownTitle = "باقي للأذان:"
                    remainingFormatted = "--"
                    iqamaTitle = "موعد الإقامة:"
                    iqamaFormatted = "--"
                }
            }

            val views = RemoteViews(context.packageName, R.layout.widget_prayer_next).apply {
                setTextViewText(R.id.widget_date_chip, dateChipText)
                setTextViewText(R.id.widget_prayer_label, prayerLabel)
                setTextViewText(R.id.widget_prayer_name, prayerName)
                setTextViewText(R.id.widget_prayer_time, prayerTimeFormatted)

                setTextViewText(R.id.widget_countdown_title, countdownTitle)
                setTextViewText(R.id.widget_remaining_time, remainingFormatted)

                if (isIqamaActive) {
                    // Show active Iqama container with amber badge, hide normal container
                    setViewVisibility(R.id.widget_iqama_container, android.view.View.GONE)
                    setViewVisibility(R.id.widget_iqama_active_container, android.view.View.VISIBLE)
                    setTextViewText(R.id.widget_iqama_active_title, iqamaTitle)
                    setTextViewText(R.id.widget_iqama_active_time, iqamaFormatted)
                    setTextColor(R.id.widget_prayer_label, Color.parseColor("#F59E0B"))
                } else {
                    // Show standard Iqama container, hide active container
                    setViewVisibility(R.id.widget_iqama_container, android.view.View.VISIBLE)
                    setViewVisibility(R.id.widget_iqama_active_container, android.view.View.GONE)
                    setTextViewText(R.id.widget_iqama_title, iqamaTitle)
                    setTextViewText(R.id.widget_iqama_time, iqamaFormatted)
                    setTextColor(R.id.widget_prayer_label, Color.parseColor("#80B3A2"))
                }

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            try {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, PrayerAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (id in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
