package au.edu.jcu.assessment.utilityapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlin.math.roundToInt

@Composable
fun WeatherApp(
    settingsRepository: SettingsRepository,
    weatherRepository: WeatherRepository,
    photoWalkRepository: PhotoWalkRepository
) {
    var selectedTab by rememberSaveable { mutableStateOf("Weather") }
    var city by rememberSaveable { mutableStateOf(settingsRepository.getCity()) }
    var fahrenheit by rememberSaveable {
        mutableStateOf(settingsRepository.getFahrenheit())
    }
    var uvThreshold by rememberSaveable {
        mutableStateOf(settingsRepository.getUvThreshold())
    }
    var gear by remember { mutableStateOf(photoWalkRepository.getGear()) }
    var photoTheme by remember { mutableStateOf(photoWalkRepository.getTheme()) }

    // Constructor injection keeps the ViewModel independent of the real API.
    val factory = remember(weatherRepository) {
        viewModelFactory { initializer { WeatherViewModel(weatherRepository) } }
    }
    val weatherViewModel: WeatherViewModel = viewModel(factory = factory)
    LaunchedEffect(city) { weatherViewModel.load(city) }

    val forecastDate = weatherViewModel.uiState.weather
        ?.takeIf { it.city == city }?.forecastDate
    val contextKey = forecastDate?.let { packingContext(city, it, gear) }
    // Persistent storage is the source when the city, day or gear changes.
    var packedIds by remember(contextKey) {
        mutableStateOf(contextKey?.let(photoWalkRepository::getPackedIds) ?: emptySet())
    }
    val taskKey = forecastDate?.let { photoTaskContext(city, it, gear, photoTheme) }
    var completedTasks by remember(taskKey) {
        mutableStateOf(taskKey?.let(photoWalkRepository::getCompletedTasks) ?: emptySet())
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "Weather",
                    onClick = { selectedTab = "Weather" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Photo walk") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            // Keep cards readable on tablets as well as phones.
            val screenModifier = Modifier.widthIn(max = 640.dp).fillMaxSize()
            if (selectedTab == "Weather") {
                WeatherScreen(
                    state = weatherViewModel.uiState,
                    city = city,
                    fahrenheit = fahrenheit,
                    uvThreshold = uvThreshold,
                    onRefresh = { weatherViewModel.load(city, force = true) },
                    gear = gear,
                    packedIds = packedIds,
                    photoTheme = photoTheme,
                    completedTasks = completedTasks,
                    onThemeChange = {
                        photoTheme = it
                        photoWalkRepository.saveTheme(it)
                    },
                    onTaskChange = { id, checked ->
                        taskKey?.let { key ->
                            completedTasks = if (checked) completedTasks + id else completedTasks - id
                            photoWalkRepository.saveCompletedTasks(key, completedTasks)
                        }
                    },
                    onResetTasks = {
                        taskKey?.let { key ->
                            completedTasks = emptySet()
                            photoWalkRepository.saveCompletedTasks(key, completedTasks)
                        }
                    },
                    onGearChange = {
                        gear = it
                        photoWalkRepository.saveGear(it)
                    },
                    onPackedChange = { id, checked ->
                        contextKey?.let { key ->
                            packedIds = if (checked) packedIds + id else packedIds - id
                            photoWalkRepository.savePackedIds(key, packedIds)
                        }
                    },
                    onResetPacking = {
                        contextKey?.let { key ->
                            packedIds = emptySet()
                            photoWalkRepository.savePackedIds(key, packedIds)
                        }
                    },
                    modifier = screenModifier
                )
            } else {
                SettingsScreen(
                    city = city,
                    onCityChange = {
                        city = it
                        settingsRepository.saveCity(it)
                    },
                    fahrenheit = fahrenheit,
                    onUnitChange = {
                        fahrenheit = it
                        settingsRepository.saveFahrenheit(it)
                    },
                    uvThreshold = uvThreshold,
                    onThresholdChange = {
                        val rounded = it.roundToInt().toFloat()
                        if (uvThreshold != rounded) {
                            uvThreshold = rounded
                            settingsRepository.saveUvThreshold(rounded.toInt())
                        }
                    },
                    modifier = screenModifier
                )
            }
        }
    }
}
