package au.edu.jcu.assessment.utilityapp

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun citiesUseTheirCoordinatesAndLocalTimeZones() {
        runBlocking {
            val api = FakeWeatherApi(sampleResponse())
            val repository = OpenMeteoRepository(api)
            val singapore = repository.getWeather("Singapore")
            assertEquals("Singapore", singapore.city)
            assertEquals(1.3521, api.latitude, 0.0001)
            assertEquals(103.8198, api.longitude, 0.0001)
            assertEquals("Asia/Singapore", api.timezone)
            repository.getWeather("Brisbane")
            assertEquals(-27.4698, api.latitude, 0.0001)
            assertEquals(153.0251, api.longitude, 0.0001)
            assertEquals("Australia/Brisbane", api.timezone)
            assertEquals("celsius", api.unit)
            assertEquals(1, api.days)
        }
    }

    @Test
    fun missingOptionalForecastsRemainUnavailableInsteadOfBecomingZero() {
        runBlocking {
            val response = sampleResponse().copy(daily = DailyWeather(time = listOf("2026-10-10")))
            val weather = OpenMeteoRepository(FakeWeatherApi(response)).getWeather("Singapore")
            assertNull(weather.uvMax)
            assertNull(weather.rainMax)
        }
    }

    @Test
    fun invalidOptionalValuesRemainUnavailable() {
        runBlocking {
            val response = sampleResponse().copy(
                daily = DailyWeather(listOf("2026-10-10"), listOf(-1.0), listOf(120))
            )
            val weather = OpenMeteoRepository(FakeWeatherApi(response)).getWeather("Singapore")
            assertNull(weather.uvMax)
            assertNull(weather.rainMax)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun missingRequiredCurrentWeatherIsRejected() {
        runBlocking {
            OpenMeteoRepository(FakeWeatherApi(sampleResponse().copy(current = null)))
                .getWeather("Singapore")
        }
    }

    private fun sampleResponse() = ForecastResponse(
        current = CurrentWeather("2026-10-10T17:00", 29.5, 2),
        daily = DailyWeather(listOf("2026-10-10"), listOf(7.2), listOf(60))
    )

    private class FakeWeatherApi(private val response: ForecastResponse) : WeatherApi {
        var latitude = 0.0
        var longitude = 0.0
        var timezone = ""
        var unit = ""
        var days = 0

        override suspend fun forecast(
            latitude: Double,
            longitude: Double,
            timezone: String,
            current: String,
            daily: String,
            days: Int,
            unit: String
        ): ForecastResponse {
            this.latitude = latitude
            this.longitude = longitude
            this.timezone = timezone
            this.unit = unit
            this.days = days
            return response
        }
    }
}
