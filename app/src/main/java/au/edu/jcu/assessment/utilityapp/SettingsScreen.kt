package au.edu.jcu.assessment.utilityapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    city: String,
    onCityChange: (String) -> Unit,
    fahrenheit: Boolean,
    onUnitChange: (Boolean) -> Unit,
    uvThreshold: Float,
    onThresholdChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Make the forecast work for you",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Your city",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Choose the location for your forecast.")
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val options: @Composable () -> Unit = {
                        listOf("Singapore", "Brisbane").forEach { option ->
                            FilterChip(
                                selected = city == option,
                                onClick = { onCityChange(option) },
                                label = { Text(option) }
                            )
                        }
                    }
                    if (maxWidth < 260.dp || LocalDensity.current.fontScale > 1.3f) {
                        Column { options() }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { options() }
                    }
                }
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment =
                    androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Temperature unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (fahrenheit) "Fahrenheit (°F)" else "Celsius (°C)"
                    )
                }
                Switch(
                    modifier = Modifier.semantics { contentDescription = "Use Fahrenheit" },
                    checked = fahrenheit,
                    onCheckedChange = onUnitChange
                )
            }
        }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "UV alert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Highlight today's maximum UV when it meets your threshold.")
                Text(
                    uvThreshold.roundToInt().toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    modifier = Modifier.semantics { contentDescription = "UV alert threshold" },
                    value = uvThreshold,
                    onValueChange = onThresholdChange,
                    valueRange = 3f..11f,
                    steps = 7
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("3")
                    Text("11")
                }
                Text(
                    "This changes the app's highlight, not the UV forecast.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            "Your settings are saved automatically.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Cream settings", showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun SettingsScreenPreview() {
    MobileTech_UtilityAppTheme(darkTheme = false) {
        SettingsScreen("Singapore", {}, false, {}, 6f, {})
    }
}
