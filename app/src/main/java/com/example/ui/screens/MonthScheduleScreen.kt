package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerType
import com.example.data.model.TimeMode
import com.example.data.repository.PrayerRepository
import com.example.ui.MainUiState
import java.util.Calendar

@Composable
fun MonthScheduleScreen(
    uiState: MainUiState,
    repository: PrayerRepository,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCalendarDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("month_schedule_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Screen Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "جدول مواقيت الصلاة - شهر أكتوبر",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "محافظة الأقصر • ${uiState.timeMode.displayName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "31 يومًا",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Table container with horizontal scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("اليوم", width = 50.dp)
                    TableHeaderCell("الفجر", width = 64.dp)
                    TableHeaderCell("الشروق", width = 64.dp)
                    TableHeaderCell("الظهر", width = 64.dp)
                    TableHeaderCell("العصر", width = 64.dp)
                    TableHeaderCell("الغروب", width = 64.dp)
                    TableHeaderCell("المغرب", width = 64.dp)
                    TableHeaderCell("العشاء", width = 64.dp)
                }

                // Table Rows
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(uiState.allMonthDays) { day ->
                        val isToday = day.day == currentCalendarDay
                        val isSelected = day.day == uiState.selectedDay

                        val fajr = repository.computeAdjustedTime(day.fajr, uiState.timeMode).time24
                        val sunrise = repository.computeAdjustedTime(day.sunrise, uiState.timeMode).time24
                        val dhuhr = repository.computeAdjustedTime(day.dhuhr, uiState.timeMode).time24
                        val asr = repository.computeAdjustedTime(day.asr, uiState.timeMode).time24
                        val sunset = repository.computeAdjustedTime(day.sunset, uiState.timeMode).time24
                        val maghrib = repository.computeAdjustedTime(day.maghrib, uiState.timeMode).time24
                        val isha = repository.computeAdjustedTime(day.isha, uiState.timeMode).time24

                        val rowBackground = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                            day.day % 2 == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Row(
                            modifier = Modifier
                                .testTag("table_row_${day.day}")
                                .background(rowBackground)
                                .clickable { onDayClick(day.day) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(
                                text = "${day.day}${if (isToday) " ★" else ""}",
                                width = 50.dp,
                                isHighlight = isToday || isSelected,
                                isBold = true
                            )
                            TableCell(text = fajr, width = 64.dp)
                            TableCell(text = sunrise, width = 64.dp, isSecondary = true)
                            TableCell(text = dhuhr, width = 64.dp)
                            TableCell(text = asr, width = 64.dp)
                            TableCell(text = sunset, width = 64.dp, isSecondary = true)
                            TableCell(text = maghrib, width = 64.dp)
                            TableCell(text = isha, width = 64.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "★ اليوم الحالي • اضغط على أي يوم لعرض تفاصيله في الشاشة الرئيسية",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHighlight: Boolean = false,
    isSecondary: Boolean = false,
    isBold: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isBold || isHighlight) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            color = when {
                isHighlight -> MaterialTheme.colorScheme.primary
                isSecondary -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                else -> MaterialTheme.colorScheme.onSurface
            }
        ),
        textAlign = TextAlign.Center
    )
}
