package au.edu.jcu.assessment.utilityapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import java.util.Locale
import kotlin.math.roundToInt

// This screen receives state and events, so it can be previewed without networking.
@Composable
fun WeatherScreen(
    state: WeatherUiState,
    city: String,
    fahrenheit: Boolean,
    uvThreshold: Float,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Weather & UV",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                enabled = state.city == city && !state.loading,
                onClick = onRefresh
            ) { Text("Refresh") }
        }

        when {
            state.city != city || state.loading -> WeatherLoadingCard()
            state.error != null -> WeatherErrorCard(state.error, onRefresh)
            state.weather != null -> {
                val weather = state.weather
                CurrentWeatherCard(weather, fahrenheit)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Today's forecast",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${weather.forecastDate} · City local date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ForecastCards(weather, uvThreshold)
                WeatherAdviceCard(weather, uvThreshold)
            }
        }
        TextButton(onClick = { uriHandler.openUri("https://open-meteo.com/") }) {
            Text("Weather data by Open-Meteo")
        }
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

@Composable
private fun CurrentWeatherCard(weather: WeatherInfo, fahrenheit: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("CURRENT WEATHER", style = MaterialTheme.typography.labelMedium)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stacked = maxWidth < 240.dp || LocalDensity.current.fontScale > 1.3f
                val details: @Composable () -> Unit = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            formatTemperature(weather.temperatureC, fahrenheit),
                            fontSize = 42.sp,
                            lineHeight = 46.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            weatherDescription(weather.weatherCode),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                if (stacked) {
                    Column {
                        CuteWeatherIllustration(weather.weatherCode, Modifier.size(64.dp))
                        details()
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) { details() }
                        CuteWeatherIllustration(weather.weatherCode, Modifier.size(76.dp))
                    }
                }
            }
            Text(
                "Weather time: ${weather.weatherTime.replace('T', ' ')} · Local",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ForecastCards(weather: WeatherInfo, uvThreshold: Float) {
    val alert = isUvAlert(weather.uvMax, uvThreshold)
    val uvText = weather.uvMax?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"
    val uvSubtitle = when {
        weather.uvMax == null -> "Unavailable"
        alert -> "Forecast meets alert"
        else -> "Below your threshold"
    }
    val rainText = weather.rainMax?.let { "$it%" } ?: "N/A"
    val rainSubtitle = if (weather.rainMax == null) "Unavailable" else "Today's maximum"
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Stack the cards on narrow displays or when the user increases text size.
        val stacked = maxWidth < 300.dp || LocalDensity.current.fontScale > 1.3f
        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WeatherMetricCard("Maximum UV", uvText, uvSubtitle, Modifier.fillMaxWidth(), alert)
                WeatherMetricCard("Rain chance", rainText, rainSubtitle, Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WeatherMetricCard("Maximum UV", uvText, uvSubtitle, Modifier.weight(1f), alert)
                WeatherMetricCard("Rain chance", rainText, rainSubtitle, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeatherMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (highlighted) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WeatherAdviceCard(weather: WeatherInfo, uvThreshold: Float) {
    OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Before you head out",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (isUvAlert(weather.uvMax, uvThreshold)) {
                Text(
                    "Today's predicted maximum UV meets your alert threshold of ${uvThreshold.roundToInt()}.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                when {
                    weather.uvMax == null -> "UV forecast is unavailable."
                    weather.uvMax >= 3.0 -> "Use sun protection and seek shade."
                    else -> "UV is low. Check conditions before going out."
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                when {
                    weather.rainMax == null -> "Rain forecast is unavailable."
                    weather.rainMax >= 50 -> "Bring an umbrella."
                    else -> "Rain is less likely. Keep an eye on conditions."
                },
                style = MaterialTheme.typography.bodyMedium
            )
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
