package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdjustedPrayerItem
import com.example.data.model.PrayerAlarmConfig
import com.example.data.model.PrayerType
import com.example.data.model.RawDayPrayer
import com.example.data.model.SoundConfig
import com.example.data.model.TimeMode
import com.example.data.preferences.PrayerPreferences
import com.example.data.repository.PrayerRepository
import com.example.service.PrayerAlarmScheduler
import com.example.util.AudioPlayerHelper
import com.example.util.PrayerDateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class MainUiState(
    val selectedDay: Int = 1,
    val timeMode: TimeMode = TimeMode.SUMMER,
    val dayPrayers: List<AdjustedPrayerItem> = emptyList(),
    val nextPrayer: AdjustedPrayerItem? = null,
    val remainingSeconds: Long = 0L,
    val remainingTimeFormatted: String = "00:00:00",
    val dayOfWeekArabic: String = "",
    val gregorianDateArabic: String = "",
    val hijriDateArabic: String = "",
    val allMonthDays: List<RawDayPrayer> = emptyList(),
    val alarmConfigs: Map<PrayerType, PrayerAlarmConfig> = emptyMap(),
    val soundConfig: SoundConfig = SoundConfig(),
    val isTestingSound: Boolean = false,
    val isTodayView: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerRepository(application)
    private val preferences = PrayerPreferences(application)
    private val scheduler = PrayerAlarmScheduler(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_MONTH).coerceIn(1, 31)
        val initialTimeMode = preferences.timeMode
        val allDays = repository.getAllDays()

        val initialAlarmConfigs = PrayerType.obligatoryPrayers.associateWith {
            preferences.getAlarmConfig(it)
        }
        val initialSound = preferences.soundConfig

        _uiState.update {
            it.copy(
                selectedDay = currentDay,
                timeMode = initialTimeMode,
                allMonthDays = allDays,
                alarmConfigs = initialAlarmConfigs,
                soundConfig = initialSound,
                dayOfWeekArabic = PrayerDateUtils.getDayOfWeekArabic(now),
                gregorianDateArabic = PrayerDateUtils.getGregorianDateArabic(now),
                hijriDateArabic = PrayerDateUtils.getHijriDateArabic(now)
            )
        }

        refreshPrayerTimes()
        scheduler.scheduleAllPrayers()
        startCountdownTicker()
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (isActive) {
                updateCountdown()
                delay(1000L)
            }
        }
    }

    private fun updateCountdown() {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentSecond = now.get(Calendar.SECOND)
        val currentDay = now.get(Calendar.DAY_OF_MONTH).coerceIn(1, 31)

        val state = _uiState.value
        val todayAdjusted = repository.getAdjustedDayPrayers(currentDay, state.timeMode)

        val nextPair = PrayerDateUtils.findNextPrayer(
            todayAdjusted,
            currentHour,
            currentMinute,
            currentSecond
        )

        val nextPrayerItem: AdjustedPrayerItem?
        val remainingSecs: Long

        if (nextPair != null) {
            nextPrayerItem = nextPair.first
            remainingSecs = nextPair.second
        } else {
            // After Isha: next prayer is tomorrow's Fajr
            val tomorrowDay = if (currentDay >= 31) 1 else currentDay + 1
            val tomorrowAdjusted = repository.getAdjustedDayPrayers(tomorrowDay, state.timeMode)
            val tomorrowFajr = tomorrowAdjusted.find { it.type == PrayerType.FAJR }

            if (tomorrowFajr != null) {
                val currentTotalSec = currentHour * 3600L + currentMinute * 60L + currentSecond
                val tomorrowFajrTotalSec = (24 * 3600L) + (tomorrowFajr.hour24 * 3600L + tomorrowFajr.minute * 60L)
                remainingSecs = (tomorrowFajrTotalSec - currentTotalSec).coerceAtLeast(0L)
                nextPrayerItem = tomorrowFajr
            } else {
                remainingSecs = 0L
                nextPrayerItem = null
            }
        }

        // Highlight next prayer in current day prayers
        val updatedPrayers = state.dayPrayers.map { item ->
            val isNext = (state.selectedDay == currentDay && nextPrayerItem?.type == item.type)
            val currentTotalSec = currentHour * 3600L + currentMinute * 60L + currentSecond
            val itemTotalSec = item.hour24 * 3600L + item.minute * 60L
            val isPassed = (state.selectedDay < currentDay) || (state.selectedDay == currentDay && itemTotalSec <= currentTotalSec)
            item.copy(isNext = isNext, isPassed = isPassed)
        }

        _uiState.update {
            it.copy(
                nextPrayer = nextPrayerItem,
                remainingSeconds = remainingSecs,
                remainingTimeFormatted = PrayerDateUtils.formatSecondsToCountdown(remainingSecs),
                dayPrayers = updatedPrayers,
                isTestingSound = AudioPlayerHelper.isPlaying()
            )
        }
    }

    fun selectDay(day: Int) {
        val bounded = day.coerceIn(1, 31)
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        _uiState.update {
            it.copy(
                selectedDay = bounded,
                isTodayView = (bounded == currentDay)
            )
        }
        refreshPrayerTimes()
    }

    fun setTimeMode(mode: TimeMode) {
        preferences.timeMode = mode
        _uiState.update { it.copy(timeMode = mode) }
        refreshPrayerTimes()
        scheduler.scheduleAllPrayers()
    }

    fun updateAlarmConfig(prayer: PrayerType, enabled: Boolean, offsetMinutes: Int) {
        preferences.setAlarmConfig(prayer, enabled, offsetMinutes)
        val updatedMap = _uiState.value.alarmConfigs.toMutableMap().apply {
            put(prayer, PrayerAlarmConfig(prayer, enabled, offsetMinutes))
        }
        _uiState.update { it.copy(alarmConfigs = updatedMap) }
        refreshPrayerTimes()
        scheduler.scheduleAllPrayers()
    }

    fun setCustomSound(uriString: String, fileName: String?) {
        val newConfig = SoundConfig(isCustom = true, customUriString = uriString, customFileName = fileName)
        preferences.soundConfig = newConfig
        _uiState.update { it.copy(soundConfig = newConfig) }
    }

    fun setDefaultSound() {
        val newConfig = SoundConfig(isCustom = false, customUriString = null, customFileName = null)
        preferences.soundConfig = newConfig
        _uiState.update { it.copy(soundConfig = newConfig) }
    }

    fun startSoundTest() {
        _uiState.update { it.copy(isTestingSound = true) }
        AudioPlayerHelper.playSound(getApplication(), _uiState.value.soundConfig) {
            _uiState.update { it.copy(isTestingSound = false) }
        }
    }

    fun stopSoundTest() {
        AudioPlayerHelper.stopSound()
        _uiState.update { it.copy(isTestingSound = false) }
    }

    private fun refreshPrayerTimes() {
        val state = _uiState.value
        val rawAdjusted = repository.getAdjustedDayPrayers(state.selectedDay, state.timeMode)

        val mergedPrayers = rawAdjusted.map { item ->
            val alarm = state.alarmConfigs[item.type]
            item.copy(
                alarmEnabled = alarm?.enabled ?: false,
                alarmOffsetMinutes = alarm?.offsetMinutes ?: 0
            )
        }

        _uiState.update { it.copy(dayPrayers = mergedPrayers) }
        updateCountdown()
    }

    override fun onCleared() {
        super.onCleared()
        AudioPlayerHelper.stopSound()
    }
}
