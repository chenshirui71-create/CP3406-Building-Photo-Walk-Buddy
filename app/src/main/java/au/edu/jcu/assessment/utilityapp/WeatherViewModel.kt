package au.edu.jcu.assessment.utilityapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class WeatherUiState(
    val city: String = "",
    val loading: Boolean = false,
    val weather: WeatherInfo? = null,
    val error: String? = null
)

class WeatherViewModel(
    private val repository: WeatherRepository
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
                // A cancelled city request must not overwrite the latest result.
                ensureActive()
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

