package au.edu.jcu.assessment.utilityapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoWalkPlanTest {
    @Test
    fun phoneAndCameraHaveDifferentBasics() {
        val phone = buildPhotoWalkPlan(PhotographyGear.PHONE, 2.0, 10, 6f)
        val camera = buildPhotoWalkPlan(PhotographyGear.CAMERA, 2.0, 10, 6f)
        assertEquals(setOf("phone", "power_bank"), phone.items.map { it.id }.toSet())
        assertEquals(setOf("camera", "battery", "memory_card"), camera.items.map { it.id }.toSet())
    }

    @Test
    fun exactThresholdsAddReminders() {
        val below = buildPhotoWalkPlan(PhotographyGear.PHONE, 5.9, 49, 6f)
        val exact = buildPhotoWalkPlan(PhotographyGear.PHONE, 6.0, 50, 6f)
        assertFalse(below.items.any { it.id == "hat" || it.id == "rain_gear" })
        assertTrue(exact.items.any { it.id == "sun_protection" })
        assertTrue(exact.items.any { it.id == "device_cover" })
        assertTrue(exact.planB.contains("sheltered route"))
    }

    @Test
    fun missingAndInvalidDataStayUnknown() {
        val missing = buildPhotoWalkPlan(PhotographyGear.CAMERA, null, null, 6f)
        val invalid = buildPhotoWalkPlan(PhotographyGear.CAMERA, Double.NaN, 120, 6f)
        assertEquals(missing, invalid)
        assertTrue(missing.notes.any { it.startsWith("UV forecast unavailable") })
        assertTrue(missing.planB.startsWith("Rain data is missing"))
        assertEquals(3, missing.items.size)
    }

    @Test
    fun hiddenOldItemsDoNotInflateProgress() {
        val plan = buildPhotoWalkPlan(PhotographyGear.PHONE, 2.0, 10, 6f)
        assertEquals(1, packedItemCount(plan.items, setOf("phone", "hat", "camera")))
    }

    @Test
    fun listsAreSeparatedByCityDateAndGear() {
        val original = packingContext("Singapore", "2026-10-13", PhotographyGear.PHONE)
        assertNotEquals(original, packingContext("Brisbane", "2026-10-13", PhotographyGear.PHONE))
        assertNotEquals(original, packingContext("Singapore", "2026-10-14", PhotographyGear.PHONE))
        assertNotEquals(original, packingContext("Singapore", "2026-10-13", PhotographyGear.CAMERA))
    }

    @Test
    fun unknownStoredGearFallsBackToPhone() {
        assertEquals(PhotographyGear.PHONE, PhotographyGear.fromStored("OLD_VALUE"))
        assertEquals(PhotographyGear.CAMERA, PhotographyGear.fromStored("CAMERA"))
    }
}
