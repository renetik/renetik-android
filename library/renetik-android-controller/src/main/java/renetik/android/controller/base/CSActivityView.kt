package renetik.android.controller.base

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import renetik.android.controller.navigation.CSNavigationView
import renetik.android.controller.navigation.last
import renetik.android.core.extensions.content.inputService
import renetik.android.core.kotlin.className
import renetik.android.core.kotlin.unexpected
import renetik.android.core.lang.CSLayoutRes
import renetik.android.core.lang.lazy.CSLazyNullableVar.Companion.lazyNullableVar
import renetik.android.core.lang.variable.CSVariable
import renetik.android.core.lang.variable.isTrue
import renetik.android.core.logging.CSLog.logWarnTrace
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.common.destruct
import renetik.android.event.fire
import renetik.android.event.listen
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.onChange
import renetik.android.event.registration.plus
import renetik.android.event.registration.registerListenOnce
import renetik.android.ui.extensions.view.isShowing
import renetik.android.ui.extensions.view.isVisible
import renetik.android.ui.protocol.CSVisibility

open class CSActivityView<ViewType : View>
    : CSView<ViewType>, CSActivityViewInterface, LifecycleOwner, CSHasRegistrations {
    override val eventResume by lazy { event<Unit>() }
    override val eventPause by lazy { event<Unit>() }
    override val eventBack by lazy { event<CSVariable<Boolean>>() }
    final override fun activity(): CSActivity<*> = activity!!
    var isResumed = false
    private var isResumeFirstTime = false

    var parentActivityView: CSActivityView<*>? = null
        private set

    var activity: CSActivity<*>? = null
    private var showingInPager: Boolean? = null

    constructor(activity: CSActivity<*>) : super(activity) {
        this.activity = activity
        initializeParent(activity)
    }

    constructor(activity: CSActivity<*>, layout: CSLayoutRes) : super(activity, layout) {
        this.activity = activity
        initializeParent(activity)
    }

    constructor(parent: CSActivityView<*>) : super(parent) {
        parentActivityView = parent
        initializeParent(parent)
    }

    constructor(parent: CSActivityView<*>, @IdRes viewId: Int) : super(parent, viewId) {
        parentActivityView = parent
        initializeParent(parent)
    }

    constructor(parent: CSActivityView<*>, layout: CSLayoutRes) : super(parent, layout) {
        parentActivityView = parent
        initializeParent(parent)
    }

    constructor(parent: CSActivityView<*>, group: ViewGroup, layout: CSLayoutRes)
            : super(parent, group, layout) {
        parentActivityView = parent
        initializeParent(parent)
    }

    open fun onResume() {
        if (isResumed) return
        isResumed = true
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
        if (isPaused && isVisible.isTrue) {
            logWarnTrace { "Not Resumed while paused, should be resumed first:$this" }
            return
        }
        isResumed = false
        updateVisibility()
        eventPause.fire()
    }

    fun lifecycleUpdate() {
        parentActivityView?.let {
            if (it.isResumed) {
                if (isPaused) onResume()
            } else if (isResumed) onPause()
        }
    }

    fun lifecycleStop() {
        if (isResumed) onPause()
        destruct()
    }

    private fun <Parent> initializeParent(parent: Parent)
            where Parent : CSActivityViewInterface, Parent : CSVisibility {
        activity = parent.activity()
        this + parent.eventResume.listen(::onResume)
        this + parent.eventPause.listen(::onPause)
        this + parent.eventBack.listen(::onBack)
        this + parent.isVisible.onChange(::updateVisibility)
    }

    protected open fun onBack(goBack: CSVariable<Boolean>) {
        eventBack.fire(goBack)
        if (goBack.value && isVisible.isTrue) {
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
        if (isResumed) onPause()
        updateVisibility()
        super.onRemovedFromParentView()
    }

    fun showingInPager(isShowing: Boolean) = apply {
        if (showingInPager == isShowing) return this
        showingInPager = isShowing
        updateVisibility()
    }

    final override fun hideKeyboard() {
        activity?.currentFocus?.let {
            inputService.hideSoftInputFromWindow(it.rootView.windowToken, 0)
        } ?: super.hideKeyboard()
    }

    override val lifecycle: Lifecycle get() = activity().lifecycle

    override var isVisible = property(false)
    private var onViewShowingCalled = false

    override fun updateVisibility() {
        if (checkIfIsShowing()) {
            if (!isVisible.isTrue) onViewVisibilityChanged(true)
        } else if (isVisible.isTrue) onViewVisibilityChanged(false)
    }

    private fun checkIfIsShowing(): Boolean {
        if (!isResumed) return false
        if (!view.isVisible) return false
        if (showingInPager == false) return false
        if (isShowingInPager && parentActivityView?.let { it.isVisible.isTrue } == true) return true
        if (isShowingInPager && navigation?.last == this) return true
        if (parentActivityView?.let { it.isVisible.isTrue } == false) return false
        return view.isShowing()
    }

    val isShowingInPager get() = showingInPager == true

    private fun onViewVisibilityChanged(showing: Boolean) {
        if (isVisible.isTrue == showing) return
        isVisible.value(showing)
        if (isVisible.isTrue) onViewShowing() else onViewHiding()
    }

    protected open fun onViewShowing() {
        if (!onViewShowingCalled) {
            onViewShowingFirstTime()
            onViewShowingCalled = true
        } else onViewShowingAgain()
    }

    protected open fun onViewShowingFirstTime() {}

    protected open fun onViewShowingAgain() {}

    protected open fun onViewHiding() {
        if (!onViewShowingCalled) {
            onViewHidingFirstTime()
            onViewShowingCalled = true
        } else onViewHidingAgain()
    }

    protected open fun onViewHidingFirstTime() {}
    protected open fun onViewHidingAgain() {}

    open var navigation: CSNavigationView? by lazyNullableVar {
        findNavigation()?.also { registerListenOnce(it.eventDestruct) { navigation = null } }
    }

    private fun findNavigation(): CSNavigationView? {
        var controller: CSActivityView<*>? = parentActivityView
        do {
            if (controller?.navigation != null) return controller.navigation
            controller = controller?.parentActivityView
        } while (controller != null)
        return null
    }

    override fun onDestruct() {
        if (isResumed) onPause()
        if (isDestructed) unexpected("$className $this Already destroyed")
        super.onDestruct()
        parentActivityView = null
        activity = null
    }
}