package renetik.android.controller.navigation

import android.view.Gravity.START
import android.view.Gravity.TOP
import android.view.View
import android.view.ViewGroup
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
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Left
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Right
import renetik.android.controller.navigation.CSNavigationItemPopupSide.Top
import renetik.android.core.android.graphics.CSColor.Companion.colorRes
import renetik.android.core.lang.CSLayoutRes.Companion.layout
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.change.invoke
import renetik.android.event.invoke
import renetik.android.event.lifecycle.destruct
import renetik.android.event.listen
import renetik.android.event.registration.plus
import renetik.android.ui.R.color
import renetik.android.ui.R.layout.cs_frame_match
import renetik.android.ui.protocol.onHasSize
import renetik.android.ui.protocol.registerAfterLayout
import renetik.android.ui.view.add
import renetik.android.ui.view.background
import renetik.android.ui.view.bottomFloat
import renetik.android.ui.view.heightWrap
import renetik.android.ui.view.leftFloat
import renetik.android.ui.view.locationInWindow
import renetik.android.ui.view.onClick
import renetik.android.ui.view.onLayoutChange
import renetik.android.ui.view.passClicksUnder
import renetik.android.ui.view.rightFloat
import renetik.android.ui.view.topFloat
import renetik.android.ui.widget.layoutMatch

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

    val viewContent: View by lazy {
        val frameLayout = if (isFullScreen)
            (fullScreenFrameLayout ?: frameLayout)
        else frameLayout
        (frameLayout?.let {
            inflate<FrameLayout>(it).apply { add<View>(viewLayout, layoutMatch) }
        } ?: inflate<View>(viewLayout))
    }

    open var isBackNavigationAllowed = true
    open val isBackgroundDimmed = true
    var isFullScreen = false
    var isPopup = false
        internal set
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
        view.add(viewContent)
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

    fun from(button: View, side: CSNavigationItemPopupSide = Bottom) = apply {
        isPopup = true
        selected(button)
        dismissOnTouchOut(true)
        isFullScreen = false
        animation = Fade
        viewContent.updateLayoutParams<LayoutParams> { gravity = START or TOP }
        onHasSize {
            when (side) {
                Bottom -> positionDialogContentFromViewBottom(button)
                Right -> positionDialogContentFromViewRight(button)
                Left -> positionDialogContentFromViewLeft(button)
                Top -> positionDialogContentFromViewTop(button)
            }
            correctContentOverflow()
        }
        this + view.onLayoutChange(::correctContentOverflow)
    }

    private fun positionDialogContentFromViewBottom(fromView: View) {
        val fromViewLocation = fromView.locationInWindow
        val fromViewTopCenterX = fromViewLocation.x + (fromView.width / 2)
        var desiredX = fromViewTopCenterX.toFloat() - (viewContent.width / 2)
        if (desiredX + viewContent.width > width - horizontalPadding)
            desiredX -= (desiredX + viewContent.width) - (width - horizontalPadding)
        if (desiredX < horizontalPadding) desiredX = horizontalPadding
        viewContent.x = desiredX
        viewContent.y = fromViewLocation.y.toFloat() + fromView.height
    }

    val screenAvailableHeight get() = height - verticalPadding * 2
    val screenAvailableWidth get() = width - horizontalPadding * 2

    private fun positionDialogContentFromViewTop(fromView: View) {
        val fromViewLocation = fromView.locationInWindow
        val fromViewTopCenterX = fromViewLocation.x + (fromView.width / 2)
        var desiredX = fromViewTopCenterX.toFloat() - (viewContent.width / 2)
        if (desiredX + viewContent.width > screenAvailableWidth) desiredX -= (desiredX + viewContent.width) - screenAvailableWidth
        if (desiredX < horizontalPadding) desiredX = horizontalPadding
        viewContent.x = desiredX
        viewContent.y =
            fromViewLocation.y.toFloat() - viewContent.height - verticalPadding
    }

    private fun correctContentOverflow() {
        if (viewContent.bottomFloat > screenAvailableHeight)
            viewContent.topFloat -= viewContent.bottomFloat - screenAvailableHeight
        if (viewContent.rightFloat > screenAvailableWidth)
            viewContent.leftFloat -= viewContent.rightFloat - screenAvailableWidth
    }

    private fun positionDialogContentFromViewRight(fromView: View) {
        val fromViewLocation = fromView.locationInWindow
        val fromViewLeftCenterY = fromViewLocation.y + (fromView.height / 2)
        var desiredY = fromViewLeftCenterY.toFloat() - (viewContent.height / 2)
        if (desiredY + viewContent.height > screenAvailableHeight)
            desiredY -= (desiredY + viewContent.height) - screenAvailableHeight
        if (desiredY < verticalPadding) desiredY = verticalPadding
        viewContent.x = fromViewLocation.x.toFloat() + fromView.width
        viewContent.y = desiredY
    }

    private fun positionDialogContentFromViewLeft(fromView: View) {
        val fromViewLocation = fromView.locationInWindow
        viewContent.x = fromViewLocation.x.toFloat() - viewContent.width
        viewContent.y = fromViewLocation.y.toFloat() - verticalPadding
    }

    fun wrapContentIfNotFullscreen() {
        if (isFullScreen) return
        viewContent.heightWrap()
        registerAfterLayout(::correctContentOverflow)
    }
}