package renetik.android.core.android.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CSWindowProfileTest {

    @Test
    fun portraitWindowTakesLandscapeHeightFromSmallestWidth() {
        val landscape = CSWindowProfile(widthDp = 540, heightDp = 900, smallestWidthDp = 490).landscape

        assertEquals(900, landscape.widthDp)
        assertEquals(490, landscape.heightDp)
    }

    @Test
    fun landscapeWindowStaysUnchanged() {
        val profile = CSWindowProfile(widthDp = 900, heightDp = 480, smallestWidthDp = 490)

        assertSame(profile, profile.landscape)
    }
}
