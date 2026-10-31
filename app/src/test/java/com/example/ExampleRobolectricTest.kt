package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AdjustedPrayerItem
import com.example.data.model.PrayerType
import com.example.data.model.TimeMode
import com.example.data.repository.PrayerRepository
import com.example.util.PrayerDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("نداء", appName)
    }

    @Test
    fun `load october and november prayer times for luxor`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PrayerRepository(context)

        // October check
        val octDays = repository.getAllDays(10)
        assertEquals(31, octDays.size)
        val octDay1 = repository.getDayPrayer(10, 1)
        assertEquals("05:24", octDay1.fajr)

        // November check (30 days from user image)
        val novDays = repository.getAllDays(11)
        assertEquals(30, novDays.size)
        val novDay1 = repository.getDayPrayer(11, 1)
        assertEquals("04:41", novDay1.fajr)
        assertEquals("06:03", novDay1.sunrise)
        assertEquals("11:46", novDay1.dhuhr)
        assertEquals("14:52", novDay1.asr)
        assertEquals("17:06", novDay1.sunset)
        assertEquals("17:15", novDay1.maghrib)
        assertEquals("18:30", novDay1.isha)
    }

    @Test
    fun `next prayer never selects sunrise or sunset`() {
        val prayers = listOf(
            AdjustedPrayerItem(PrayerType.FAJR, "04:41", "04:41", 4, 41, "04:41 ص"),
            AdjustedPrayerItem(PrayerType.SUNRISE, "06:03", "06:03", 6, 3, "06:03 ص"),
            AdjustedPrayerItem(PrayerType.DHUHR, "11:46", "11:46", 11, 46, "11:46 ص"),
            AdjustedPrayerItem(PrayerType.ASR, "14:52", "14:52", 14, 52, "02:52 م"),
            AdjustedPrayerItem(PrayerType.SUNSET, "17:06", "17:06", 17, 6, "05:06 م"),
            AdjustedPrayerItem(PrayerType.MAGHRIB, "17:15", "17:15", 17, 15, "05:15 م"),
            AdjustedPrayerItem(PrayerType.ISHA, "18:30", "18:30", 18, 30, "06:30 م")
        )

        // At 5:30 AM (before sunrise 6:03, after Fajr 4:41), next prayer should be DHUHR, NOT sunrise!
        val nextAt530 = PrayerDateUtils.findNextPrayer(prayers, 5, 30, 0)
        assertNotNull(nextAt530)
        assertEquals(PrayerType.DHUHR, nextAt530!!.first.type)

        // At 16:00 (before sunset 17:06, after Asr 14:52), next prayer should be MAGHRIB, NOT sunset!
        val nextAt16 = PrayerDateUtils.findNextPrayer(prayers, 16, 0, 0)
        assertNotNull(nextAt16)
        assertEquals(PrayerType.MAGHRIB, nextAt16!!.first.type)
    }

    @Test
    fun `iqama durations match user specifications`() {
        assertEquals(15, PrayerType.FAJR.iqamaMinutes)
        assertEquals(15, PrayerType.DHUHR.iqamaMinutes)
        assertEquals(15, PrayerType.ASR.iqamaMinutes)
        assertEquals(5, PrayerType.MAGHRIB.iqamaMinutes)
        assertEquals(15, PrayerType.ISHA.iqamaMinutes)
    }
}
