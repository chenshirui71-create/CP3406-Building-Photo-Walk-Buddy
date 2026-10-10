package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileTech_UtilityAppTheme {
                WeatherApp()
            }
        }
    }
}

@Composable
fun WeatherApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember(context.applicationContext) {
        SettingsRepository(context.applicationContext)
    }

    var selectedTab by rememberSaveable {
        mutableStateOf("Weather")
    }
    var city by rememberSaveable {
        mutableStateOf(settings.getCity())
    }
    var fahrenheit by rememberSaveable {
        mutableStateOf(settings.getFahrenheit())
    }
    var uvThreshold by rememberSaveable {
        mutableStateOf(settings.getUvThreshold())
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "Weather",
                    onClick = { selectedTab = "Weather" },
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = null)
                    },
                    label = { Text("Weather") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" },
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == "Weather") {
                WeatherScreen(city, fahrenheit, uvThreshold)
            } else {
                SettingsScreen(
                    city = city,
                    onCityChange = {
                        city = it
                        settings.saveCity(it)
                    },
                    fahrenheit = fahrenheit,
                    onUnitChange = {
                        fahrenheit = it
                        settings.saveFahrenheit(it)
                    },
                    uvThreshold = uvThreshold,
                    onThresholdChange = {
                        uvThreshold = it
                        settings.saveUvThreshold(it.roundToInt())
                    }
                )
            }
        }
    }
}

@Composable
fun WeatherScreen(
    city: String,
    fahrenheit: Boolean,
    uvThreshold: Float
) {
    val weatherViewModel: WeatherViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val state = weatherViewModel.uiState
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(city) {
        weatherViewModel.load(city)
    }

    Text(
        "Weather & UV",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column {
            Text(city, style = MaterialTheme.typography.titleLarge)
            Text(
                "Plan your day outdoors",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(
            enabled = state.city == city && !state.loading,
            onClick = { weatherViewModel.load(city, force = true) }
        ) {
            Text("Refresh")
        }
    }

    if (state.city != city || state.loading) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text("Getting the latest weather...")
            }
        }
    } else if (state.error != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Weather unavailable",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(state.error)
                Button(
                    onClick = {
                        weatherViewModel.load(city, force = true)
                    }
                ) {
                    Text("Try again")
                }
            }
        }
    } else {
        val weather = state.weather

        if (weather != null) {
            val temperature = if (fahrenheit) {
                weather.temperatureC * 9.0 / 5.0 + 32
            } else {
                weather.temperatureC
            }
            val unit = if (fahrenheit) "°F" else "°C"
            val temperatureText = String.format(
                java.util.Locale.US, "%.1f", temperature
            )
            val uv = weather.uvMax
            val rain = weather.rainMax
            val alert = uv != null && uv >= uvThreshold.toDouble()
            val uvText = uv?.let {
                String.format(java.util.Locale.US, "%.1f", it)
            } ?: "N/A"
            val rainText = rain?.let { "$it%" } ?: "N/A"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "CURRENT WEATHER",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            "$temperatureText$unit",
                            modifier = Modifier.weight(1f),
                            fontSize = 44.sp,
                            lineHeight = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        CuteWeatherIllustration(
                            code = weather.weatherCode,
                            modifier = Modifier.size(96.dp)
                        )
                    }
                    Text(
                        weatherDescription(weather.weatherCode),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Weather time: ${
                            weather.weatherTime.replace('T', ' ')
                        }\nCity local time",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                "Today's forecast",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                weather.forecastDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherCard(
                    title = "Maximum UV",
                    value = uvText,
                    subtitle = when {
                        uv == null -> "Unavailable"
                        alert -> "Threshold reached"
                        else -> "Below your threshold"
                    },
                    modifier = Modifier.weight(1f),
                    highlighted = alert
                )
                WeatherCard(
                    title = "Rain chance",
                    value = rainText,
                    subtitle = "Today's maximum",
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Before you head out",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (alert) {
                        Text(
                            "UV has reached your alert threshold of ${
                                uvThreshold.roundToInt()
                            }.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        when {
                            uv == null ->
                                "UV forecast is unavailable."
                            uv >= 3.0 ->
                                "Use sun protection and seek shade."
                            else ->
                                "UV is low. Check conditions before going out."
                        }
                    )
                    Text(
                        when {
                            rain == null ->
                                "Rain forecast is unavailable."
                            rain >= 50 ->
                                "Bring an umbrella."
                            else ->
                                "Rain is less likely. Keep an eye on conditions."
                        }
                    )
                }
            }
        }
    }

    TextButton(
        onClick = { uriHandler.openUri("https://open-meteo.com/") }
    ) {
        Text("Weather data by Open-Meteo")
    }
}

fun weatherSymbol(code: Int): String = when (code) {
    0, 1 -> "☀"
    2, 3 -> "☁"
    45, 48 -> "≋"
    51, 53, 55, 56, 57, 61, 63, 65, 66, 67,
    80, 81, 82 -> "☂"
    71, 73, 75, 77, 85, 86 -> "❄"
    95, 96, 99 -> "ϟ"
    else -> "—"
}

