package renetik.android.controller.base

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import renetik.android.controller.navigation.CSNavigationView
import renetik.android.controller.navigation.last
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.kotlin.unexpected
import renetik.android.core.lang.CSLayoutRes
import renetik.android.core.lang.lazy.CSLazyVal.Companion.lazyVal
import renetik.android.core.lang.value.isTrue
import renetik.android.core.lang.variable.CSVariable
import renetik.android.core.logging.CSLog.logWarnTrace
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.change.onChange
import renetik.android.event.fire
import renetik.android.event.lifecycle.destruct
import renetik.android.event.listen
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.plus
import renetik.android.ui.view.isVisible
import renetik.android.ui.view.isVisibleInParentRecursively
import renetik.android.ui.protocol.CSVisibility

open class CSActivityView<ViewType : View>
    : CSView<ViewType>, CSActivityViewInterface, LifecycleOwner, CSHasRegistrations {
    final override val eventResume by lazy { event<Unit>() }
    final override val eventPause by lazy { event<Unit>() }
    final override val eventBack by lazy { event<CSVariable<Boolean>>() }
    final override fun activity(): CSViewActivity<*> = activity!!
    var isResume = false
    private var isResumeFirstTime = false

    private var parentActivityView: CSActivityView<*>? = null

    var activity: CSViewActivity<*>? = null
    private var showingInPager: Boolean? = null

    constructor(activity: CSViewActivity<*>) : super(activity) {
        this.activity = activity
        initializeParent(activity)
    }

    constructor(activity: CSViewActivity<*>, layout: CSLayoutRes)
            : super(activity, layout) {
        this.activity = activity
        initializeParent(activity)
    }

    constructor(activity: CSViewActivity<*>, view: ViewType)
            : super(activity, view) {
        this.activity = activity
        initializeParent(activity)
    }

    constructor(parent: CSActivityViewInterface) : super(parent) {
        parentActivityView = parent as CSActivityView<*>
        initializeParent(parent)
    }

    constructor(parent: CSActivityViewInterface, @IdRes viewId: Int)
            : super(parent, viewId) {
        parentActivityView = parent as CSActivityView<*>
        initializeParent(parent)
    }

    constructor(parent: CSActivityViewInterface, layout: CSLayoutRes)
            : super(parent, layout) {
        parentActivityView = parent as CSActivityView<*>
        initializeParent(parent)
    }

    constructor(parent: CSActivityViewInterface, view: ViewType)
            : super(parent, view) {
        parentActivityView = parent as CSActivityView<*>
        initializeParent(parent)
    }

    constructor(parent: CSActivityViewInterface, group: ViewGroup, layout: CSLayoutRes)
            : super(parent, group, layout) {
        parentActivityView = parent as CSActivityView<*>
        initializeParent(parent)
    }

    open fun onResume() {
        if (isResume) return
        isResume = true
        if (!isResumeFirstTime) {
            onResumeFirstTime()
            isResumeFirstTime = true
        } else onResumeAgain()
        updateVisibility()
        eventResume.fire()
    }

    protected open fun onResumeFirstTime() {}

    protected open fun onResumeAgain() {}

    open fun onPause() {
        if (isPaused && isVisibility.isTrue) {
            logWarnTrace { "Not Resumed while paused, should be resumed first:$this" }
            return
        }
        isResume = false
        updateVisibility()
        eventPause.fire()
    }

    fun lifecycleUpdate() {
        parentActivityView?.let {
            if (it.isResume) {
                if (isPaused) onResume()
            } else if (isResume) onPause()
        }
    }

    fun lifecycleStop() {
        if (isResume) onPause()
        destruct()
    }

    private fun <Parent> initializeParent(parent: Parent)
            where Parent : CSActivityViewInterface, Parent : CSVisibility {
        activity = parent.activity()
        this + parent.eventResume.listen(::onResume)
        this + parent.eventPause.listen(::onPause)
        this + parent.eventBack.listen(::onBack)
        this + parent.isVisibility.onChange(::updateVisibility)
    }

    protected open fun onBack(goBack: CSVariable<Boolean>) {
        eventBack.fire(goBack)
        if (goBack.value && isVisibility.isTrue) {
            hideKeyboard()
            goBack.value = onGoBack()
        }
    }

    protected open fun onGoBack() = true

    override fun onAddedToParentView() {
        lifecycleUpdate()
        updateVisibility()
        super.onAddedToParentView()
    }

    override fun onRemovedFromParentView() {
        if (isResume) onPause()
        updateVisibility()
        super.onRemovedFromParentView()
    }

    fun showingInPager(isShowing: Boolean): CSActivityView<ViewType> = apply {
        if (showingInPager == isShowing) return this
        showingInPager = isShowing
        updateVisibility()
    }

    final override fun hideKeyboard() {
        val focused = activity?.currentFocus
        val rootView = focused?.rootView
            ?: activity?.window?.decorView ?: view.rootView ?: return
        val insets = ViewCompat.getRootWindowInsets(rootView) ?: return
        if (!insets.isVisible(WindowInsetsCompat.Type.ime())) return
        @Suppress("DEPRECATION") // Keep compat while minSdk stays 26
        ViewCompat.getWindowInsetsController(rootView)?.hide(WindowInsetsCompat.Type.ime())
    }

    override val lifecycle: Lifecycle get() = activity().lifecycle

    private var overrideVisibility: Boolean? = null

    fun overrideVisibility(isVisible: Boolean? = null) {
        overrideVisibility = isVisible
        updateVisibility()
    }

    override fun checkIfIsShowing(): Boolean {
        if (!isResume) return false
        if (overrideVisibility != null) return overrideVisibility!!
        if (!view.isVisible) return false
        if (showingInPager == false) return false
        if (isShowingInPager && parentActivityView?.isVisibility.isTrue) return true
        if (isShowingInPager && navigation?.last == this) return true
        if (parentActivityView?.isVisibility?.isTrue == false) return false
        return view.isVisibleInParentRecursively()
    }

    val isShowingInPager get() = showingInPager == true

    open val navigation: CSNavigationView? by lazyVal { findNavigation() }

    private fun findNavigation(): CSNavigationView? =
        generateSequence(parentActivityView) { it.parentActivityView }
            .map { it.navigation }.firstOrNull()

    override fun onDestruct() {
        if (isResume) onPause()
        if (isDestructed) unexpected("$debugClassName $this Already destroyed")
        super.onDestruct()
        parentActivityView = null
        activity = null
    }
}
