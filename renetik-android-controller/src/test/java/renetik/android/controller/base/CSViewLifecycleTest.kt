package renetik.android.controller.base

import android.os.Bundle
import android.view.View
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
import renetik.android.event.lifecycle.destruct
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.TestApplication
import renetik.android.ui.R.layout.cs_frame_match

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CSViewLifecycleTest {

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
    private val parent: CSActivityView<FrameLayout> by lazy {
        activityController.get().activityView
    }

    @Test
    fun viewCreationFromInstance() {
        val view = CSView(parent, View(parent.view.context))
        assertFalse(view.isDestructed)
        assertTrue(view.view.isAttachedToWindow || view.view.parent == null)
    }

    @Test
    fun destructFiresEventDestructOnce() {
        val view = CSView(parent, View(parent.view.context))
        var destructed = 0
        view.eventDestruct.listen { destructed += 1 }
        view.destruct()
        assertTrue(view.isDestructed)
        assert(expected = 1, actual = destructed)
    }

    @Test
    fun parentDestructPropagatesToChild() {
        val view = CSView(parent, View(parent.view.context))
        var destructed = 0
        view.eventDestruct.listen { destructed += 1 }
        activityController.pause().stop().destroy()
        assertTrue(view.isDestructed)
        assert(expected = 1, actual = destructed)
    }
}
