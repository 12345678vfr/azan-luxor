package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.PrayerPreferences
import com.example.util.AudioPlayerHelper

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerId = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_ID) ?: "general"
        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "الصلاة"
        val offsetMinutes = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_OFFSET_MINUTES, 0)

        // Strict Deduplication Guard: Ignore triggers for the same prayer within 5 minutes
        val prefs = context.getSharedPreferences("luxor_alarm_dedup", Context.MODE_PRIVATE)
        val dedupKey = "last_trigger_${prayerId}_$offsetMinutes"
        val lastTriggerTime = prefs.getLong(dedupKey, 0L)
        val now = System.currentTimeMillis()

        if (now - lastTriggerTime < 5 * 60 * 1000L) {
            Log.w("PrayerAlarmReceiver", "Duplicate alarm suppressed for $prayerId ($offsetMinutes min)")
            return
        }
        prefs.edit().putLong(dedupKey, now).apply()

        showPrayerNotification(context, prayerName, offsetMinutes)

        // Play the chosen prayer alarm audio (takbeer by default)
        val preferences = PrayerPreferences(context)
        AudioPlayerHelper.playSound(context, preferences.soundConfig)

        // Reschedule alarms for subsequent days
        PrayerAlarmScheduler(context).scheduleAllPrayers()

        // Update home screen widgets immediately
        com.example.widget.PrayerAppWidgetProvider.updateAllWidgets(context)
    }

    private fun showPrayerNotification(context: Context, prayerName: String, offsetMinutes: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

        val channelId = "prayer_times_luxor_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تنبيهات مواقيت الصلاة بالأقصر",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات مواعيد الصلوات والتنبيهات المسبقة لمحافظة الأقصر"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (offsetMinutes > 0) {
            "تنبيه: اقترب موعد صلاة $prayerName"
        } else {
            "حان الآن موعد أذان $prayerName"
        }

        val text = if (offsetMinutes > 0) {
            "متبقي $offsetMinutes دقائق على موعد صلاة $prayerName في محافظة الأقصر"
        } else {
            "الله أكبر.. موعد صلاة $prayerName حسب توقيت محافظة الأقصر"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.luxor_prayer_icon_1790913481063)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notification)
    }
}
