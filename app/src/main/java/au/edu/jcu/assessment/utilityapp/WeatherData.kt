package au.edu.jcu.assessment.utilityapp

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// JSON returned by Open-Meteo.
data class ForecastResponse(
    val current: CurrentWeather? = null,
    val daily: DailyWeather? = null
)

data class CurrentWeather(
    val time: String? = null,
    @SerializedName("temperature_2m")
    val temperature: Double? = null,
    @SerializedName("weather_code")
    val weatherCode: Int? = null
)

data class DailyWeather(
    val time: List<String>? = null,
    @SerializedName("uv_index_max")
    val uvMax: List<Double?>? = null,
    @SerializedName("precipitation_probability_max")
    val rainMax: List<Int?>? = null
)

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("timezone") timezone: String,
        @Query("current")
        current: String = "temperature_2m,weather_code",
        @Query("daily")
        daily: String = "uv_index_max,precipitation_probability_max",
        @Query("forecast_days") days: Int = 1,
        @Query("temperature_unit") unit: String = "celsius"
    ): ForecastResponse
}

// Data used by the screen.
data class WeatherInfo(
    val city: String,
    val temperatureC: Double,
    val weatherCode: Int,
    val uvMax: Double?,
    val rainMax: Int?,
    val forecastDate: String,
    val weatherTime: String
)

interface WeatherRepository {
    suspend fun getWeather(city: String): WeatherInfo
}

class OpenMeteoRepository(
    private val api: WeatherApi
) : WeatherRepository {
    override suspend fun getWeather(city: String): WeatherInfo {
        val response = when (city) {
            "Singapore" -> api.forecast(
                latitude = 1.3521,
                longitude = 103.8198,
                timezone = "Asia/Singapore"
            )
            "Brisbane" -> api.forecast(
                latitude = -27.4698,
                longitude = 153.0251,
                timezone = "Australia/Brisbane"
            )
            else -> error("Unsupported city.")
        }

        val current = response.current
            ?: error("Current weather is missing.")
        val daily = response.daily
            ?: error("Daily forecast is missing.")

        return WeatherInfo(
            city = city,
            temperatureC = current.temperature?.takeIf { it.isFinite() }
                ?: error("Temperature is missing."),
            weatherCode = current.weatherCode
                ?: error("Weather condition is missing."),
            uvMax = daily.uvMax?.firstOrNull()
                ?.takeIf { it.isFinite() && it >= 0.0 },
            rainMax = daily.rainMax?.firstOrNull()
                ?.takeIf { it in 0..100 },
            forecastDate = daily.time?.firstOrNull()
                ?: error("Forecast date is missing."),
            weatherTime = current.time
                ?: error("Weather time is missing.")
        )
    }
}

// Provides shared dependencies.
object WeatherContainer {
    val repository: WeatherRepository by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        val api = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)

        OpenMeteoRepository(api)
    }
}
