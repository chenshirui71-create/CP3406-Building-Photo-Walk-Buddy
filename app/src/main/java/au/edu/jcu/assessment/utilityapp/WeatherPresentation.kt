package au.edu.jcu.assessment.utilityapp

import java.util.Locale

// Open-Meteo returns Celsius; changing units does not need another API request.
fun formatTemperature(celsius: Double, fahrenheit: Boolean): String {
    val value = if (fahrenheit) celsius * 9.0 / 5.0 + 32 else celsius
    val unit = if (fahrenheit) "°F" else "°C"
    return String.format(Locale.US, "%.1f%s", value, unit)
}

fun isUvAlert(uvMax: Double?, threshold: Float): Boolean =
    uvMax != null && uvMax >= threshold.toDouble()

fun weatherDescription(code: Int): String = when (code) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61, 63, 65 -> "Rain"
    66, 67 -> "Freezing rain"
    71, 73, 75, 77 -> "Snow"
    80, 81, 82 -> "Rain showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm with hail"
    else -> "Unknown weather"
}