package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Dependencies are supplied at the app entry point, not created by screens.
        val settingsRepository = SettingsRepository(applicationContext)
        val weatherRepository = WeatherContainer.repository
        val photoWalkRepository = PhotoWalkRepository(applicationContext)
        setContent {
            MobileTech_UtilityAppTheme {
                WeatherApp(settingsRepository, weatherRepository, photoWalkRepository)
            }
        }
    }
}
