package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdjustedPrayerItem
import com.example.data.model.AppThemePalette
import com.example.data.model.IqamaState
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
    val selectedMonth: Int = 10,
    val selectedDay: Int = 1,
    val timeMode: TimeMode = TimeMode.WINTER,
    val appPalette: AppThemePalette = AppThemePalette.EMERALD,
    val dayPrayers: List<AdjustedPrayerItem> = emptyList(),
    val nextPrayer: AdjustedPrayerItem? = null,
    val iqamaState: IqamaState? = null,
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
        val currentMonth = (now.get(Calendar.MONTH) + 1).let { if (it == 11) 11 else 10 }
        val maxDays = if (currentMonth == 11) 30 else 31
        val currentDay = now.get(Calendar.DAY_OF_MONTH).coerceIn(1, maxDays)

        val initialTimeMode = preferences.timeMode
        val initialPalette = preferences.appTheme
        val allDays = repository.getAllDays(currentMonth)

        val initialAlarmConfigs = PrayerType.obligatoryPrayers.associateWith {
            preferences.getAlarmConfig(it)
        }
        val initialSound = preferences.soundConfig

        _uiState.update {
            it.copy(
                selectedMonth = currentMonth,
                selectedDay = currentDay,
                timeMode = initialTimeMode,
                appPalette = initialPalette,
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

        val currentCalMonth = (now.get(Calendar.MONTH) + 1).let { if (it == 11) 11 else 10 }
        val maxDays = if (currentCalMonth == 11) 30 else 31
        val currentCalDay = now.get(Calendar.DAY_OF_MONTH).coerceIn(1, maxDays)

        val state = _uiState.value
        val todayAdjusted = repository.getAdjustedDayPrayers(currentCalMonth, currentCalDay, state.timeMode)

        // Find next obligatory prayer (Fajr, Dhuhr, Asr, Maghrib, Isha)
        val nextPair = PrayerDateUtils.findNextPrayer(
            todayAdjusted,
            currentHour,
            currentMinute,
            currentSecond
        )

        // Find active Iqama if right now is within the prayer's iqama window
        val activeIqama = PrayerDateUtils.findActiveIqama(
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
            val (tomorrowMonth, tomorrowDay) = if (currentCalDay >= maxDays) {
                val nextMonth = if (currentCalMonth == 10) 11 else 10
                Pair(nextMonth, 1)
            } else {
                Pair(currentCalMonth, currentCalDay + 1)
            }

            val tomorrowAdjusted = repository.getAdjustedDayPrayers(tomorrowMonth, tomorrowDay, state.timeMode)
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
        val isViewingToday = (state.selectedMonth == currentCalMonth && state.selectedDay == currentCalDay)
        val updatedPrayers = state.dayPrayers.map { item ->
            val isNext = (isViewingToday && nextPrayerItem?.type == item.type)
            val currentTotalSec = currentHour * 3600L + currentMinute * 60L + currentSecond
            val itemTotalSec = item.hour24 * 3600L + item.minute * 60L
            val isPassed = (state.selectedMonth < currentCalMonth) ||
                    (state.selectedMonth == currentCalMonth && state.selectedDay < currentCalDay) ||
                    (isViewingToday && itemTotalSec <= currentTotalSec)
            item.copy(isNext = isNext, isPassed = isPassed)
        }

        _uiState.update {
            it.copy(
                nextPrayer = nextPrayerItem,
                iqamaState = activeIqama,
                remainingSeconds = remainingSecs,
                remainingTimeFormatted = PrayerDateUtils.formatSecondsToCountdown(remainingSecs),
                dayPrayers = updatedPrayers,
                isTestingSound = AudioPlayerHelper.isPlaying()
            )
        }
    }

    fun selectMonth(month: Int) {
        val targetMonth = if (month == 11) 11 else 10
        val maxDays = if (targetMonth == 11) 30 else 31
        val newDay = _uiState.value.selectedDay.coerceIn(1, maxDays)
        val allDays = repository.getAllDays(targetMonth)

        val now = Calendar.getInstance()
        val currentCalMonth = now.get(Calendar.MONTH) + 1
        val currentCalDay = now.get(Calendar.DAY_OF_MONTH)

        _uiState.update {
            it.copy(
                selectedMonth = targetMonth,
                selectedDay = newDay,
                allMonthDays = allDays,
                isTodayView = (targetMonth == currentCalMonth && newDay == currentCalDay)
            )
        }
        refreshPrayerTimes()
    }

    fun selectDay(day: Int) {
        val maxDays = if (_uiState.value.selectedMonth == 11) 30 else 31
        val bounded = day.coerceIn(1, maxDays)
        val now = Calendar.getInstance()
        val currentCalMonth = now.get(Calendar.MONTH) + 1
        val currentCalDay = now.get(Calendar.DAY_OF_MONTH)

        _uiState.update {
            it.copy(
                selectedDay = bounded,
                isTodayView = (_uiState.value.selectedMonth == currentCalMonth && bounded == currentCalDay)
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

    fun setAppPalette(palette: AppThemePalette) {
        preferences.appTheme = palette
        _uiState.update { it.copy(appPalette = palette) }
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

    fun setSoundType(soundType: String) {
        val newConfig = SoundConfig(soundType = soundType, isCustom = false, customUriString = null, customFileName = null)
        preferences.soundConfig = newConfig
        _uiState.update { it.copy(soundConfig = newConfig) }
    }

    fun setCustomSound(uriString: String, fileName: String?) {
        val newConfig = SoundConfig(
            soundType = SoundConfig.SOUND_TYPE_CUSTOM,
            isCustom = true,
            customUriString = uriString,
            customFileName = fileName
        )
        preferences.soundConfig = newConfig
        _uiState.update { it.copy(soundConfig = newConfig) }
    }

    fun setDefaultSound() {
        setSoundType(SoundConfig.SOUND_TYPE_TAKBEER)
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
        val rawAdjusted = repository.getAdjustedDayPrayers(state.selectedMonth, state.selectedDay, state.timeMode)

        val mergedPrayers = rawAdjusted.map { item ->
            val alarm = state.alarmConfigs[item.type]
            item.copy(
                alarmEnabled = alarm?.enabled ?: false,
                alarmOffsetMinutes = alarm?.offsetMinutes ?: 0
            )
        }

        _uiState.update { it.copy(dayPrayers = mergedPrayers) }
        updateCountdown()
        com.example.widget.PrayerAppWidgetProvider.updateAllWidgets(getApplication())
    }

    fun updateWidgets() {
        com.example.widget.PrayerAppWidgetProvider.updateAllWidgets(getApplication())
    }

    override fun onCleared() {
        super.onCleared()
        AudioPlayerHelper.stopSound()
    }
}
