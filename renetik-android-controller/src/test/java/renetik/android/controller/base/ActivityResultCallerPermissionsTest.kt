package renetik.android.controller.base

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityResultCallerPermissionsTest {

    @Test
    fun cancelledRequestIsNotGranted() {
        assertFalse(emptyMap<String, Boolean>().isPermissionsGranted())
        assertFalse(emptyMap<String, Boolean>().isPermissionsGranted(requireAll = false))
    }

    @Test
    fun partialGrantDependsOnRequireAll() {
        val results = mapOf("fine" to false, "coarse" to true)
        assertFalse(results.isPermissionsGranted())
        assertTrue(results.isPermissionsGranted(requireAll = false))
        assertTrue(mapOf("fine" to true, "coarse" to true).isPermissionsGranted())
    }
}
