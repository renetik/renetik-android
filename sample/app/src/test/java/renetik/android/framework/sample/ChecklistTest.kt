package renetik.android.framework.sample

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.testing.context
import renetik.android.ui.picker.CSNumberPicker

@RunWith(RobolectricTestRunner::class)
@Config(application = SampleTestApplication::class)
class ChecklistTest {

    @Test
    fun allModuleChecksPass() {
        val checks = ModuleChecks(context).all
        assertEquals(14, checks.size)
        val failed = checks.filter { !it.passed }.map { it.module }
        assertTrue("Failed module checks: $failed", failed.isEmpty())
    }

    @Test
    fun checklistIsAttachedToActivityWindowAndRowsRendered() {
        val activity = buildActivity(MainActivity::class.java).setup().get()
        val mainView = activity.activityView
        val root: ViewGroup = mainView.view

        // The navigation view must be a child of the activity root frame,
        // otherwise pushed screens are never in the window (blank screen).
        assertTrue("Navigation is not attached to the activity root frame",
            (0 until root.childCount).any { root.getChildAt(it) === mainView.navigation.view })

        // Every checklist row must be reachable from the activity root view
        // tree — not merely from the (possibly detached) checklist view.
        val checklist = mainView.checklist
        assertTrue(checklist.checks.isNotEmpty())
        assertTrue(checklist.allPassed)
        checklist.checks.forEach { check ->
            val status = root.findViewWithTag<View>(ChecklistView.statusTag(check))
            assertNotNull("Row for ${check.module} not in activity view tree", status)
        }
    }

    @Test
    @Config(qualifiers = "notnight")
    fun statusBarUsesDarkIconsInLightMode() {
        val activity = buildActivity(MainActivity::class.java).setup().get()

        assertTrue(WindowCompat.getInsetsController(
            activity.window, activity.window.decorView
        ).isAppearanceLightStatusBars)
    }

    @Test
    @Config(qualifiers = "night")
    fun statusBarUsesLightIconsInDarkMode() {
        val activity = buildActivity(MainActivity::class.java).setup().get()

        assertFalse(WindowCompat.getInsetsController(
            activity.window, activity.window.decorView
        ).isAppearanceLightStatusBars)
    }

    @Test
    fun pickerUsesDarkThemeTextColors() {
        val configuration = Configuration(context.resources.configuration).apply {
            uiMode = uiMode and UI_MODE_NIGHT_MASK.inv() or UI_MODE_NIGHT_YES
        }
        val darkContext = context.createConfigurationContext(configuration).apply {
            setTheme(R.style.AppTheme)
        }
        val pickerCheck = ModuleChecks(darkContext).all
            .single { it.module == "ui-picker" }
        val picker = pickerCheck.demoView!!.invoke(darkContext) as CSNumberPicker
        val colors = darkContext.obtainStyledAttributes(intArrayOf(
            android.R.attr.textColorSecondary,
            android.R.attr.textColorPrimary,
        ))

        assertEquals(colors.getColor(0, 0), picker.textColor)
        assertEquals(colors.getColor(1, 0), picker.selectedTextColor)
        assertEquals(colors.getColor(1, 0), picker.dividerColor)
        colors.recycle()
    }
}
