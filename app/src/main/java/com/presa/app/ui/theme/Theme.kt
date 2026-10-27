package com.presa.app.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.presa.app.domain.model.ThemeMode

val LocalPresaColors = staticCompositionLocalOf { LightPresaColors }

private val PresaShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(9.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
fun PresaTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (isDark) DarkPresaColors else LightPresaColors
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = colors.navy,
            onPrimary = colors.onNavy,
            primaryContainer = colors.panel3,
            onPrimaryContainer = colors.navy,
            secondary = colors.gold,
            onSecondary = colors.onGold,
            secondaryContainer = colors.goldSoft,
            onSecondaryContainer = colors.ink,
            tertiary = colors.forwardBlue,
            onTertiary = colors.onForward,
            background = colors.background,
            onBackground = colors.ink,
            surface = colors.panel,
            onSurface = colors.ink,
            surfaceVariant = colors.panel2,
            onSurfaceVariant = colors.ink2,
            surfaceTint = colors.navy,
            inversePrimary = colors.gold,
            surfaceBright = colors.panel,
            surfaceDim = colors.background,
            surfaceContainer = colors.panel,
            surfaceContainerHigh = colors.panel2,
            surfaceContainerHighest = colors.panel3,
            surfaceContainerLow = colors.panel2,
            surfaceContainerLowest = colors.panel,
            outline = colors.lineStrong,
            outlineVariant = colors.line,
            error = colors.alert,
            onError = colors.onAlert,
            errorContainer = colors.alertSoft,
            onErrorContainer = colors.alert,
            inverseSurface = colors.ink,
            inverseOnSurface = colors.panel,
            scrim = Color.Black,
        )
    } else {
        lightColorScheme(
            primary = colors.navy,
            onPrimary = colors.onNavy,
            primaryContainer = colors.panel3,
            onPrimaryContainer = colors.navy,
            secondary = colors.gold,
            onSecondary = colors.onGold,
            secondaryContainer = colors.goldSoft,
            onSecondaryContainer = colors.ink,
            tertiary = colors.forwardBlue,
            onTertiary = colors.onForward,
            background = colors.background,
            onBackground = colors.ink,
            surface = colors.panel,
            onSurface = colors.ink,
            surfaceVariant = colors.panel2,
            onSurfaceVariant = colors.ink2,
            surfaceTint = colors.navy,
            inversePrimary = colors.gold,
            surfaceBright = colors.panel,
            surfaceDim = colors.background,
            surfaceContainer = colors.panel,
            surfaceContainerHigh = colors.panel2,
            surfaceContainerHighest = colors.panel3,
            surfaceContainerLow = colors.panel2,
            surfaceContainerLowest = colors.panel,
            outline = colors.lineStrong,
            outlineVariant = colors.line,
            error = colors.alert,
            onError = colors.onAlert,
            errorContainer = colors.alertSoft,
            onErrorContainer = colors.alert,
            inverseSurface = colors.ink,
            inverseOnSurface = colors.panel,
            scrim = Color.Black,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalPresaColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PresaTypography,
            shapes = PresaShapes,
            content = content,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
