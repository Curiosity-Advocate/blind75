package grocery.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Light = lightColorScheme(
    primary = Color(0xFF2E7D32),          // fresh green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7E4B9),
    onPrimaryContainer = Color(0xFF0B2E0D),
    secondary = Color(0xFF5B6C5B),
    secondaryContainer = Color(0xFFDDE7DA),
    onSecondaryContainer = Color(0xFF161D16),
    tertiary = Color(0xFFB07C00),         // money gold, for accents
    background = Color(0xFFF6FAF4),
    onBackground = Color(0xFF191D18),
    surface = Color(0xFFFBFDF9),
    onSurface = Color(0xFF191D18),
    surfaceVariant = Color(0xFFE3E9E0),
    onSurfaceVariant = Color(0xFF43483F),
    error = Color(0xFFB3261E),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF9CD69B),
    onPrimary = Color(0xFF0B380F),
    primaryContainer = Color(0xFF265428),
    onPrimaryContainer = Color(0xFFB7E4B9),
    secondary = Color(0xFFBFC9BB),
    secondaryContainer = Color(0xFF414A40),
    onSecondaryContainer = Color(0xFFDDE7DA),
    tertiary = Color(0xFFE9C46A),
    background = Color(0xFF11140F),
    onBackground = Color(0xFFE1E4DC),
    surface = Color(0xFF181C16),
    onSurface = Color(0xFFE1E4DC),
    surfaceVariant = Color(0xFF43483F),
    onSurfaceVariant = Color(0xFFC3C8BC),
)

private val GroceryShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun GroceryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        shapes = GroceryShapes,
        content = content,
    )
}
