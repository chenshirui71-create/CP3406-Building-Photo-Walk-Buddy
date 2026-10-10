package au.edu.jcu.assessment.utilityapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherLogicTest {
    @Test
    fun temperatureConversionIncludesUnitAndHandlesNegativeValues() {
        assertEquals("0.0°C", formatTemperature(0.0, false))
        assertEquals("32.0°F", formatTemperature(0.0, true))
        assertEquals("71.1°F", formatTemperature(21.7, true))
        assertEquals("-40.0°F", formatTemperature(-40.0, true))
    }

    @Test
    fun uvAlertIncludesExactThresholdAndExcludesUnavailableForecast() {
        assertFalse(isUvAlert(null, 6f))
        assertFalse(isUvAlert(5.9, 6f))
        assertTrue(isUvAlert(6.0, 6f))
        assertTrue(isUvAlert(6.8, 6f))
    }
}
