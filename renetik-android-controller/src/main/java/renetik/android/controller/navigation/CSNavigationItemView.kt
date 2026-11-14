package renetik.android.controller.navigation

import android.view.Gravity.CENTER
import android.view.Gravity.START
import android.view.Gravity.TOP
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.LayoutRes
import androidx.core.view.updateLayoutParams
import renetik.android.controller.base.CSActivityView
import renetik.android.controller.base.height
import renetik.android.controller.base.width
import renetik.android.controller.navigation.CSNavigationItemAnimation.Fade
import renetik.android.controller.navigation.CSNavigationItemAnimation.None
import renetik.android.controller.navigation.CSNavigationItemAnimation.Slide
import renetik.android.controller.navigation.CSNavigationItemAnimation.SlideFade
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Bottom
import renetik.android.controller.navigation.CSNavigationPresentation.FullScreen
import renetik.android.controller.navigation.CSNavigationPresentation.Popup
import renetik.android.controller.navigation.CSNavigationPresentation.Window
import renetik.android.core.android.content.dimensionInt
import renetik.android.core.android.graphics.CSColor.Companion.colorRes
import renetik.android.core.lang.CSLayoutRes.Companion.layout
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.change.invoke
import renetik.android.event.invoke
import renetik.android.event.lifecycle.destruct
import renetik.android.event.listen
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.plus
import renetik.android.event.registration.register
import renetik.android.ui.R.color
import renetik.android.ui.R.layout.cs_frame_match
import renetik.android.ui.protocol.onHasSize
import renetik.android.ui.protocol.registerAfterLayout
import renetik.android.ui.view.add
import renetik.android.ui.view.background
import renetik.android.ui.view.onClick
import renetik.android.ui.view.onLayoutChange
import renetik.android.ui.view.passClicksUnder
import renetik.android.ui.view.removeFromSuperview

