package au.edu.jcu.assessment.utilityapp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoWalkPanel(
    plan: PhotoWalkPlan,
    gear: PhotographyGear,
    packedIds: Set<String>,
    inspiration: PhotoInspiration,
    theme: PhotoTheme,
    completedTasks: Set<String>,
    onThemeChange: (PhotoTheme) -> Unit,
    onTaskChange: (String, Boolean) -> Unit,
    onResetTasks: () -> Unit,
    onGearChange: (PhotographyGear) -> Unit,
    onPackedChange: (String, Boolean) -> Unit,
    onReset: () -> Unit,
    bounded: Boolean = false,
    modifier: Modifier = Modifier
) {
    var captureTab by rememberSaveable { mutableStateOf(false) }
    var personalise by rememberSaveable { mutableStateOf(false) }
    var showNotes by rememberSaveable { mutableStateOf(false) }
    var showPlanB by rememberSaveable { mutableStateOf(false) }
    var expandedTask by rememberSaveable(theme, inspiration.title) { mutableStateOf<String?>(null) }
    var expandedItem by rememberSaveable(gear) { mutableStateOf<String?>(null) }
    val prepareScroll = rememberScrollState()
    val captureScroll = rememberScrollState()
    val packed = packedItemCount(plan.items, packedIds)
    val taken = photoTaskCount(inspiration.tasks, completedTasks)

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(inspiration.title, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${gear.label} · ${theme.label}", style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        TextButton(onClick = { personalise = true }) { Text("Personalise") }
                    }
                }
                CameraSticker(Modifier.size(54.dp))
            }
        }
        ChoiceChips(listOf("Prepare ($packed/${plan.items.size})", "Capture ($taken/3)"),
            if (captureTab) 1 else 0) { captureTab = it == 1 }

        val tasksModifier = if (bounded) Modifier.weight(1f).fillMaxWidth() else Modifier.fillMaxWidth()
        Column(tasksModifier.verticalScroll(if (captureTab) captureScroll else prepareScroll),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!captureTab) {
                OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(12.dp).animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Pack for your photo walk", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text(if (packed == plan.items.size) "All listed items packed" else "$packed of ${plan.items.size} packed",
                            style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { packed.toFloat() / plan.items.size.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth())
                        plan.items.forEach { item ->
                            val hint = when (item.id) {
                                "hat", "sun_protection" -> "Today's maximum UV meets your reminder threshold."
                                "rain_gear", "device_cover" -> "Today's maximum rain chance is at least 50%."
                                else -> "Part of your ${gear.label.lowercase()} photography kit."
                            }
                            CompactTaskRow(item.label, hint, item.id in packedIds,
                                expandedItem == item.id,
                                { expandedItem = if (expandedItem == item.id) null else item.id },
                                { onPackedChange(item.id, it) })
                        }
                        TextButton(onClick = onReset, enabled = packedIds.isNotEmpty()) { Text("Empty this bag") }
                    }
                }
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(horizontal = 12.dp).animateContentSize()) {
                        TextButton(onClick = { showPlanB = !showPlanB }) {
                            Text(if (showPlanB) "Plan B · Hide" else "Plan B · View backup idea")
                        }
                        if (showPlanB) Text(plan.planB, modifier = Modifier.padding(bottom = 12.dp),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                TextButton(onClick = { showNotes = !showNotes }) {
                    Text(if (showNotes) "Hide reminder notes" else "How weather reminders work")
                }
                if (showNotes) plan.notes.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text("Packing ticks are saved for this city, forecast day and gear.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(inspiration.reason, style = MaterialTheme.typography.bodySmall)
                        Text(if (gear == PhotographyGear.PHONE)
                            "Phone tip: tap your subject to focus."
                            else "Camera tip: check focus and exposure.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(12.dp).animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your 3-shot challenge", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text(if (taken == 3) "Three moments captured. Nice work!" else "$taken of 3 captured · Tap Tip for guidance",
                            style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { taken / 3f }, modifier = Modifier.fillMaxWidth())
                        inspiration.tasks.forEachIndexed { index, task ->
                            CompactTaskRow(task.title, task.hint, task.id in completedTasks,
                                expandedTask == task.id,
                                { expandedTask = if (expandedTask == task.id) null else task.id },
                                { onTaskChange(task.id, it) }, index + 1)
                        }
                        TextButton(onClick = onResetTasks, enabled = completedTasks.isNotEmpty()) {
                            Text("Restart this challenge")
                        }
                    }
                }
                Text("Tick after taking a photo. This app does not store photos.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    if (personalise) {
        ModalBottomSheet(onDismissRequest = { personalise = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Make this walk yours", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text("Choose your gear and a photo theme.", style = MaterialTheme.typography.bodyMedium)
                Text("Photography gear", style = MaterialTheme.typography.titleSmall)
                ChoiceChips(PhotographyGear.entries.map { it.label }, gear.ordinal) {
                    onGearChange(PhotographyGear.entries[it])
                }
                Text("Photo theme", style = MaterialTheme.typography.titleSmall)
                ChoiceChips(PhotoTheme.entries.map { it.label }, theme.ordinal) {
                    onThemeChange(PhotoTheme.entries[it])
                }
                Text(when (theme) {
                    PhotoTheme.STREET -> "Street: lines, colours and everyday architecture."
                    PhotoTheme.NATURE -> "Nature: plants, shapes and textures."
                    PhotoTheme.DETAILS -> "Details: close-up textures and small still-life scenes."
                }, style = MaterialTheme.typography.bodyMedium)
                Text("Choices save automatically. Each theme has its own photo progress.",
                    style = MaterialTheme.typography.bodySmall)
                Button(onClick = { personalise = false }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CompactTaskRow(
    title: String, hint: String, checked: Boolean, expanded: Boolean,
    onExpand: () -> Unit, onChange: (Boolean) -> Unit, number: Int? = null
) {
    Column(Modifier.fillMaxWidth().background(
        if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)
    ).animateContentSize()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (number != null) {
                Box(Modifier.size(24.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Text(number.toString(), style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.width(6.dp))
            }
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onExpand) { Text(if (expanded) "Hide" else "Tip") }
            Checkbox(checked = checked, onCheckedChange = onChange)
        }
        if (expanded) Text(hint, modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChoiceChips(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val chips: @Composable () -> Unit = {
            labels.forEachIndexed { index, label ->
                FilterChip(selected = selected == index, onClick = { onSelect(index) },
                    label = { Text(label) })
            }
        }
        if (maxWidth < 280.dp || LocalDensity.current.fontScale > 1.3f) {
            Column { chips() }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
        }
    }
}

@Composable
private fun CameraSticker(modifier: Modifier = Modifier) {
    val body = MaterialTheme.colorScheme.primary
    val light = MaterialTheme.colorScheme.surface
    val accent = MaterialTheme.colorScheme.tertiary
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(light.copy(alpha = 0.55f), radius = w * 0.47f)
        drawRoundRect(body, Offset(w * 0.13f, h * 0.30f), Size(w * 0.74f, h * 0.45f), CornerRadius(w * 0.10f))
        drawRoundRect(body, Offset(w * 0.27f, h * 0.22f), Size(w * 0.25f, h * 0.17f), CornerRadius(w * 0.04f))
        drawCircle(light, radius = w * 0.19f, center = Offset(w * 0.51f, h * 0.53f))
        drawCircle(accent, radius = w * 0.13f, center = Offset(w * 0.51f, h * 0.53f))
        drawCircle(light.copy(alpha = 0.7f), radius = w * 0.04f, center = Offset(w * 0.47f, h * 0.48f))
        drawRoundRect(light, Offset(w * 0.70f, h * 0.38f), Size(w * 0.10f, h * 0.06f), CornerRadius(w * 0.02f))
        drawLine(accent, Offset(w * 0.80f, h * 0.15f), Offset(w * 0.87f, h * 0.22f), strokeWidth = w * 0.03f)
        drawLine(accent, Offset(w * 0.80f, h * 0.22f), Offset(w * 0.87f, h * 0.15f), strokeWidth = w * 0.03f)
        drawCircle(accent, radius = w * 0.04f, center = Offset(w * 0.13f, h * 0.83f), style = Stroke(w * 0.02f))
    }
}
