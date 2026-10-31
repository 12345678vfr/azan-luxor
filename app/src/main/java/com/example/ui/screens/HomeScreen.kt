package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.TimeMode
import com.example.ui.MainUiState
import com.example.ui.components.PrayerCard

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onSelectMonth: (Int) -> Unit,
    onSelectDay: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayListState = rememberLazyListState()

    // Scroll to selected day on change
    LaunchedEffect(uiState.selectedDay, uiState.selectedMonth) {
        val targetIndex = (uiState.selectedDay - 1).coerceAtLeast(0)
        dayListState.animateScrollToItem(targetIndex)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Section with Image & Next Prayer Countdown & Iqama Timer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                // Background Illustration
                Image(
                    painter = painterResource(id = R.drawable.luxor_prayer_hero_1790913562140),
                    contentDescription = "الأقصر",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark atmospheric gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x990A1A17),
                                    Color(0xCC0E2C26),
                                    Color(0xF00A1815)
                                )
                            )
                        )
                )

                // Content inside Hero
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Bar inside Hero: App Name "نداء" & Location & Time Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "نداء • محافظة الأقصر",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        // Time mode chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.timeMode == TimeMode.SUMMER)
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            modifier = Modifier
                                .testTag("time_mode_badge")
                                .clickable { onNavigateToSettings() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.timeMode == TimeMode.SUMMER)
                                        Icons.Default.WbSunny
                                    else
                                        Icons.Default.NightsStay,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = uiState.timeMode.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Next Prayer & Live Countdown & Iqama Counter
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Iqama Banner (Shows between Adhan and Iqama)
                        AnimatedVisibility(
                            visible = uiState.iqamaState != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            uiState.iqamaState?.let { iqama ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .testTag("iqama_countdown_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "متبقي على إقامة صلاة ${iqama.prayer.arabicName}:  ${iqama.formattedRemaining} دقيقة",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.nextPrayer != null) {
                            Text(
                                text = "الصلاة القادمة: ${uiState.nextPrayer.type.arabicName}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 22.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Large live countdown
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x33FFFFFF),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "متبقي  ${uiState.remainingTimeFormatted}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "موعد الأذان: ${uiState.nextPrayer.formatted12h}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xCCFFFFFF),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        } else {
                            Text(
                                text = "تمت جميع صلوات اليوم",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Dates: Gregorian & Hijri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.dayOfWeekArabic}، ${uiState.gregorianDateArabic}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xEEFFFFFF),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = uiState.hijriDateArabic,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Month Selector Bar (أكتوبر / نوفمبر)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // October Button
                val isOct = uiState.selectedMonth == 10
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("month_btn_10")
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isOct) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = if (isOct) 2.dp else 1.dp,
                            color = if (isOct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectMonth(10) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "شهر أكتوبر (10)",
                        fontWeight = if (isOct) FontWeight.Bold else FontWeight.Medium,
                        color = if (isOct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }

                // November Button
                val isNov = uiState.selectedMonth == 11
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("month_btn_11")
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isNov) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = if (isNov) 2.dp else 1.dp,
                            color = if (isNov) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectMonth(11) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "شهر نوفمبر (11)",
                        fontWeight = if (isNov) FontWeight.Bold else FontWeight.Medium,
                        color = if (isNov) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Days of Month Selector (1..30 or 1..31)
        item {
            val monthLabel = if (uiState.selectedMonth == 11) "نوفمبر" else "أكتوبر"
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مواقيت شهر $monthLabel - يوم ${uiState.selectedDay}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    if (!uiState.isTodayView) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .testTag("today_quick_btn")
                                .clickable {
                                    val now = java.util.Calendar.getInstance()
                                    val curMonth = (now.get(java.util.Calendar.MONTH) + 1).let { if (it == 11) 11 else 10 }
                                    val curDay = now.get(java.util.Calendar.DAY_OF_MONTH)
                                    onSelectMonth(curMonth)
                                    onSelectDay(curDay)
                                }
                        ) {
                            Text(
                                text = "العودة لليوم",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val curCal = java.util.Calendar.getInstance()
                val curCalMonth = curCal.get(java.util.Calendar.MONTH) + 1
                val curCalDay = curCal.get(java.util.Calendar.DAY_OF_MONTH)

                LazyRow(
                    state = dayListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.allMonthDays) { dayItem ->
                        val isSelected = dayItem.day == uiState.selectedDay
                        val isCurrentCalendarDay = (uiState.selectedMonth == curCalMonth && dayItem.day == curCalDay)

                        Box(
                            modifier = Modifier
                                .testTag("day_chip_${dayItem.day}")
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isCurrentCalendarDay -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .border(
                                    width = if (isCurrentCalendarDay && !isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else if (isCurrentCalendarDay)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectDay(dayItem.day) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "يوم",
                                    fontSize = 11.sp,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${dayItem.day}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Daily Prayer Times
        item {
            Text(
                text = "مواعيد الصلاة واليوم",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        // Prayer Cards list
        items(uiState.dayPrayers) { prayerItem ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PrayerCard(item = prayerItem)
            }
        }
    }
}
