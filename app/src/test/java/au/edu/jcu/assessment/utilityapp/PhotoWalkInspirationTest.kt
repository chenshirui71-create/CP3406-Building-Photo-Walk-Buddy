package au.edu.jcu.assessment.utilityapp

import org.junit.Assert.*
import org.junit.Test

class PhotoWalkInspirationTest {
    @Test
    fun rainThresholdChangesStreetIdeas() {
        val below = buildPhotoInspiration(PhotoTheme.STREET, 0, 49)
        val exact = buildPhotoInspiration(PhotoTheme.STREET, 0, 50)
        assertTrue(below.tasks.first().title.contains("leading line"))
        assertTrue(exact.tasks.first().title.contains("window reflection"))
        assertTrue(exact.reason.contains("not a time-specific forecast"))
        assertNotEquals(below.tasks.first().id, exact.tasks.first().id)
    }

    @Test
    fun currentRainOverridesLowDailyRainProbability() {
        val result = buildPhotoInspiration(PhotoTheme.STREET, 63, 20)
        assertEquals("A sheltered photo story", result.title)
        assertTrue(result.reason.contains("Current weather reports precipitation"))
    }

    @Test
    fun thunderstormAlwaysUsesIndoorIdeasAndPlanB() {
        val result = buildPhotoInspiration(PhotoTheme.NATURE, 95, null)
        assertEquals("An indoor photo day", result.title)
        assertTrue(result.reason.contains("thunderstorm"))
        val plan = buildPhotoWalkPlan(PhotographyGear.PHONE, null, 0, 6f, 95)
        assertTrue(plan.planB.startsWith("Choose an indoor"))
    }

    @Test
    fun missingRainDoesNotBecomeZero() {
        val missing = buildPhotoInspiration(PhotoTheme.STREET, 3, null)
        val invalid = buildPhotoInspiration(PhotoTheme.STREET, 3, 120)
        assertEquals(missing, invalid)
        assertTrue(missing.reason.contains("unavailable"))
    }

    @Test
    fun themesHaveThreeUniqueTasksAndSeparateProgress() {
        PhotoTheme.entries.forEach { theme ->
            val tasks = buildPhotoInspiration(theme, 0, 10).tasks
            assertEquals(3, tasks.size)
            assertEquals(3, tasks.map { it.id }.toSet().size)
            assertEquals(1, photoTaskCount(tasks, setOf(tasks[0].id, "unrelated")))
        }
        val street = photoTaskContext("Singapore", "2026-10-13", PhotographyGear.PHONE, PhotoTheme.STREET)
        val nature = photoTaskContext("Singapore", "2026-10-13", PhotographyGear.PHONE, PhotoTheme.NATURE)
        assertNotEquals(street, nature)
    }

    @Test
    fun invalidThemeFallsBackToStreet() {
        assertEquals(PhotoTheme.STREET, PhotoTheme.fromStored("REMOVED"))
        assertEquals(PhotoTheme.DETAILS, PhotoTheme.fromStored("DETAILS"))
    }
}
