# Weather & UV

A focused daily-life utility for CP3406 Assessment 1. It helps users check the
weather and plan time outdoors with temperature, today's maximum UV index and
maximum precipitation probability for Singapore or Brisbane.

Developed from the lecturer-provided MobileTech Utility App starter template.

## Core features

- Weather screen: current temperature and conditions, city-local weather time,
  today's forecast date, maximum UV and maximum rain probability.
- Settings screen: select Singapore or Brisbane, switch Celsius/Fahrenheit,
  and choose a UV highlight threshold from 3 to 11.
- Settings persist with SharedPreferences. The selected tab and preferences
  also use `rememberSaveable` during activity recreation.
- Refresh, loading feedback, connection/error messages and retry.
- Cream and apricot Material 3 cards with weather-dependent Canvas illustrations.
  Light/dark palettes, scrolling, width limits and stacked layouts accommodate
  smaller screens and increased text size.

## Implementation

| Part | Responsibility |
| --- | --- |
| `MainActivity.kt` | Hosts Compose and supplies dependencies at the app entry point. |
| `WeatherApp.kt` | Bottom navigation, shared settings, ViewModel factory and city loading. |
| `WeatherScreen.kt` | State-driven UI and reusable weather/forecast/advice cards. |
| `SettingsScreen.kt` | City, unit and UV controls; state changes are sent to the app. |
| `WeatherIllustration.kt` | Decorative sun, cloud, rain, fog, snow and storm drawings. |
| `WeatherViewModel.kt` | Loading/success/error state and lifecycle-bound coroutine jobs. |
| `WeatherData.kt` | Retrofit API, response models, repository and dependency container. |
| `WeatherPresentation.kt` | Temperature formatting, UV threshold comparison and condition labels. |
| `SettingsRepository.kt` | Reads and writes persistent preferences. |
| `ui/theme/` | Material 3 colour schemes and typography. |

The dependency flow is `WeatherApi -> OpenMeteoRepository -> WeatherViewModel -> UI`.
`WeatherViewModel` requires a repository through its constructor. A
`viewModelFactory` in `WeatherApp` supplies the repository given to the app by
`MainActivity`. This is manual dependency injection; no Hilt framework is needed.
The repository depends on the `WeatherApi` interface, so tests can inject a fake
API without network access.

The ViewModel cancels an earlier request when the city changes. It checks that
the coroutine is still active before publishing results, so cancelled requests
cannot overwrite newer data. Its activity-scoped instance survives rotation.
Views receive state and event callbacks rather than constructing network clients.

## Weather data

Data source: [Open-Meteo](https://open-meteo.com/), via
`https://api.open-meteo.com/v1/forecast`.

- `current`: `temperature_2m,weather_code`
- `daily`: `uv_index_max,precipitation_probability_max`
- `forecast_days`: `1`
- `temperature_unit`: `celsius`
- City-specific coordinates and time zones are used for each request.

The API always returns Celsius; Fahrenheit is calculated locally. Optional UV
or rain values remain `N/A` when absent or outside their valid range. Missing
required weather fields produce an error rather than fabricated values.

**UV is today's predicted maximum, not a live current reading.** The selected
threshold changes highlighting only. It is not a background notification.
Rain probability is the daily maximum supplied by the API, not a promise that
rain will or will not occur. The UI includes a link crediting Open-Meteo.

## Run

1. Open the project root (containing `settings.gradle.kts`) in Android Studio.
2. Use JDK 17 for Gradle and install Android SDK Platform 36.
3. Sync Gradle. The project uses Gradle 8.13, AGP 8.13.2 and Kotlin 2.0.21.
4. Run the `app` configuration on an emulator or device running API 26 or later.
5. An internet connection is needed to retrieve forecasts. No API key is required.

Use the configured local SDK path in `local.properties`; this machine-specific
file is excluded from Git. Keep the existing Gradle/JDK configuration that
already works on the development computer.

## Validation

The refactored app builds and runs successfully on the Android emulator.
Local JVM tests passed using .\gradlew.bat testDebugUnitTest.
Manual checks passed for city switching, temperature units, UV thresholds,
saved settings, network error handling and recovery, and screen rotation.

Run local JVM tests from the project terminal on Windows:

```powershell
.\gradlew.bat testDebugUnitTest
```

`WeatherLogicTest` checks temperature conversion and the exact UV alert boundary.
`WeatherRepositoryTest` checks city coordinates/time zones, unavailable optional
values, invalid optional values and missing required current-weather data.
Both test classes completed successfully.

Manual checks for the refactored version:

- Load both cities and confirm the city-local timestamps and forecast dates.
- Change units and UV threshold; confirm the main screen updates.
- Close/reopen the app and verify settings remain saved.
- Disconnect Wi-Fi and mobile data, refresh, reconnect, and retry successfully.
- Rotate the device and try a larger system font; check scrolling and controls.
- Check light and dark themes and open the data-source link.

## Limitations

The app supports two preset cities. It does not use GPS, deliver background
alerts, or save weather forecasts for offline use. A failed refresh displays
an error and retry action. Forecast values can change with each API update.

## Sources and AI acknowledgement

- Lecturer-provided CP3406 starter template (original project structure, theme
  setup, Gradle configuration and navigation concept).
- [Open-Meteo API documentation](https://open-meteo.com/en/docs).
- [Android: ViewModels with dependencies](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories).
- [Android: Compose layout basics](https://developer.android.com/develop/ui/compose/layouts/basics).
- [Android: SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences).

I used ChatGPT to support my learning of Kotlin and Compose. It helped with explanations of errors, libraries and concepts; code drafts and refactoring suggestions; weather API integration (network requests, JSON parsing and API key configuration); small UI adjustments such as alignment, spacing and colours; test ideas and example test code; Android Studio, Gradle and emulator troubleshooting; and README wording and formatting. I applied, ran and checked the suggestions in my own project, and I did the manual checking and testing myself. The overall UI design and implementation were my own work. Some suggestions were used before I fully understood them. I can explain the general purpose of Retrofit and dependency injection, but I still need references to set up Retrofit or replace a dependency on my own. A separate Declaration of AI-Generated Material must accurately record this assistance when submitting the assignment.
