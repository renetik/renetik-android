package renetik.android.framework.sample

import android.widget.FrameLayout
import renetik.android.controller.base.CSActivityView
import renetik.android.controller.base.push
import renetik.android.controller.navigation.CSNavigationItemView
import renetik.android.controller.navigation.CSNavigationView
import renetik.android.controller.navigation.fullScreen
import renetik.android.core.lang.CSLayoutRes.Companion.layout
import renetik.android.core.lang.lazy.CSLazyVal.Companion.lazyVal
import renetik.android.ui.R.layout.cs_frame_match
import renetik.android.ui.protocol.add

class MainView(activity: MainActivity) :
    CSActivityView<FrameLayout>(activity, layout(cs_frame_match)) {

    // The navigation view must be attached to this root frame, otherwise the
    // pushed screens are never in the window and the activity looks blank.
    override val navigation: CSNavigationView by lazyVal {
        CSNavigationView(this).also { view.add(it, 0) }
    }

    val checklist: ChecklistView by lazy { ChecklistView(this) }

    override fun onViewShowingFirstTime() {
        super.onViewShowingFirstTime()
        checklist.fullScreen().push()
    }
}

class DetailView(parent: CSNavigationItemView) :
    CSNavigationItemView(parent, viewLayout = R.layout.sample_detail) {

    override fun onViewReady() {
        super.onViewReady()
        viewContent.findViewById<android.view.View>(R.id.sample_detail_close_button)
            .setOnClickListener { dismiss() }
    }
}
