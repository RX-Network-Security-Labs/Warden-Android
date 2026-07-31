package com.warden.rnsl.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warden Colors
val WardenBlue = Color(0xFF1565C0)
val WardenBlueDark = Color(0xFF0D47A1)
val WardenBlueLight = Color(0xFF1E88E5)
val WardenBlueContainer = Color(0xFFBBDEFB)
val WardenOnBlue = Color(0xFFFFFFFF)
val GoodGreen = Color(0xFF4CAF50)
val BadRed = Color(0xFFF44336)
val UnknownGrey = Color(0xFF9E9E9E)
val SurfaceLight = Color(0xFFF5F5F5)

private val LightColorScheme = lightColorScheme(
    primary = WardenBlue,
    onPrimary = Color.White,
    primaryContainer = WardenBlueContainer,
    onPrimaryContainer = WardenBlueDark,
    secondary = WardenBlueLight,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = SurfaceLight,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE3F2FD),
    onSurfaceVariant = Color(0xFF37474F),
    error = BadRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = WardenBlueLight,
    onPrimary = Color.Black,
    primaryContainer = WardenBlueDark,
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = WardenBlue,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF263238),
    onSurfaceVariant = Color(0xFFB0BEC5),
    error = BadRed,
    onError = Color.White
)

@Composable
fun WardenTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
