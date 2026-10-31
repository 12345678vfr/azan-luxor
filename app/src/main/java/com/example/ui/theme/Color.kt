package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemePalette

// 1. EMERALD PALETTE (واحة الزمرد)
val EmeraldPrimaryLight = Color(0xFF135A52)
val EmeraldOnPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFFD0F0E8)
val EmeraldOnPrimaryContainerLight = Color(0xFF03201D)
val EmeraldSecondaryLight = Color(0xFFB45309)
val EmeraldBackgroundLight = Color(0xFFF7FAF8)
val EmeraldSurfaceLight = Color(0xFFFFFFFF)
val EmeraldSurfaceVariantLight = Color(0xFFE5EDE8)

val EmeraldPrimaryDark = Color(0xFF5EEAD4)
val EmeraldOnPrimaryDark = Color(0xFF003832)
val EmeraldPrimaryContainerDark = Color(0xFF0F4D46)
val EmeraldOnPrimaryContainerDark = Color(0xFFCCFBF1)
val EmeraldSecondaryDark = Color(0xFFFCD34D)
val EmeraldBackgroundDark = Color(0xFF0B1413)
val EmeraldSurfaceDark = Color(0xFF121F1D)
val EmeraldSurfaceVariantDark = Color(0xFF1C2C29)

// 2. SAPPHIRE PALETTE (سكون الليل)
val SapphirePrimaryLight = Color(0xFF1E3A8A)
val SapphireOnPrimaryLight = Color(0xFFFFFFFF)
val SapphirePrimaryContainerLight = Color(0xFFDBEAFE)
val SapphireOnPrimaryContainerLight = Color(0xFF172554)
val SapphireSecondaryLight = Color(0xFF0D9488)
val SapphireBackgroundLight = Color(0xFFF6F8FC)
val SapphireSurfaceLight = Color(0xFFFFFFFF)
val SapphireSurfaceVariantLight = Color(0xFFE2E8F0)

val SapphirePrimaryDark = Color(0xFF93C5FD)
val SapphireOnPrimaryDark = Color(0xFF172554)
val SapphirePrimaryContainerDark = Color(0xFF1E3A8A)
val SapphireOnPrimaryContainerDark = Color(0xFFDBEAFE)
val SapphireSecondaryDark = Color(0xFF2DD4BF)
val SapphireBackgroundDark = Color(0xFF0A0F1D)
val SapphireSurfaceDark = Color(0xFF111827)
val SapphireSurfaceVariantDark = Color(0xFF1E293B)

// 3. AMBER SAND PALETTE (رمال الأقصر)
val AmberSandPrimaryLight = Color(0xFF854D0E)
val AmberSandOnPrimaryLight = Color(0xFFFFFFFF)
val AmberSandPrimaryContainerLight = Color(0xFFFEF3C7)
val AmberSandOnPrimaryContainerLight = Color(0xFF451A03)
val AmberSandSecondaryLight = Color(0xFF9A3412)
val AmberSandBackgroundLight = Color(0xFFFDFBF7)
val AmberSandSurfaceLight = Color(0xFFFFFFFF)
val AmberSandSurfaceVariantLight = Color(0xFFEFE8DB)

val AmberSandPrimaryDark = Color(0xFFFCD34D)
val AmberSandOnPrimaryDark = Color(0xFF451A03)
val AmberSandPrimaryContainerDark = Color(0xFF78350F)
val AmberSandOnPrimaryContainerDark = Color(0xFFFEF3C7)
val AmberSandSecondaryDark = Color(0xFFFB923C)
val AmberSandBackgroundDark = Color(0xFF14110B)
val AmberSandSurfaceDark = Color(0xFF1E1A14)
val AmberSandSurfaceVariantDark = Color(0xFF2C251D)

// 4. OLIVE PALETTE (الزيتوني الوادع)
val OlivePrimaryLight = Color(0xFF3F6212)
val OliveOnPrimaryLight = Color(0xFFFFFFFF)
val OlivePrimaryContainerLight = Color(0xFFECFCCB)
val OliveOnPrimaryContainerLight = Color(0xFF1A2E05)
val OliveSecondaryLight = Color(0xFF4D7C0F)
val OliveBackgroundLight = Color(0xFFF8FAF6)
val OliveSurfaceLight = Color(0xFFFFFFFF)
val OliveSurfaceVariantLight = Color(0xFFE8EDE4)

val OlivePrimaryDark = Color(0xFFA3E635)
val OliveOnPrimaryDark = Color(0xFF1A2E05)
val OlivePrimaryContainerDark = Color(0xFF365314)
val OliveOnPrimaryContainerDark = Color(0xFFECFCCB)
val OliveSecondaryDark = Color(0xFFBEF264)
val OliveBackgroundDark = Color(0xFF0E140B)
val OliveSurfaceDark = Color(0xFF161E12)
val OliveSurfaceVariantDark = Color(0xFF222C1D)