@Composable
fun WeatherCard(
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
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = if (highlighted) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SettingsScreen(
    city: String,
    onCityChange: (String) -> Unit,
    fahrenheit: Boolean,
    onUnitChange: (Boolean) -> Unit,
    uvThreshold: Float,
    onThresholdChange: (Float) -> Unit
) {
    Text(
        "Settings",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        "Make the forecast work for you",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Your city",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Choose the location for your forecast.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Singapore", "Brisbane").forEach { option ->
                    FilterChip(
                        selected = city == option,
                        onClick = { onCityChange(option) },
                        label = { Text(option) }
                    )
                }
            }
        }
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment =
                androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Temperature unit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (fahrenheit) "Fahrenheit (°F)" else "Celsius (°C)"
                )
            }
            Switch(
                checked = fahrenheit,
                onCheckedChange = onUnitChange
            )
        }
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "UV alert",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Highlight UV when it reaches your chosen threshold.")
            Text(
                uvThreshold.roundToInt().toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = uvThreshold,
                onValueChange = onThresholdChange,
                valueRange = 3f..11f,
                steps = 7
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("3")
                Text("11")
            }
            Text(
                "This changes the app's highlight, not the UV forecast.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    Text(
        "Your settings are saved automatically.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
@Composable
fun CuteWeatherIllustration(
    code: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fun point(x: Float, y: Float) =
            Offset(w * x / 100f, h * y / 100f)

        val cocoa = Color(0xFF654832)
        val sunshine = Color(0xFFFFC568)
        val cloudCream = Color(0xFFFFF9EF)
        val cloudShadow = Color(0xFFE6CFB4)
        val rainBlue = Color(0xFF7CABB3)

        val clear = code == 0 || code == 1
        val partlyCloudy = code == 2
        val fog = code == 45 || code == 48
        val rain = code in listOf(
            51, 53, 55, 56, 57, 61, 63, 65,
            66, 67, 80, 81, 82
        )
        val snow = code in listOf(71, 73, 75, 77, 85, 86)
        val storm = code in listOf(95, 96, 99)

        // Sun and rounded rays.
        if (clear || partlyCloudy) {
            val sunCenter = if (clear) point(50f, 46f) else point(32f, 31f)
            val radius = w * if (clear) 0.24f else 0.17f

            for (index in 0 until 8) {
                val angle = Math.PI * index / 4.0
                val inner = radius + w * 0.055f
                val outer = radius + w * 0.11f
                drawLine(
                    color = sunshine,
                    start = Offset(
                        sunCenter.x + kotlin.math.cos(angle).toFloat() * inner,
                        sunCenter.y + kotlin.math.sin(angle).toFloat() * inner
                    ),
                    end = Offset(
                        sunCenter.x + kotlin.math.cos(angle).toFloat() * outer,
                        sunCenter.y + kotlin.math.sin(angle).toFloat() * outer
                    ),
                    strokeWidth = w * 0.045f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(sunshine, radius, sunCenter)

            if (clear) {
                drawCircle(cocoa, w * 0.018f, point(42f, 44f))
                drawCircle(cocoa, w * 0.018f, point(58f, 44f))
                drawArc(
                    color = cocoa,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = point(44f, 48f),
                    size = androidx.compose.ui.geometry.Size(
                        w * 0.12f, h * 0.08f
                    ),
                    style = Stroke(w * 0.02f, cap = StrokeCap.Round)
                )
            }
        }

        // Soft cloud with a little face.
        if (!clear) {
            drawRoundRect(
                color = cloudShadow,
                topLeft = point(17f, 46f),
                size = androidx.compose.ui.geometry.Size(
                    w * 0.70f, h * 0.25f
                ),
                cornerRadius = CornerRadius(w * 0.13f)
            )
            drawCircle(cloudCream, w * 0.18f, point(34f, 48f))
            drawCircle(cloudCream, w * 0.23f, point(54f, 39f))
            drawCircle(cloudCream, w * 0.17f, point(74f, 49f))
            drawRoundRect(
                color = cloudCream,
                topLeft = point(17f, 45f),
                size = androidx.compose.ui.geometry.Size(
                    w * 0.70f, h * 0.23f
                ),
                cornerRadius = CornerRadius(w * 0.12f)
            )
            drawCircle(cocoa, w * 0.016f, point(44f, 52f))
            drawCircle(cocoa, w * 0.016f, point(62f, 52f))
            drawArc(
                color = cocoa,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = point(48f, 55f),
                size = androidx.compose.ui.geometry.Size(
                    w * 0.10f, h * 0.06f
                ),
                style = Stroke(w * 0.018f, cap = StrokeCap.Round)
            )
        }

        if (rain) {
            listOf(34f, 54f, 74f).forEach { x ->
                drawLine(
                    color = rainBlue,
                    start = point(x, 77f),
                    end = point(x - 4f, 89f),
                    strokeWidth = w * 0.04f,
                    cap = StrokeCap.Round
                )
            }
        }

        if (snow) {
            listOf(34f, 54f, 74f).forEach { x ->
                drawCircle(rainBlue, w * 0.035f, point(x, 83f))
            }
        }

        if (fog) {
            listOf(78f, 88f).forEach { y ->
                drawLine(
                    color = cloudShadow,
                    start = point(27f, y),
                    end = point(77f, y),
                    strokeWidth = w * 0.04f,
                    cap = StrokeCap.Round
                )
            }
        }

        if (storm) {
            val lightning = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.55f, h * 0.71f)
                lineTo(w * 0.43f, h * 0.84f)
                lineTo(w * 0.54f, h * 0.84f)
                lineTo(w * 0.47f, h * 0.98f)
                lineTo(w * 0.69f, h * 0.79f)
                lineTo(w * 0.57f, h * 0.79f)
                close()
            }
            drawPath(lightning, sunshine)
        }
    }
}