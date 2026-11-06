package vn.ecard.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object EC {
    val Bg = Color(0xFF0F1626)
    val Surface = Color(0xFF18233A)
    val Surface2 = Color(0xFF22304D)
    val Gold = Color(0xFFE8C25A)
    val Red = Color(0xFFC62828)
    val Text = Color(0xFFF3EEDF)
    val Muted = Color(0xFF9AA6BF)
}

private val scheme = darkColorScheme(
    primary = EC.Gold,
    onPrimary = Color(0xFF2A1E00),
    secondary = EC.Red,
    onSecondary = Color.White,
    background = EC.Bg,
    onBackground = EC.Text,
    surface = EC.Surface,
    onSurface = EC.Text,
    surfaceVariant = EC.Surface2,
    onSurfaceVariant = EC.Muted,
    surfaceContainer = EC.Surface,
    surfaceContainerHigh = EC.Surface2,
    outline = Color(0xFF3A4A6B)
)

@Composable
fun ECardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
