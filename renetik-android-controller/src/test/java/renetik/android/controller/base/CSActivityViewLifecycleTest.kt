package renetik.android.controller.base

import android.os.Bundle
import android.widget.FrameLayout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import renetik.android.core.lang.CSLayoutRes.Companion.layout
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.TestApplication
import renetik.android.ui.R.layout.cs_frame_match

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CSActivityViewLifecycleTest {

    private class Activity : CSViewActivity<CSActivityView<FrameLayout>>() {
        override fun createView() = ActivityView(this)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView()
        }
    }

    private class ActivityView(activity: CSViewActivity<CSActivityView<FrameLayout>>) :
        CSActivityView<FrameLayout>(activity, layout(cs_frame_match))

    private val activityController: ActivityController<Activity> by lazy {
        buildActivity(Activity::class.java).setup()
    }
    private val activityView: CSActivityView<FrameLayout> by lazy {
        activityController.get().activityView
    }

    @Test
    fun resumePauseLifecycle() {
        assertTrue(activityView.isResume)
        assertFalse(activityView.isPaused)

        activityController.pause()
        assertTrue(activityView.isPaused)

        activityController.resume()
        assertTrue(activityView.isResume)
    }

    @Test
    fun onResumeOnPauseListeners() {
        var resumed = 0
        var paused = 0
        activityView.onResume { resumed += 1 }
        activityView.onPause { paused += 1 }

        activityController.pause()
        assert(expected = 1, actual = paused)
        activityController.resume()
        assert(expected = 1, actual = resumed)
    }

    @Test
    fun destroyDestructsActivityView() {
        var destructed = 0
        activityView.eventDestruct.listen { destructed += 1 }
        activityController.pause().stop().destroy()
        assertTrue(activityView.isDestructed)
        assert(expected = 1, actual = destructed)
    }
}