open class CSNavigationItemView(
    navigationParent: CSActivityView<out ViewGroup>,
    private val horizontalPadding: Float = 0f,
    private val verticalPadding: Float = 0f,
    @LayoutRes private val viewLayout: Int,
    @LayoutRes private val frameLayout: Int? = null,
    @LayoutRes private val fullScreenFrameLayout: Int? = null,
) : CSActivityView<FrameLayout>(
    navigationParent.navigation!!, cs_frame_match.layout
) {

    constructor(
        parent: CSActivityView<out ViewGroup>,
        contentWidthMarginDp: Float, contentHeightMarginDp: Float,
        @LayoutRes viewLayout: Int, @LayoutRes frameLayout: Int? = null
    ) : this(parent, contentWidthMarginDp, contentHeightMarginDp,
        viewLayout, frameLayout, null)

    constructor(
        parent: CSActivityView<out ViewGroup>,
        contentWidthMarginDp: Float, contentHeightMarginDp: Float,
        @LayoutRes viewLayout: Int,
    ) : this(parent, contentWidthMarginDp, contentHeightMarginDp,
        viewLayout, null, null)

    private var layoutWidth = 0
    private var layoutHeight = 0

    val layoutView: View by lazy {
        inflate<View>(viewLayout).also {
            layoutWidth = it.layoutParams.width
            layoutHeight = it.layoutParams.height
        }
    }

    @LayoutRes private var currentFrameLayout: Int? = frameLayout
    private var currentFrameView: View? = null

    /** Theme frame around [layoutView], or [layoutView] itself when there is no frame. */
    private val frameView: View
        get() = currentFrameView ?: framed(currentFrameLayout).also { currentFrameView = it }
    private val isFramed get() = frameView !== layoutView

    private var presentation: CSNavigationPresentation? = null
    private var contentSize = CSContentSize.Layout
    val isFullScreen get() = presentation == FullScreen
    val isPopup get() = presentation is Popup
    private val popupPlacement by lazy {
        CSNavigationItemPopupPlacement(view, horizontalPadding, verticalPadding)
    }
    private var popupLayoutRegistration: CSRegistration? = null

    open var isBackNavigationAllowed = true
    open val isBackgroundDimmed = true
    val isOpen: Boolean get() = navigation?.controllers?.contains(this) == true

    open fun onViewControllerPush(navigation: CSNavigationView) = Unit
    open fun onViewControllerPop(navigation: CSNavigationView) = Unit

    var animation = Fade

    private val eventDismiss = event()
    fun onDismiss(function: () -> Unit) = eventDismiss.listen(function)

    private val eventOnClose = event()
    fun onClose(function: () -> Unit) = eventOnClose.listen(function)

    private var dismissOnTouchOut = false
    fun dismissOnTouchOut(dismiss: Boolean = true) = apply { dismissOnTouchOut = dismiss }

    private var passClicksUnder = false
    fun passClicksUnder(pass: Boolean = true) = apply {
        passClicksUnder = pass
        view.passClicksUnder(passClicksUnder)
    }

    init {
        this + navigationParent.eventDestruct {
            if (isOpen) close()
            if (!lifecycleStopOnRemoveFromParentView) destruct()
        }
    }

    override fun onViewReady() {
        super.onViewReady()
        view.add(frameView)
    }

    override fun onAddedToParentView() {
        applyPresentation()
        super.onAddedToParentView()
    }

    override fun onViewShowingFirstTime() {
        super.onViewShowingFirstTime()
        if (dismissOnTouchOut) view.onClick { onBackgroundClick() }
        else view.passClicksUnder(passClicksUnder)
        if (!isFullScreen && isBackgroundDimmed) {
            val color = if (isPopup) color.cs_dialog_popup_background
            else color.cs_dialog_background
            view.background(colorRes(color))
        }
    }

    protected open fun onBackgroundClick() = dismiss()

    fun dismiss() {
        if (isDestructed) return
        eventDismiss()
        close()
    }

    private fun close() {
        navigation?.pop(this)
    }

    override fun onRemovedFromParentView() {
        super.onRemovedFromParentView()
        eventOnClose()
    }

    val pushAnimation
        get() = when (animation) {
            Slide, SlideFade -> CSNavigationAnimation.SlideInRight
            Fade -> CSNavigationAnimation.FadeIn
            None -> CSNavigationAnimation.None
        }

    val popAnimation
        get() = when (animation) {
            Slide -> CSNavigationAnimation.SlideOutLeft
            Fade, SlideFade -> CSNavigationAnimation.FadeOut
            None -> CSNavigationAnimation.None
        }

    /** Takes effect when shown; its size is applied like [resizeContent]. */
    fun present(presentation: CSNavigationPresentation) {
        this.presentation = presentation
        when (presentation) {
            is Popup -> {
                contentSize = contentSize.overriddenBy(presentation.size)
                selected(presentation.anchor)
                dismissOnTouchOut()
            }
            is Window -> contentSize = contentSize.overriddenBy(presentation.size)
            FullScreen -> Unit
        }
    }

    /** Sets the dimensions [size] defines, the others keep their current size. */
    fun resizeContent(size: CSContentSize) {
        contentSize = contentSize.overriddenBy(size)
        if (isOpen) applyPresentation()
    }

    fun from(button: View, side: CSNavigationItemPopupSide = Bottom) = apply {
        present(Popup(button, side))
    }

    private fun applyPresentation() {
        useFrame(if (isFullScreen) fullScreenFrameLayout ?: frameLayout else frameLayout)
        when (val presentation = presentation) {
            null -> applyContentSize()
            is Popup -> placePopup(presentation)
            is Window -> placeWindow()
            FullScreen -> layoutFullScreen()
        }
    }

    private fun useFrame(@LayoutRes layout: Int?) {
        if (layout == currentFrameLayout) return
        currentFrameLayout = layout
        val previousFrameView = currentFrameView ?: return
        val root = previousFrameView.parent as? ViewGroup
        val index = root?.indexOfChild(previousFrameView) ?: -1
        root?.removeView(previousFrameView)
        currentFrameView = framed(layout)
        root?.addView(frameView, index)
    }

    private fun framed(@LayoutRes frame: Int?): View =
        frame?.let {
            inflate<ViewGroup>(it).apply {
                // Frames consume touches so dismissOnTouchOut reacts only outside the window.
                isClickable = true
                add(layoutView)
            }
        } ?: layoutView.removeFromSuperview()

    private fun placePopup(popup: Popup) {
        frameView.updateLayoutParams<LayoutParams> { gravity = START or TOP }
        sizeFrame(WRAP_CONTENT)
        applyContentSize()
        onHasSize { popupPlacement.place(frameView, popup.anchor, popup.side) }
        popupLayoutRegistration = register(popupLayoutRegistration,
            view.onLayoutChange(::keepPopupInside))
    }

    private fun keepPopupInside() = popupPlacement.keepInside(frameView)

    private fun placeWindow() {
        frameView.updateLayoutParams<LayoutParams> { gravity = CENTER }
        sizeFrame(WRAP_CONTENT)
        applyContentSize()
        registerAfterLayout(::shrinkWindowToScreen)
    }

    private fun layoutFullScreen() {
        sizeFrame(MATCH_PARENT)
        layoutView.updateLayoutParams {
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
    }

    private fun sizeFrame(size: Int) {
        if (isFramed) frameView.updateLayoutParams {
            width = size
            height = size
        }
    }

    private fun applyContentSize() = layoutView.updateLayoutParams {
        width = contentSize.width?.let { dimensionInt(it) } ?: layoutWidth
        height = contentSize.height?.let { dimensionInt(it) } ?: layoutHeight
    }

    private fun shrinkWindowToScreen() {
        val maxWidth = (screenAvailableWidth -
                if (isFramed) frameView.paddingLeft + frameView.paddingRight else 0).toInt()
        val maxHeight = (screenAvailableHeight -
                if (isFramed) frameView.paddingTop + frameView.paddingBottom else 0).toInt()
        if (layoutView.width <= maxWidth && layoutView.height <= maxHeight) return
        layoutView.updateLayoutParams {
            if (layoutView.width > maxWidth) width = maxWidth
            if (layoutView.height > maxHeight) height = maxHeight
        }
    }

    val screenAvailableHeight get() = height - verticalPadding * 2
    val screenAvailableWidth get() = width - horizontalPadding * 2
}
