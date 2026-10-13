package au.edu.jcu.assessment.utilityapp

enum class PhotographyGear(val label: String) {
    PHONE("Phone"), CAMERA("Camera");

    companion object {
        fun fromStored(value: String?): PhotographyGear =
            entries.firstOrNull { it.name == value } ?: PHONE
    }
}

data class PackingItem(val id: String, val label: String)

data class PhotoWalkPlan(
    val items: List<PackingItem>,
    val notes: List<String>,
    val planB: String
)

// This is a packing reminder rule, not a guarantee that rain will occur.
const val PHOTO_WALK_RAIN_THRESHOLD = 50

fun buildPhotoWalkPlan(
    gear: PhotographyGear,
    uvMax: Double?,
    rainMax: Int?,
    uvThreshold: Float,
    weatherCode: Int? = null
): PhotoWalkPlan {
    val uv = uvMax?.takeIf { it.isFinite() && it >= 0.0 }
    val rain = rainMax?.takeIf { it in 0..100 }
    val items = mutableListOf<PackingItem>()
    if (gear == PhotographyGear.PHONE) {
        items += PackingItem("phone", "Phone")
        items += PackingItem("power_bank", "Charging cable / power bank")
    } else {
        items += PackingItem("camera", "Camera")
        items += PackingItem("battery", "Charged battery / spare battery")
        items += PackingItem("memory_card", "Memory card")
    }
    if (uv != null && uv >= uvThreshold) {
        items += PackingItem("hat", "Hat")
        items += PackingItem("sun_protection", "Sun protection")
    }
    if (rain != null && rain >= PHOTO_WALK_RAIN_THRESHOLD) {
        items += PackingItem("rain_gear", "Rain gear")
        items += PackingItem("device_cover", "Protective bag for your equipment")
    }
    val notes = buildList {
        if (uv == null) add("UV forecast unavailable: check UV conditions before leaving.")
        if (rain == null) add("Rain forecast unavailable: check conditions before leaving.")
        add("Daily maximum forecasts do not describe a specific walking time.")
        add("Your UV threshold controls reminders; it is not a safety rating.")
    }
    val planB = when {
        weatherCode != null && weatherCode in 95..99 ->
            "Choose an indoor photo activity while thunderstorm conditions are reported. Check local weather updates before going out."
        rain == null -> "Rain data is missing. Keep an indoor photo option and check the forecast before leaving."
        rain >= PHOTO_WALK_RAIN_THRESHOLD -> "Consider an indoor photo stop or a sheltered route if rain develops. Check opening times and photography rules yourself."
        else -> "Keep an indoor photo stop as a backup. A lower rain forecast does not guarantee a dry walk."
    }
    return PhotoWalkPlan(items, notes, planB)
}

// Separate lists prevent yesterday's or another city's packing from appearing complete.
fun packingContext(city: String, forecastDate: String, gear: PhotographyGear): String =
    "$city|$forecastDate|${gear.name}"

fun packedItemCount(items: List<PackingItem>, packedIds: Set<String>): Int =
    items.count { it.id in packedIds }
