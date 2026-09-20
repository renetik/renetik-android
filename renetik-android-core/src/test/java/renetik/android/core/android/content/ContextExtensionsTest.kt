package renetik.android.core.android.content

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.CATEGORY_DEFAULT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.ContextThemeWrapper
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import renetik.android.testing.context

@RunWith(RobolectricTestRunner::class)
class ContextExtensionsTest {

    @Test
    fun resourcesDimensionsAssetsAndFormatting() {
        val stringId = resource("test_formatted_string", "string")
        val stringsId = resource("test_strings", "array")
        val intsId = resource("test_ints", "array")
        val dimenId = resource("test_dimension", "dimen")

        assertEquals("Hello [B]Bold[/B]", context.string(stringId))
        assertEquals("Hello Bold", context.formatted(stringId).toString())
        assertEquals(listOf("one", "two"), context.strings(stringsId))
        assertEquals(listOf(3, 5, 8), context.ints(intsId))
        assertEquals(context.resources.getDimension(dimenId), context.dimension(dimenId))
        assertEquals(context.resources.getDimensionPixelSize(dimenId), context.dimensionPx(dimenId))
        assertEquals("Hello, Foo!\n", context.assetsReadText("foo.txt"))
    }

    @Test
    fun attributeColorsResolveLiteralAndColorStateListValues() {
        val themedContext = ContextThemeWrapper(
            context, resource("TestColorTheme", "style")
        )
        val literalAttribute = resource("test_literal_color_attribute", "attr")
        val stateListAttribute = resource("test_state_list_color_attribute", "attr")

        assertEquals(Color.rgb(0x12, 0x34, 0x56),
            themedContext.attributeColor(literalAttribute))
        assertEquals(Color.rgb(0x45, 0x67, 0x89),
            themedContext.attributeColor(stateListAttribute))
        assertEquals(Color.rgb(0x45, 0x67, 0x89),
            themedContext.attributeColorOrNull(stateListAttribute))
    }

    @Test
    fun temporaryFilesAndFoldersAreCreatedInCache() {
        val file = context.temporaryFile("tmp")
        val folder = context.temporaryFolder()

        assertTrue(file.exists())
        assertTrue(file.name.endsWith(".tmp"))
        assertEquals(context.cacheDir, file.parentFile)
        assertTrue(folder.exists())
        assertTrue(folder.isDirectory)
        assertEquals(context.cacheDir, folder.parentFile)
    }

    @Test
    fun intentConstructorsAndStringExposeActionTypeCategoryAndExtras() {
        val classIntent = Intent(context, SampleActivity::class, "sample.action")
        val reifiedIntent = Intent<SampleActivity>(context, "reified.action")
        val typedIntent = Intent(ACTION_SEND, "text/plain")
        val categorizedIntent = Intent(ACTION_SEND, CATEGORY_DEFAULT, "text/plain")
            .putExtra(EXTRA_TEXT, "hello")

        assertEquals(SampleActivity::class.java.name, classIntent.component!!.className)
        assertEquals("sample.action", classIntent.action)
        assertEquals(SampleActivity::class.java.name, reifiedIntent.component!!.className)
        assertEquals("reified.action", reifiedIntent.action)
        assertEquals("text/plain", typedIntent.type)
        assertTrue(categorizedIntent.categories!!.contains(CATEGORY_DEFAULT))
        assertTrue(categorizedIntent.asString.contains("$EXTRA_TEXT hello (java.lang.String)"))
    }

    @Test
    fun startActivityIfAvailableUsesLaunchResultInsteadOfPackageQuery() {
        val intent = Intent("test.action")
        val availableContext = RecordingContext(context)
        var unavailableException: ActivityNotFoundException? = null

        assertTrue(availableContext.startActivityIfAvailable(intent))
        assertEquals(intent, availableContext.startedIntent)
        assertFalse(UnavailableContext(context).startActivityIfAvailable(intent) {
            unavailableException = it
        })
        assertTrue(unavailableException != null)
        assertFalse(RestrictedContext(context).startActivityIfAvailable(intent))
    }

    @Test
    fun startApplicationTargetsPackageWithoutQueryingInstalledApplications() {
        val recordingContext = RecordingContext(context)

        recordingContext.startApplication("test.package")

        assertEquals("test.package", recordingContext.startedIntent!!.getPackage())
        assertEquals(Intent.ACTION_MAIN, recordingContext.startedIntent!!.action)
        assertTrue(recordingContext.startedIntent!!.categories!!.contains(Intent.CATEGORY_LAUNCHER))
    }

    @Test
    fun grantUriPermissionsUsesClipDataAndRequestedFlags() {
        val uri = Uri.parse("content://test/photo")
        val intent = Intent().grantUriPermissions(uri, write = true)

        assertEquals(uri, intent.clipData!!.getItemAt(0).uri)
        assertTrue(intent.flags and FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertTrue(intent.flags and FLAG_GRANT_WRITE_URI_PERMISSION != 0)
    }

    @Test
    fun permissionsCanBeGrantedAndDeniedByRobolectric() {
        val application = context.applicationContext as Application

        shadowOf(application).denyPermissions(CAMERA)
        assertFalse(context.isPermissionGranted(CAMERA))
        assertFalse(context.isPermissionsGranted(CAMERA))
        assertArrayEquals(arrayOf(CAMERA), context.getDeniedPermissions(listOf(CAMERA)))

        shadowOf(application).grantPermissions(CAMERA)
        assertTrue(context.isPermissionGranted(CAMERA))
        assertTrue(context.isPermissionsGranted(listOf(CAMERA)))
        assertArrayEquals(emptyArray<String>(), context.getDeniedPermissions(listOf(CAMERA)))
    }

    @Test
    fun clearDrawableCopiesBounds() {
        val bounds = Rect(1, 2, 11, 12)
        val drawable = ColorDrawable(Color.RED).also { it.bounds = bounds }

        assertEquals(bounds, drawable.createClear().bounds)
        assertEquals(bounds, bounds.createClearDrawable().bounds)
    }

    private fun resource(name: String, type: String): Int =
        context.resources.getIdentifier(name, type, context.packageName).also {
            assertTrue("$type/$name should exist", it != 0)
        }

    private class SampleActivity : Activity()

    private class RecordingContext(base: Context) : ContextWrapper(base) {
        var startedIntent: Intent? = null

        override fun startActivity(intent: Intent) {
            startedIntent = intent
        }
    }

    private class UnavailableContext(base: Context) : ContextWrapper(base) {
        override fun startActivity(intent: Intent) = throw ActivityNotFoundException()
    }

    private class RestrictedContext(base: Context) : ContextWrapper(base) {
        override fun startActivity(intent: Intent) = throw SecurityException()
    }
}
