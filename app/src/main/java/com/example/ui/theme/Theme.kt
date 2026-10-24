package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = PineGreen80,
  onPrimary = PineGreenDark,
  primaryContainer = PineGreenLight,
  onPrimaryContainer = Color.White,
  secondary = Terracotta80,
  onSecondary = Color.Black,
  secondaryContainer = Terracotta,
  onSecondaryContainer = Color.White,
  tertiary = SageGreen80,
  background = DarkBackground,
  onBackground = DarkOnSurface,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFA5B0AB),
  outline = Color(0xFF3B4540)
)

private val LightColorScheme = lightColorScheme(
  primary = PineGreen,
  onPrimary = Color.White,
  primaryContainer = SageGreenContainer,
  onPrimaryContainer = PineGreenDark,
  secondary = Terracotta,
  onSecondary = Color.White,
  secondaryContainer = TerracottaContainer,
  onSecondaryContainer = Color(0xFF4A1907),
  tertiary = SageGreen,
  background = WarmCream,
  onBackground = CharcoalText,
  surface = WarmSurface,
  onSurface = CharcoalText,
  surfaceVariant = WarmSurfaceVariant,
  onSurfaceVariant = CharcoalMuted,
  outline = WarmBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Preserve brand identity
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
