package au.edu.jcu.assessment.utilityapp


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
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
            temperatureC = current.temperature
                ?: error("Temperature is missing."),
            weatherCode = current.weatherCode
                ?: error("Weather condition is missing."),
            uvMax = daily.uvMax?.firstOrNull(),
            rainMax = daily.rainMax?.firstOrNull(),
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

data class WeatherUiState(
    val city: String = "",
    val loading: Boolean = false,
    val weather: WeatherInfo? = null,
    val error: String? = null
)

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherContainer.repository
) : ViewModel() {
    var uiState by mutableStateOf(WeatherUiState())
        private set

    private var request: Job? = null

    fun load(city: String, force: Boolean = false) {
        if (!force && uiState.city == city &&
            (uiState.loading || uiState.weather != null)
        ) {
            return
        }

        request?.cancel()
        uiState = WeatherUiState(city = city, loading = true)

        request = viewModelScope.launch {
            try {
                val weather = repository.getWeather(city)
                uiState = WeatherUiState(city = city, weather = weather)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = when (e) {
                    is HttpException ->
                        "Weather service returned error ${e.code()}. Try again."
                    is IOException ->
                        "Could not connect. Check your internet and try again."
                    else ->
                        "Could not read the weather data. Try again."
                }
                uiState = WeatherUiState(city = city, error = message)
            }
        }
    }
}

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