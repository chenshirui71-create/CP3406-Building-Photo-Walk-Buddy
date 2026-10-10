package au.edu.jcu.assessment.utilityapp

import android.content.Context

class SettingsRepository(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("weather_settings", Context.MODE_PRIVATE)

    fun getCity(): String {
        return preferences.getString("city", "Singapore")
            ?.takeIf { it == "Singapore" || it == "Brisbane" }
            ?: "Singapore"
    }

    fun getFahrenheit(): Boolean {
        return preferences.getBoolean("fahrenheit", false)
    }

    fun getUvThreshold(): Float {
        return preferences.getInt("uv_threshold", 6)
            .coerceIn(3, 11)
            .toFloat()
    }

    fun saveCity(city: String) {
        preferences.edit().putString("city", city).apply()
    }

    fun saveFahrenheit(enabled: Boolean) {
        preferences.edit().putBoolean("fahrenheit", enabled).apply()
    }

    fun saveUvThreshold(value: Int) {
        preferences.edit()
            .putInt("uv_threshold", value.coerceIn(3, 11))
            .apply()
    }
}