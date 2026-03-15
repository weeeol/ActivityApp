package com.weeeol.activityapp.ui.theme

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
    primary = MidnightAccent,           // Buttons and active icons will be crisp white
    secondary = MidnightSecondaryText,
    background = MidnightBackground,    // The deep matte black from your image
    surface = MidnightSurface,          // Your cards (Health, Timers)
    surfaceVariant = MidnightSurfaceVariant, // Your Keep Notes and Nav Bar

    // This ensures your text automatically stays white against the black!
    onPrimary = Color.Black,            // Text inside a white button becomes black
    onBackground = Color.White,         // Text on the background is white
    onSurface = Color.White             // Text on cards is white
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,               // Active icons and buttons will be crisp black
    secondary = Color.DarkGray,
    background = Color(0xFFF8F9FA),      // A clean, soft off-white background
    surface = Color.White,               // Pure white for your cards
    surfaceVariant = Color(0xFFE9ECEF),  // Soft grey for the glass navigation bar

    // This ensures your text automatically flips to black against the white background!
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ActivityAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 2. NEW: Add a toggle for dynamic color (defaults to true)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 3. THE MAGIC: Check if dynamic color is allowed AND if the phone is Android 12+ (S)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 4. Fallback for older Android versions
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // Make sure this matches your typography variable if you have one!
        typography = Typography,
        content = content
    )
}