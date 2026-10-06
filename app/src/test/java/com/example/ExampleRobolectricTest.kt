package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PrayerType
import com.example.data.model.TimeMode
import com.example.data.repository.PrayerRepository
import org.junit.Assert.assertEquals
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
        assertEquals("مواقيت الأقصر", appName)
    }

    @Test
    fun `load october prayer times for luxor`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PrayerRepository(context)
        val allDays = repository.getAllDays()

        assertEquals(31, allDays.size)

        // Day 1 check
        val day1 = repository.getDayPrayer(1)
        assertEquals("05:24", day1.fajr)
        assertEquals("06:44", day1.sunrise)
        assertEquals("12:49", day1.dhuhr)
        assertEquals("16:11", day1.asr)
        assertEquals("18:33", day1.sunset)
        assertEquals("18:43", day1.maghrib)
        assertEquals("19:57", day1.isha)
    }

    @Test
    fun `winter time offset subtracts 1 hour`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = PrayerRepository(context)

        // Asr on Day 1 is 16:11 in Summer Time
        val summerTime = repository.computeAdjustedTime("16:11", TimeMode.SUMMER)
        assertEquals("16:11", summerTime.time24)

        // In Winter Time it should be 15:11
        val winterTime = repository.computeAdjustedTime("16:11", TimeMode.WINTER)
        assertEquals("15:11", winterTime.time24)
    }
}
