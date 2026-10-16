package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Light, Modern, Professional Color System:
// Crisp White Backgrounds
val PureWhite = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFF8FAFC)
val CardBackground = Color(0xFFFFFFFF)
val BorderLight = Color(0xFFE2E8F0)
val BorderMedium = Color(0xFFCBD5E1)
val DividerLight = Color(0xFFF1F5F9)

// Deep Black & Slate Typography
val TextDark = Color(0xFF0F172A)
val TextDarkSecondary = Color(0xFF334155)
val TextMuted = Color(0xFF64748B)
val TextLightMuted = Color(0xFF94A3B8)

// Crimson / Ruby Red Accent
val RubyRed = Color(0xFFDC2626)
val RubyRedLight = Color(0xFFEF4444)
val RubyRedBg = Color(0xFFFEE2E2)
val RubyRedBorder = Color(0xFFFECACA)

// Royal Purple / Violet Accent
val RoyalPurple = Color(0xFF7C3AED)
val RoyalPurpleDark = Color(0xFF6D28D9)
val RoyalPurpleLight = Color(0xFF8B5CF6)
val RoyalPurpleBg = Color(0xFFF5F3FF)
val RoyalPurpleBorder = Color(0xFFDDD6FE)

// Functional Status Colors
val SuccessGreen = Color(0xFF16A34A)
val SuccessGreenBg = Color(0xFFDCFCE7)
val WarningAmber = Color(0xFFD97706)
val WarningAmberBg = Color(0xFFFEF3C7)
val InfoBlue = Color(0xFF2563EB)
val InfoBlueBg = Color(0xFFDBEAFE)

// Premium Gradients
val RedGradient = Brush.linearGradient(
    listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
)

val PurpleGradient = Brush.linearGradient(
    listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
)

val VioletRedGradient = Brush.linearGradient(
    listOf(Color(0xFF7C3AED), Color(0xFFEF4444))
)

val DarkHeaderGradient = Brush.linearGradient(
    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
)
