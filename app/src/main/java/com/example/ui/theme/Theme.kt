package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JobExpressProfessionalColorScheme = lightColorScheme(
    primary = RubyRed,
    onPrimary = PureWhite,
    primaryContainer = RubyRedBg,
    onPrimaryContainer = RubyRed,
    secondary = RoyalPurple,
    onSecondary = PureWhite,
    secondaryContainer = RoyalPurpleBg,
    onSecondaryContainer = RoyalPurpleDark,
    background = SurfaceLight,
    onBackground = TextDark,
    surface = PureWhite,
    onSurface = TextDark,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextDarkSecondary,
    outline = BorderLight,
    outlineVariant = DividerLight,
    error = RubyRed,
    onError = PureWhite
)

@Composable
fun JobExpressTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JobExpressProfessionalColorScheme,
        typography = Typography,
        content = content
    )
}