fun getThemeColorScheme(palette: AppThemePalette, isDark: Boolean): ColorScheme {
    return if (isDark) {
        when (palette) {
            AppThemePalette.EMERALD -> darkColorScheme(
                primary = EmeraldPrimaryDark,
                onPrimary = EmeraldOnPrimaryDark,
                primaryContainer = EmeraldPrimaryContainerDark,
                onPrimaryContainer = EmeraldOnPrimaryContainerDark,
                secondary = EmeraldSecondaryDark,
                background = EmeraldBackgroundDark,
                surface = EmeraldSurfaceDark,
                surfaceVariant = EmeraldSurfaceVariantDark,
                onSurface = Color(0xFFE2E8E6),
                onSurfaceVariant = Color(0xFFB4C4C0)
            )
            AppThemePalette.SAPPHIRE -> darkColorScheme(
                primary = SapphirePrimaryDark,
                onPrimary = SapphireOnPrimaryDark,
                primaryContainer = SapphirePrimaryContainerDark,
                onPrimaryContainer = SapphireOnPrimaryContainerDark,
                secondary = SapphireSecondaryDark,
                background = SapphireBackgroundDark,
                surface = SapphireSurfaceDark,
                surfaceVariant = SapphireSurfaceVariantDark,
                onSurface = Color(0xFFE2E8F0),
                onSurfaceVariant = Color(0xFF94A3B8)
            )
            AppThemePalette.AMBER_SAND -> darkColorScheme(
                primary = AmberSandPrimaryDark,
                onPrimary = AmberSandOnPrimaryDark,
                primaryContainer = AmberSandPrimaryContainerDark,
                onPrimaryContainer = AmberSandOnPrimaryContainerDark,
                secondary = AmberSandSecondaryDark,
                background = AmberSandBackgroundDark,
                surface = AmberSandSurfaceDark,
                surfaceVariant = AmberSandSurfaceVariantDark,
                onSurface = Color(0xFFF3EFEA),
                onSurfaceVariant = Color(0xFFC7BEB3)
            )
            AppThemePalette.OLIVE -> darkColorScheme(
                primary = OlivePrimaryDark,
                onPrimary = OliveOnPrimaryDark,
                primaryContainer = OlivePrimaryContainerDark,
                onPrimaryContainer = OliveOnPrimaryContainerDark,
                secondary = OliveSecondaryDark,
                background = OliveBackgroundDark,
                surface = OliveSurfaceDark,
                surfaceVariant = OliveSurfaceVariantDark,
                onSurface = Color(0xFFE7ECE4),
                onSurfaceVariant = Color(0xFFB6C2B2)
            )
        }
    } else {
        when (palette) {
            AppThemePalette.EMERALD -> lightColorScheme(
                primary = EmeraldPrimaryLight,
                onPrimary = EmeraldOnPrimaryLight,
                primaryContainer = EmeraldPrimaryContainerLight,
                onPrimaryContainer = EmeraldOnPrimaryContainerLight,
                secondary = EmeraldSecondaryLight,
                background = EmeraldBackgroundLight,
                surface = EmeraldSurfaceLight,
                surfaceVariant = EmeraldSurfaceVariantLight,
                onSurface = Color(0xFF19211F),
                onSurfaceVariant = Color(0xFF43504C)
            )
            AppThemePalette.SAPPHIRE -> lightColorScheme(
                primary = SapphirePrimaryLight,
                onPrimary = SapphireOnPrimaryLight,
                primaryContainer = SapphirePrimaryContainerLight,
                onPrimaryContainer = SapphireOnPrimaryContainerLight,
                secondary = SapphireSecondaryLight,
                background = SapphireBackgroundLight,
                surface = SapphireSurfaceLight,
                surfaceVariant = SapphireSurfaceVariantLight,
                onSurface = Color(0xFF0F172A),
                onSurfaceVariant = Color(0xFF475569)
            )
            AppThemePalette.AMBER_SAND -> lightColorScheme(
                primary = AmberSandPrimaryLight,
                onPrimary = AmberSandOnPrimaryLight,
                primaryContainer = AmberSandPrimaryContainerLight,
                onPrimaryContainer = AmberSandOnPrimaryContainerLight,
                secondary = AmberSandSecondaryLight,
                background = AmberSandBackgroundLight,
                surface = AmberSandSurfaceLight,
                surfaceVariant = AmberSandSurfaceVariantLight,
                onSurface = Color(0xFF292524),
                onSurfaceVariant = Color(0xFF57534E)
            )
            AppThemePalette.OLIVE -> lightColorScheme(
                primary = OlivePrimaryLight,
                onPrimary = OliveOnPrimaryLight,
                primaryContainer = OlivePrimaryContainerLight,
                onPrimaryContainer = OliveOnPrimaryContainerLight,
                secondary = OliveSecondaryLight,
                background = OliveBackgroundLight,
                surface = OliveSurfaceLight,
                surfaceVariant = OliveSurfaceVariantLight,
                onSurface = Color(0xFF1C2216),
                onSurfaceVariant = Color(0xFF4C5843)
            )
        }
    }
}
