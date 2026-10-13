package au.edu.jcu.assessment.utilityapp

enum class PhotoTheme(val label: String) {
    STREET("Street"), NATURE("Nature"), DETAILS("Details");
    companion object {
        fun fromStored(value: String?): PhotoTheme =
            entries.firstOrNull { it.name == value } ?: STREET
    }
}

data class PhotoTask(val id: String, val title: String, val hint: String)
data class PhotoInspiration(val title: String, val reason: String, val tasks: List<PhotoTask>)

fun buildPhotoInspiration(theme: PhotoTheme, weatherCode: Int, rainMax: Int?): PhotoInspiration {
    val rain = rainMax?.takeIf { it in 0..100 }
    val indoor = weatherCode in 95..99
    val covered = indoor || (rain != null && rain >= PHOTO_WALK_RAIN_THRESHOLD) ||
        weatherCode in setOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82)
    val mode = if (covered) "covered" else "open"
    fun task(id: String, title: String, hint: String) =
        PhotoTask("${theme.name}_$mode$id", title, hint)
    val tasks = when (theme) {
        PhotoTheme.STREET -> if (covered) listOf(
            task("1", "Frame a window reflection", "Shoot from inside; look for layered shapes in the glass."),
            task("2", "Find a repeating pattern", "Try indoor tiles, shelves or a corridor where photography is allowed."),
            task("3", "Capture one colour story", "Find three objects sharing a colour in the same sheltered space.")
        ) else listOf(
            task("1", "Follow a leading line", "Use a path or building edge to guide the eye through the frame."),
            task("2", "Spot a colour contrast", "Look for two contrasting colours in signs or architecture."),
            task("3", "Frame a small city detail", "Photograph a texture or shape you would usually walk past.")
        )
        PhotoTheme.NATURE -> if (covered) listOf(
            task("1", "Look closely at a leaf", "Use an indoor plant; focus on the veins and shapes."),
            task("2", "Frame nature through glass", "Photograph the view from indoors without stepping into the rain."),
            task("3", "Make a tiny still life", "Arrange a plant or natural object near a window.")
        ) else listOf(
            task("1", "Find a leaf pattern", "Fill the frame with repeating shapes without disturbing plants."),
            task("2", "Frame a tree silhouette", "Try a clear outline against the sky; avoid pointing at the sun."),
            task("3", "Show a natural texture", "Look for bark, stones or fallen leaves along your route.")
        )
        PhotoTheme.DETAILS -> listOf(
            task("1", "Get close to a texture", "Try fabric, a bag or a small object; check that the focus is sharp."),
            task("2", "Make a simple still life", "Choose one object and remove distracting items around it."),
            task("3", "Try a different viewpoint", "Photograph the same object from above and then from its side.")
        )
    }
    val title = when {
        indoor -> "An indoor photo day"
        covered -> "A sheltered photo story"
        weatherCode == 0 || weatherCode == 1 -> "Shapes, colours & contrast"
        else -> "A little everyday photo story"
    }
    val reason = when {
        indoor -> "Current conditions report a thunderstorm. Choose indoor ideas and check local weather updates."
        rain == null -> "Rain forecast is unavailable. These are creative prompts; check conditions before heading out."
        covered && rain >= PHOTO_WALK_RAIN_THRESHOLD -> "Today's maximum rain chance is $rain%. Start with sheltered ideas; this is not a time-specific forecast."
        covered -> "Current weather reports precipitation. Start with sheltered ideas and check conditions again."
        else -> "Use these prompts for inspiration. Today's $rain% maximum rain chance does not guarantee a dry walk."
    }
    return PhotoInspiration(title, reason, tasks)
}

fun photoTaskCount(tasks: List<PhotoTask>, completed: Set<String>): Int =
    tasks.count { it.id in completed }

fun photoTaskContext(city: String, forecastDate: String, gear: PhotographyGear, theme: PhotoTheme): String =
    "${packingContext(city, forecastDate, gear)}|${theme.name}"
