package au.edu.jcu.assessment.utilityapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CreamLightColors = lightColorScheme(
    primary = Color(0xFF805035),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE1C5),
    onPrimaryContainer = Color(0xFF452B1D),

    secondary = Color(0xFF766044),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4E6CD),
    onSecondaryContainer = Color(0xFF493923),

    tertiary = Color(0xFF637252),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE1EACF),
    onTertiaryContainer = Color(0xFF303E23),

    background = Color(0xFFFFF8EF),
    onBackground = Color(0xFF392D26),
    surface = Color(0xFFFFF8EF),
    onSurface = Color(0xFF392D26),
    surfaceVariant = Color(0xFFF1E5D8),
    onSurfaceVariant = Color(0xFF6D5B4D),
    surfaceContainer = Color(0xFFF5EBDD),
    surfaceContainerLow = Color(0xFFFFF2E3),
    surfaceContainerHigh = Color(0xFFEFE1D1),
    surfaceContainerHighest = Color(0xFFE8D8C7),

    outline = Color(0xFF9B8571),
    outlineVariant = Color(0xFFDCCBB8),

    error = Color(0xFFA33732),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDCD4),
    onErrorContainer = Color(0xFF652B26)
)

private val CreamDarkColors = darkColorScheme(
    primary = Color(0xFFF0BB95),
    onPrimary = Color(0xFF452B1D),
    primaryContainer = Color(0xFF60402B),
    onPrimaryContainer = Color(0xFFFFE1C5),

    secondary = Color(0xFFD9C3A1),
    onSecondary = Color(0xFF3D301F),
    secondaryContainer = Color(0xFF554632),
    onSecondaryContainer = Color(0xFFF4E6CD),

    tertiary = Color(0xFFBDCDA5),
    onTertiary = Color(0xFF29351F),
    tertiaryContainer = Color(0xFF435438),
    onTertiaryContainer = Color(0xFFE1EACF),

    background = Color(0xFF201A16),
    onBackground = Color(0xFFF2E4D8),
    surface = Color(0xFF201A16),
    onSurface = Color(0xFFF2E4D8),
    surfaceVariant = Color(0xFF504338),
    onSurfaceVariant = Color(0xFFD5C4B5),
    surfaceContainer = Color(0xFF2B231D),
    surfaceContainerLow = Color(0xFF261F19),
    surfaceContainerHigh = Color(0xFF362C24),
    surfaceContainerHighest = Color(0xFF41362D),

    outline = Color(0xFFA99889),
    outlineVariant = Color(0xFF504338),

    error = Color(0xFFFFB4A8),
    onError = Color(0xFF651E19),
    errorContainer = Color(0xFF81302A),
    onErrorContainer = Color(0xFFFFDCD4)
)

@Composable
fun MobileTech_UtilityAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CreamDarkColors else CreamLightColors,
        typography = Typography,
        content = content
    )
}