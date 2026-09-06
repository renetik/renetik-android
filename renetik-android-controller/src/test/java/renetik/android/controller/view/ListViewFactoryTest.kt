package renetik.android.controller.view

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import renetik.android.controller.base.CSActivityView
import renetik.android.controller.base.CSView
import renetik.android.controller.base.CSViewActivity
import renetik.android.core.lang.CSLayoutRes.Companion.layout
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire
import renetik.android.event.lifecycle.destruct
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.TestApplication
import renetik.android.ui.R.layout.cs_frame_match

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class ListViewFactoryTest {

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

    private fun itemView() = CSView(parent, View(parent.view.context))

    @Test
    fun createsViewForEachModelAndForAddedOnes() {
        val eventAdded = event<String>()
        val views = mutableListOf<CSView<View>>()
        views.viewFactory(parent, listOf("a", "b"), eventAdded, content = parent.view) {
            itemView()
        }
        assert(expected = 2, actual = views.size)
        assert(expected = 2, actual = parent.view.childCount)

        eventAdded.fire("c")
        assert(expected = 3, actual = views.size)
        assert(expected = 3, actual = parent.view.childCount)
    }

    @Test
    fun destructedViewIsRemovedFromContentAndList()  {
        val eventAdded = event<String>()
        val views = mutableListOf<CSView<View>>()
        views.viewFactory(parent, listOf("a", "b"), eventAdded, content = parent.view) {
            itemView()
        }
        val first = views.first()
        first.destruct()
        assertTrue(first.isDestructed)
        assert(expected = 1, actual = views.size)
        assert(expected = 1, actual = parent.view.childCount)
    }

    @Test
    fun viewGroupAddViewVariant() {
        val views = mutableListOf<CSView<View>>()
        views.addView("model", content = parent.view, create = { itemView() })
        assert(expected = 1, actual = views.size)
        assert(expected = 1, actual = parent.view.childCount)
    }
}
