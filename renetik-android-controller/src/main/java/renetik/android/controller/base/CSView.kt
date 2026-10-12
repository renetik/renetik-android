@file:Suppress("LeakingThis")

package renetik.android.controller.base

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import renetik.android.core.kotlin.className
import renetik.android.core.kotlin.unexpected
import renetik.android.core.lang.CSLayoutRes
import renetik.android.core.lang.lazy.CSLazyNullableVar.Companion.lazyNullableVar
import renetik.android.core.lang.value.isTrue
import renetik.android.core.lang.variable.CSVariable.Companion.variable
import renetik.android.core.logging.CSLog.logErrorTrace
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.android.CSContext
import renetik.android.event.change.CSHasChangeValue
import renetik.android.event.change.onChange
import renetik.android.event.invoke
import renetik.android.event.lifecycle.destruct
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.event.registration.plus
import renetik.android.ui.inflate
import renetik.android.ui.inflateAsync
import renetik.android.ui.protocol.view
import renetik.android.ui.view.isVisible
import renetik.android.ui.view.onDestruct
import renetik.android.ui.protocol.CSHasParentView
import renetik.android.ui.protocol.CSViewInterface
import renetik.android.ui.protocol.CSViewInterface.Companion.context

open class CSView<ViewType : View> : CSContext, CSHasParentView, CSViewInterface {

    var lifecycleStopOnRemoveFromParentView = true

    private var layout: Int? = null

    @IdRes
    private var viewId: Int? = null

    var parentView: CSViewInterface by variable()

    private var container: ViewGroup? by lazyNullableVar {
        parentView.view as? ViewGroup ?: (parentView as? CSView<*>)?.container
    }

    private var _view: ViewType? = null

    constructor(parent: CSViewInterface, view: ViewType) :
            super(parent, view.context) {
        this.parentView = parent
        this + parentView.isVisibility.onChange(::updateVisibility)
        this.layout = null
        this.viewId = null
        setView(view)
    }

    constructor(parent: CSViewInterface, layout: CSLayoutRes) :
            super(parent, context(parent)) {
        this.parentView = parent
        this + parentView.isVisibility.onChange(::updateVisibility)
        this.layout = layout.id
        this.viewId = null
    }

    constructor(parent: CSViewInterface, container: ViewGroup, layout: CSLayoutRes) :
            super(parent, context(parent)) {
        this.parentView = parent
        this + parentView.isVisibility.onChange(::updateVisibility)
        this.container = container
        this.layout = layout.id
        this.viewId = null
    }

    constructor(parent: CSViewInterface, @IdRes viewId: Int) : super(parent) {
        this.parentView = parent
        this + parentView.isVisibility.onChange(::updateVisibility)
        this.layout = null
        this.viewId = viewId
    }

    constructor(parent: CSViewInterface, @LayoutRes layout: Int? = null) : super(parent) {
        this.parentView = parent
        this + parentView.isVisibility.onChange(::updateVisibility)
        this.layout = layout
    }

    @Suppress("UNCHECKED_CAST")
    final override val view: ViewType
        get() = _view ?: run {
            if (isDestructed) logErrorTrace { "$className $this Already destroyed" }
            layout?.let { setView(inflate(it)) }
                ?: viewId?.let { setView(parentView.view<View>(viewId!!) as ViewType) }
                ?: createView()?.let { setView(it) }
                ?: (parentView.view as? ViewType).also { _view = it; onViewReady() }
                ?: unexpected("$className: view could not be created")
        }

    @Suppress("UNCHECKED_CAST")
    override suspend fun view(): View = _view ?: run {
        if (isDestructed) logErrorTrace { "$className $this Already destroyed" }
        layout?.let { setView(context.inflateAsync(it, container)) }
            ?: viewId?.let { setView(parentView.view<View>(viewId!!) as ViewType) }
            ?: createView()?.let { setView(it) }
            ?: (parentView.view as? ViewType).also { _view = it; onViewReady() }
            ?: unexpected("$className: view could not be created")
    }

    private fun setView(view: ViewType): ViewType {
        _view = view
        if (view.tag !is CSView<*>) view.tag = this@CSView
        onViewReady()
        return view
    }

    fun <ViewType : View> inflate(@LayoutRes layoutId: Int): ViewType =
        context.inflate(layoutId, container)

    protected open fun createView(): ViewType? = null

    protected open fun onViewReady() = Unit

    open fun hideKeyboard() {
        val rootView = view.rootView ?: return
        val insets = ViewCompat.getRootWindowInsets(rootView) ?: return
        if (!insets.isVisible(WindowInsetsCompat.Type.ime())) return
        @Suppress("DEPRECATION") // Keep compat while minSdk stays 26
        ViewCompat.getWindowInsetsController(rootView)?.hide(WindowInsetsCompat.Type.ime())
    }

    override fun onAddedToParentView() = updateVisibility()

    override fun onRemovedFromParentView() {
        if (lifecycleStopOnRemoveFromParentView && !isDestructed) destruct()
    }

    open var isActivated
        get() = contentView.isActivated
        set(value) {
            contentView.isActivated = value
        }

    open var isEnabled
        get() = contentView.isEnabled
        set(value) {
            contentView.isEnabled = value
        }

    open var isSelected
        get() = contentView.isSelected
        set(value) {
            contentView.isSelected = value
        }

    open val contentView: View get() = view

    //Visibility
    final override val isVisibility: CSHasChangeValue<Boolean>
        field = property(false)
    private var onViewShowingCalled = false

    override fun updateVisibility() {
        if (checkIfIsShowing()) {
            if (!isVisibility.isTrue) onViewVisibilityChanged(true)
        } else if (isVisibility.isTrue) onViewVisibilityChanged(false)
    }

    protected open fun checkIfIsShowing(): Boolean =
        // Maybe parentView.view.isVisible in some cases will cause problems...
        view.isVisible && parentView.isVisibility.isTrue

    private fun onViewVisibilityChanged(showing: Boolean) {
        if (isVisibility.value == showing) return
        isVisibility.value(showing)
        if (isVisibility.isTrue) onViewShowing() else onViewHiding()
    }

    protected open fun onViewShowing() {
        if (!onViewShowingCalled) {
            onViewShowingFirstTime()
            onViewShowingCalled = true
        } else onViewShowingAgain()
    }

    val onViewShowingFirstTime = event()
    protected open fun onViewShowingFirstTime() = onViewShowingFirstTime.invoke()

    protected open fun onViewShowingAgain() {}

    protected open fun onViewHiding() {
        if (!onViewShowingCalled) {
            onViewHidingFirstTime()
            onViewShowingCalled = true
        } else onViewHidingAgain()
    }

    protected open fun onViewHidingFirstTime() {}
    protected open fun onViewHidingAgain() {}

    override fun onDestruct() {
        super.onDestruct()
        // View doesn't have to be created in some cases
        _view?.let {
            if (it.tag == this) {
                it.tag = "tag instance of $className removed, onDestroy called"
                it.onDestruct()
            }
            _view = null
        }
    }
}
