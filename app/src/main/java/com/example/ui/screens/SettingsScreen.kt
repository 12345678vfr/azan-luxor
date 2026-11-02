package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppThemePalette
import com.example.data.model.PrayerAlarmConfig
import com.example.data.model.PrayerType
import com.example.data.model.TimeMode
import com.example.ui.MainUiState

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onTimeModeChange: (TimeMode) -> Unit,
    onThemePaletteChange: (AppThemePalette) -> Unit,
    onAlarmConfigChange: (PrayerType, Boolean, Int) -> Unit,
    onSetCustomSound: (String, String?) -> Unit,
    onSetDefaultSound: () -> Unit,
    onStartSoundTest: () -> Unit,
    onStopSoundTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = queryFileName(context, uri) ?: "ملف صوتي مخصص"
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Keep uri
            }
            onSetCustomSound(uri.toString(), fileName)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_content"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Permission Card (if not granted)
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notification_permission_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "إذن الإشعارات مطلوب",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                        Text(
                            text = "لضمان إرسال التنبيهات والأذان في موعدها حتى والتطبيق مغلق، يرجى تفعيل إذن الإشعارات.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("grant_permission_btn")
                        ) {
                            Text("منح إذن الإشعارات", color = Color.White)
                        }
                    }
                }
            }
        }

        // Section 1: Themes (ألوان وثيمات التطبيق الهادئة)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_selection_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "مظهر وثيم التطبيق (ألوان هادئة للعين)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "اختر السمة اللونية المريحة لك؛ جميع الثيمات مصممة بألوان هادئة ومريحة للنفس ووضع القراءة.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Theme selector items
                    AppThemePalette.entries.forEach { palette ->
                        val isSelected = uiState.appPalette == palette
                        val swatchColor = when (palette) {
                            AppThemePalette.EMERALD -> Color(0xFF135A52)
                            AppThemePalette.SAPPHIRE -> Color(0xFF1E3A8A)
                            AppThemePalette.AMBER_SAND -> Color(0xFF854D0E)
                            AppThemePalette.OLIVE -> Color(0xFF3F6212)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onThemePaletteChange(palette) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                )
                                Column {
                                    Text(
                                        text = palette.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = palette.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "محدد",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Daylight Saving / Winter Time
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("time_mode_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "نظام التوقيت (الصيفي / الشتوي)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "يبدأ التطبيق افتراضيًا بالتوقيت الشتوي لمحافظة الأقصر، وعند اختيار التوقيت الصيفي يتم تطبيق فرق الساعة وتحديث جميع التنبيهات تلقائيًا.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    val isSummer = uiState.timeMode == TimeMode.SUMMER
                    val isWinter = uiState.timeMode == TimeMode.WINTER

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summer Time Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_summer_time")
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSummer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSummer) 2.dp else 1.dp,
                                    color = if (isSummer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onTimeModeChange(TimeMode.SUMMER) }
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (isSummer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "التوقيت الصيفي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSummer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Winter Time Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_winter_time")
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isWinter) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isWinter) 2.dp else 1.dp,
                                    color = if (isWinter) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onTimeModeChange(TimeMode.WINTER) }
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.NightsStay,
                                    contentDescription = null,
                                    tint = if (isWinter) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "التوقيت الشتوي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isWinter) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Sound Settings & Audio Test
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sound_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "صوت التنبيه والأذان",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Option 1: Default Madinah Adhan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSetDefaultSound() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !uiState.soundConfig.isCustom,
                            onClick = { onSetDefaultSound() }
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = "أذان المدينة المنورة الافتراضي",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "صوت أذان الحرم المدني الشريف بجودة نقية ومدمج داخل التطبيق",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Option 2: Custom audio from device
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                audioPickerLauncher.launch("audio/*")
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.soundConfig.isCustom,
                            onClick = {
                                audioPickerLauncher.launch("audio/*")
                            }
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(
                                text = "اختيار ملف صوتي من الهاتف",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (uiState.soundConfig.isCustom && !uiState.soundConfig.customFileName.isNullOrEmpty())
                                    "الملف المحدد: ${uiState.soundConfig.customFileName}"
                                else
                                    "يمكنك اختيار ملف صوتي أو أذان مسجل من ذاكرة هاتفك",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.soundConfig.isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (uiState.soundConfig.isCustom) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("choose_sound_file_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.soundConfig.isCustom) "تغيير الملف الصوتي" else "استعراض ملفات الصوت بالهاتف",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Audio Test Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "اختبار الصوت الحالي",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (uiState.isTestingSound) "جاري تشغيل الأذان الآن..." else "استمع لتجربة الأذان ومستوى الصوت",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Button(
                            onClick = {
                                if (uiState.isTestingSound) {
                                    onStopSoundTest()
                                } else {
                                    onStartSoundTest()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isTestingSound) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("test_sound_btn")
                        ) {
                            Icon(
                                imageVector = if (uiState.isTestingSound) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isTestingSound) "إيقاف" else "تشغيل الأذان",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Alert Settings for ONLY the 5 Obligatory Prayers
        item {
            Text(
                text = "إعدادات تنبيهات الصلوات الخمس",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(PrayerType.obligatoryPrayers) { prayer ->
            val config = uiState.alarmConfigs[prayer] ?: PrayerAlarmConfig(prayer)
            PrayerAlertConfigCard(
                prayer = prayer,
                config = config,
                onConfigChange = { enabled, offset ->
                    onAlarmConfigChange(prayer, enabled, offset)
                }
            )
        }

        // Section 5: Home Screen Widget (ودجت الشاشة الرئيسية)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_screen_widget_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ودجت الشاشة الرئيسية (Home Screen Widget)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "يعرض الصلاة القادمة ووقتها والوقت المتبقي مباشرةً على شاشة هاتفك الرئيسية دون الحاجة لفتح التطبيق.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Widget Interactive Live Preview Card (Matching New Islamic Design with Iqama)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(min = 220.dp, max = 280.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(1.5.dp, Color(0xFF2C5F51), RoundedCornerShape(22.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0F3329),
                                            Color(0xFF0A241D),
                                            Color(0xFF061914)
                                        )
                                    )
                                )
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Bar
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
                                        tint = Color(0xFFE5B869),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "نداء • الأقصر",
                                        color = Color(0xFFE5B869),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${uiState.selectedDay} ${if (uiState.selectedMonth == 11) "نوفمبر" else "أكتوبر"}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            // Center Prayer & Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (uiState.iqamaState != null) "🕌 جارية الآن" else "الصلاة القادمة",
                                        color = if (uiState.iqamaState != null) Color(0xFFF59E0B) else Color(0xFF80B3A2),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = if (uiState.iqamaState != null)
                                            uiState.iqamaState?.prayer?.arabicName ?: "المغرب"
                                        else
                                            uiState.nextPrayer?.type?.arabicName ?: "المغرب",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = uiState.nextPrayer?.adjustedTime ?: "17:15",
                                        color = Color.White,
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Row 1: Adhan Countdown Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF174438),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A6455)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "باقي للأذان:",
                                        color = Color(0xFF80B3A2),
                                        fontSize = 11.sp
                                    )
                                    val remainingText = if (uiState.remainingSeconds > 0) {
                                        val hours = uiState.remainingSeconds / 3600
                                        val mins = (uiState.remainingSeconds % 3600) / 60
                                        if (hours > 0) "$hours س $mins د" else "$mins د"
                                    } else {
                                        "2 س 29 د"
                                    }
                                    Text(
                                        text = remainingText,
                                        color = Color(0xFF38D9A9),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Row 2: Iqama Countdown Pill (User Requested Feature)
                            val isIqamaActive = uiState.iqamaState != null
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isIqamaActive) Color(0xFFD97706) else Color(0xFF174438),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isIqamaActive) Color(0xFFFDE68A) else Color(0xFF2A6455)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isIqamaActive) "⏳ باقي للإقامة:" else "موعد الإقامة:",
                                        color = if (isIqamaActive) Color.White else Color(0xFF80B3A2),
                                        fontSize = 11.sp,
                                        fontWeight = if (isIqamaActive) FontWeight.Bold else FontWeight.Normal
                                    )
                                    val iqamaText = if (isIqamaActive) {
                                        "${uiState.iqamaState?.formattedRemaining} د"
                                    } else {
                                        val next = uiState.nextPrayer
                                        if (next != null) {
                                             val tot = next.hour24 * 60 + next.minute + next.type.iqamaMinutes
                                             val h = (tot / 60) % 24
                                             val m = tot % 60
                                             String.format(java.util.Locale.US, "%02d:%02d (%d د)", h, m, next.type.iqamaMinutes)
                                        } else {
                                            "17:20 (5 د)"
                                        }
                                    }
                                    Text(
                                        text = iqamaText,
                                        color = if (isIqamaActive) Color.White else Color(0xFFE5B869),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Add Widget Button
                    Button(
                        onClick = {
                            com.example.widget.PrayerAppWidgetProvider.updateAllWidgets(context)
                            val appWidgetManager = context.getSystemService(Context.APPWIDGET_SERVICE) as? android.appwidget.AppWidgetManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                                val myProvider = android.content.ComponentName(context, com.example.widget.PrayerAppWidgetProvider::class.java)
                                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                android.widget.Toast.makeText(
                                    context,
                                    "تم إرسال طلب إضافة الودجت للشاشة الرئيسية",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context,
                                    "اضغط مطولاً على الشاشة الرئيسية للهاتف واختر التطبيقات المصغرة (Widgets) ثم اختر «نداء»",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_widget_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة الودجت للشاشة الرئيسية", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "💡 نصيحة: يمكنك أيضاً الضغط مطولاً على أي مساحة فارغة في شاشة هاتفك واختيار (Widgets / تطبيقات مصغرة) واختيار ودجت «نداء» بالحجم الذي يناسبك.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Section 6: App Version & About
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_version_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "تطبيق نداء",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "مواقيت الصلاة لمحافظة الأقصر - مصر",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "الإصدار 1.0 (Version 1.0)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerAlertConfigCard(
    prayer: PrayerType,
    config: PrayerAlarmConfig,
    onConfigChange: (Boolean, Int) -> Unit
) {
    val offsetOptions = listOf(0, 5, 10, 15, 20, 30)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_card_${prayer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config.enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (config.enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (config.enabled)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = prayer.icon,
                            contentDescription = null,
                            tint = if (config.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "صلاة ${prayer.arabicName}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (config.enabled)
                                if (config.offsetMinutes == 0) "التنبيه في موعد الأذان تمامًا" else "التنبيه قبل الصلاة بـ ${config.offsetMinutes} دقيقة"
                            else
                                "التنبيه متوقف",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (config.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Switch(
                    checked = config.enabled,
                    onCheckedChange = { isChecked ->
                        onConfigChange(isChecked, config.offsetMinutes)
                    },
                    modifier = Modifier.testTag("alarm_switch_${prayer.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Offset Chips Selector (only if enabled)
            AnimatedVisibility(visible = config.enabled) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "موعد التنبيه المسبق:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(offsetOptions) { minutes ->
                            val isSelected = config.offsetMinutes == minutes
                            val label = if (minutes == 0) "في الموعد" else "$minutes د"

                            Box(
                                modifier = Modifier
                                    .testTag("offset_chip_${prayer.id}_$minutes")
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onConfigChange(true, minutes) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun queryFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return name ?: uri.lastPathSegment
}
