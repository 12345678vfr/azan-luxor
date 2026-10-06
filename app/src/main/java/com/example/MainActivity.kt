package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.PrayerRepository
import com.example.ui.MainViewModel
import com.example.ui.screens.DhikrScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenTab(val title: String) {
    HOME("الرئيسية"),
    MONTH("جدول الشهر"),
    SETTINGS("الإعدادات"),
    DHIKR("الأذكار والقبلة")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = PrayerRepository(this)

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    LuxorPrayerApp(
                        viewModel = viewModel,
                        repository = repository
                    )
                }
            }
        }
    }
}

@Composable
fun LuxorPrayerApp(
    viewModel: MainViewModel,
    repository: PrayerRepository
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }

    // Android back button: return to Home screen if on secondary tab
    BackHandler(enabled = currentTab != ScreenTab.HOME) {
        currentTab = ScreenTab.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = { currentTab = ScreenTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.HOME) Icons.Filled.AccessTime else Icons.Outlined.AccessTime,
                            contentDescription = ScreenTab.HOME.title
                        )
                    },
                    label = {
                        Text(
                            text = ScreenTab.HOME.title,
                            fontWeight = if (currentTab == ScreenTab.HOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_home"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Month Schedule Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.MONTH,
                    onClick = { currentTab = ScreenTab.MONTH },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.MONTH) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = ScreenTab.MONTH.title
                        )
                    },
                    label = {
                        Text(
                            text = ScreenTab.MONTH.title,
                            fontWeight = if (currentTab == ScreenTab.MONTH) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_month"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Dhikr & Qibla Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.DHIKR,
                    onClick = { currentTab = ScreenTab.DHIKR },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.DHIKR) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = ScreenTab.DHIKR.title
                        )
                    },
                    label = {
                        Text(
                            text = ScreenTab.DHIKR.title,
                            fontWeight = if (currentTab == ScreenTab.DHIKR) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_dhikr"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Settings Tab
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = { currentTab = ScreenTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == ScreenTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = ScreenTab.SETTINGS.title
                        )
                    },
                    label = {
                        Text(
                            text = ScreenTab.SETTINGS.title,
                            fontWeight = if (currentTab == ScreenTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("nav_settings"),
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                when (currentTab) {
                    ScreenTab.HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            onSelectDay = { day ->
                                viewModel.selectDay(day)
                            },
                            onNavigateToSettings = {
                                currentTab = ScreenTab.SETTINGS
                            }
                        )
                    }

                    ScreenTab.MONTH -> {
                        MonthScheduleScreen(
                            uiState = uiState,
                            repository = repository,
                            onDayClick = { day ->
                                viewModel.selectDay(day)
                                currentTab = ScreenTab.HOME
                            }
                        )
                    }

                    ScreenTab.SETTINGS -> {
                        SettingsScreen(
                            uiState = uiState,
                            onTimeModeChange = { mode ->
                                viewModel.setTimeMode(mode)
                            },
                            onAlarmConfigChange = { prayer, enabled, offset ->
                                viewModel.updateAlarmConfig(prayer, enabled, offset)
                            },
                            onSetCustomSound = { uri, name ->
                                viewModel.setCustomSound(uri, name)
                            },
                            onSetDefaultSound = {
                                viewModel.setDefaultSound()
                            },
                            onStartSoundTest = {
                                viewModel.startSoundTest()
                            },
                            onStopSoundTest = {
                                viewModel.stopSoundTest()
                            }
                        )
                    }

                    ScreenTab.DHIKR -> {
                        DhikrScreen()
                    }
                }
            }
        }
    }
}
