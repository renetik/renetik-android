package renetik.android.controller.navigation

import android.os.Bundle
import android.widget.FrameLayout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import renetik.android.controller.base.CSActivityView
import renetik.android.controller.base.CSViewActivity
import renetik.android.controller.base.isPaused
import renetik.android.controller.base.push
import renetik.android.core.lang.CSLayoutRes.Companion.layout
import renetik.android.core.lang.value.isTrue
import renetik.android.testing.TestApplication
import renetik.android.ui.R.layout.cs_frame_match

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CSNavigationViewTest {

    private class Activity : CSViewActivity<CSActivityView<FrameLayout>>() {
        override fun createView() = ActivityView(this)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView()
        }
    }

    class ActivityView(activity: CSViewActivity<CSActivityView<FrameLayout>>) :
        CSActivityView<FrameLayout>(activity, layout(cs_frame_match)) {
        override var navigation: CSNavigationView? = CSNavigationView(this)
    }

    private val activityController: ActivityController<Activity> by lazy {
        buildActivity(Activity::class.java).setup()
    }
    private val navigation: CSNavigationView by lazy {
        activityController.get().activityView.navigation!!
    }

    @Test
    fun test1() {
        assertTrue(navigation.isResume)
        val itemView = CSNavigationItemView(navigation, viewLayout = cs_frame_match).show()

        activityController.pause()
        assertTrue(navigation.isPaused)
        assertTrue(itemView.isPaused)

        itemView.dismiss()
        assertTrue(itemView.isDestructed)
    }

    @Test
    fun test2() {
        assertTrue(navigation.isResume)
        val itemView1 = CSNavigationItemView(navigation, viewLayout = cs_frame_match)
            .fullScreen().push()
        val itemView2 = CSNavigationItemView(navigation, viewLayout = cs_frame_match)
            .fullScreen().push()
        assertTrue(!itemView1.isVisibility.isTrue)
        assertTrue(itemView2.isVisibility.isTrue)
    }

    @Test
    fun popAndBackNavigation() {
        assertTrue(navigation.isResume)
        val itemView1 = CSNavigationItemView(navigation, viewLayout = cs_frame_match)
            .fullScreen().push()
        val itemView2 = CSNavigationItemView(navigation, viewLayout = cs_frame_match)
            .fullScreen().push()
        assertTrue(!itemView1.isVisibility.isTrue)
        assertTrue(itemView2.isVisibility.isTrue)

        navigation.pop()
        assertTrue(itemView1.isVisibility.isTrue)

        navigation.pop()
        assertTrue(navigation.controllers.isEmpty())
    }

    @Test
    fun test3() {
        assertTrue(navigation.isResume)
        val itemView1 = CSNavigationItemView(navigation, viewLayout = cs_frame_match)
            .fullScreen().push()
        val itemView2 = CSNavigationItemView(itemView1, viewLayout = cs_frame_match)
            .fullScreen().push()
        val itemView3 = CSNavigationItemView(itemView2, viewLayout = cs_frame_match)
            .fullScreen().push()
        assertTrue(!itemView1.isVisibility.isTrue)
        assertTrue(!itemView2.isVisibility.isTrue)
        assertTrue(itemView3.isVisibility.isTrue)

        itemView2.dismiss()

        assertTrue(itemView1.isVisibility.isTrue)
        assertTrue(itemView2.isDestructed)
        assertTrue(itemView3.isDestructed)
    }
}
