package au.edu.jcu.assessment.utilityapp

import android.content.Context

class PhotoWalkRepository(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("photo_walk", Context.MODE_PRIVATE)

    fun getGear(): PhotographyGear =
        PhotographyGear.fromStored(preferences.getString("gear", null))

    fun saveGear(gear: PhotographyGear) {
        preferences.edit().putString("gear", gear.name).apply()
    }

    fun getTheme(): PhotoTheme = PhotoTheme.fromStored(preferences.getString("theme", null))

    fun saveTheme(theme: PhotoTheme) {
        preferences.edit().putString("theme", theme.name).apply()
    }

    fun getCompletedTasks(contextKey: String): Set<String> =
        preferences.getStringSet("shots_$contextKey", emptySet())?.toSet() ?: emptySet()

    fun saveCompletedTasks(contextKey: String, ids: Set<String>) {
        preferences.edit().putStringSet("shots_$contextKey", ids.toSet()).apply()
    }

    fun getPackedIds(contextKey: String): Set<String> =
        preferences.getStringSet("packed_$contextKey", emptySet())?.toSet() ?: emptySet()

    fun savePackedIds(contextKey: String, ids: Set<String>) {
        // Copy the set: SharedPreferences sets must not be mutated after saving.
        preferences.edit().putStringSet("packed_$contextKey", ids.toSet()).apply()
    }
}
