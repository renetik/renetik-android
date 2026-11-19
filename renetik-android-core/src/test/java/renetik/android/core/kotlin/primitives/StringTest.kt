package renetik.android.core.kotlin.primitives

import org.junit.Assert.assertEquals
import org.junit.Test

class StringTest {
    @Test
    fun leaveEndOfLength3Last() = assertEquals("678", "12345678".leaveEndOfLength(3))

    @Test
    fun leaveEndOfLength15Last() = assertEquals("12345678", "12345678".leaveEndOfLength(15))

    /** A localized date can name a file; its separators would otherwise point into folders. */
    @Test
    fun sanitizeForFileReplacesPathSeparators() =
        assertEquals("21 10 2026 11 55 44", "21/10/2026 11:55:44".sanitizeForFile("default"))
}
