package au.edu.jcu.assessment.utilityapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import java.util.Locale

// This screen receives state and events, so it can be previewed without networking.
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    city: String,
    fahrenheit: Boolean,
    uvThreshold: Float,
    onRefresh: () -> Unit,
    gear: PhotographyGear = PhotographyGear.PHONE,
    packedIds: Set<String> = emptySet(),
    photoTheme: PhotoTheme = PhotoTheme.STREET,
    completedTasks: Set<String> = emptySet(),
    onThemeChange: (PhotoTheme) -> Unit = {},
    onTaskChange: (String, Boolean) -> Unit = { _, _ -> },
    onResetTasks: () -> Unit = {},
    onGearChange: (PhotographyGear) -> Unit = {},
    onPackedChange: (String, Boolean) -> Unit = { _, _ -> },
    onResetPacking: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        // Preserve a task viewport on normal phones; allow whole-page scrolling
        // when large text or a short window needs more vertical space.
        val bounded = maxHeight >= 560.dp && LocalDensity.current.fontScale <= 1.3f
        val pageModifier = if (bounded) Modifier.fillMaxSize()
            else Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        Column(pageModifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Photo Walk Buddy", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(city, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(enabled = state.city == city && !state.loading,
                    onClick = onRefresh) { Text("Refresh") }
            }
            when {
                state.city != city || state.loading -> WeatherLoadingCard()
                state.error != null -> WeatherErrorCard(state.error, onRefresh)
                state.weather != null -> {
                    val weather = state.weather
                    CompactWeatherSummary(weather, fahrenheit, uvThreshold)
                    PhotoWalkPanel(
                        plan = buildPhotoWalkPlan(gear, weather.uvMax, weather.rainMax,
                            uvThreshold, weather.weatherCode),
                        gear = gear,
                        packedIds = packedIds,
                        inspiration = buildPhotoInspiration(photoTheme, weather.weatherCode, weather.rainMax),
                        theme = photoTheme,
                        completedTasks = completedTasks,
                        onThemeChange = onThemeChange,
                        onTaskChange = onTaskChange,
                        onResetTasks = onResetTasks,
                        onGearChange = onGearChange,
                        onPackedChange = onPackedChange,
                        onReset = onResetPacking,
                        bounded = bounded,
                        modifier = if (bounded) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactWeatherSummary(weather: WeatherInfo, fahrenheit: Boolean, uvThreshold: Float) {
    var details by rememberSaveable { mutableStateOf(false) }
    val uv = weather.uvMax?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"
    val rain = weather.rainMax?.let { "$it%" } ?: "N/A"
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CuteWeatherIllustration(weather.weatherCode, Modifier.size(42.dp))
                Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(formatTemperature(weather.temperatureC, fahrenheit),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(weatherDescription(weather.weatherCode), style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { details = true }) { Text("Details") }
            }
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val metrics: @Composable () -> Unit = {
                    Text("Max UV $uv", style = MaterialTheme.typography.labelLarge,
                        color = if (isUvAlert(weather.uvMax, uvThreshold)) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Max rain $rain", style = MaterialTheme.typography.labelLarge)
                }
                if (maxWidth < 260.dp || LocalDensity.current.fontScale > 1.3f) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { metrics() }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { metrics() }
                }
            }
            Text("Today's forecast · ${weather.forecastDate}", style = MaterialTheme.typography.labelSmall)
        }
    }
    if (details) {
        val uriHandler = LocalUriHandler.current
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { details = false },
            title = { Text("Weather details") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current weather: ${weatherDescription(weather.weatherCode)}")
                    Text("Weather time: ${weather.weatherTime.replace('T', ' ')} · City local time")
                    Text("Forecast date: ${weather.forecastDate}")
                    Text("Maximum UV: $uv · Reminder threshold: ${uvThreshold.toInt()}")
                    Text("Maximum rain probability: $rain")
                    Text("UV and rain are daily maximum forecasts, not readings for a specific walking time.")
                    TextButton(onClick = { uriHandler.openUri("https://open-meteo.com/") }) {
                        Text("Weather data by Open-Meteo")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { details = false }) { Text("Done") } }
        )
    }
}

@Composable
private fun WeatherLoadingCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
            Text("Getting the latest weather...")
        }
    }
}

@Composable
private fun WeatherErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Weather unavailable", style = MaterialTheme.typography.titleLarge)
            Text(message)
            Button(onClick = onRetry) { Text("Try again") }
        }
    }
}

@Preview(name = "Cream weather", showBackground = true, widthDp = 360, heightDp = 740)
@Preview(name = "Large text", showBackground = true, widthDp = 320, heightDp = 740, fontScale = 1.5f)
@Composable
private fun WeatherScreenPreview() {
    MobileTech_UtilityAppTheme(darkTheme = false) {
        WeatherScreen(
            state = WeatherUiState(
                city = "Singapore",
                weather = WeatherInfo("Singapore", 29.5, 2, 7.2, 60, "2026-10-10", "2026-10-10T17:00")
            ),
            city = "Singapore",
            fahrenheit = false,
            uvThreshold = 6f,
            onRefresh = {}
        )
    }
}
