package renetik.android.ui.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire
import renetik.android.event.registration.CSRegistration

open class CSFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
    CSHasTouchEvent, CSAndroidView, CSHasDrawEvent {
    override val self: View get() = this
    override val behavior = CSAndroidViewBehavior(this, attrs)
    override val eventOnTouch: CSEvent<CSTouchEventArgs> = event<CSTouchEventArgs>()
    var onDispatchTouchEvent: ((event: MotionEvent) -> Boolean)? = null

    val eventOnDraw = event<Canvas>()
    override fun listenOnDraw(listener: (Canvas) -> Unit): CSRegistration {
        setWillNotDraw(false)
        return eventOnDraw.listen(listener)
    }

    var eventOnLayout = event()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        behavior.measure(widthMeasureSpec, heightMeasureSpec) { widthSpec, heightSpec ->
            super.onMeasure(widthSpec, heightSpec)
        }
    }

    override fun dispatchSetActivated(activated: Boolean) {
        if (behavior.dispatchState) super.dispatchSetActivated(activated)
    }

    override fun dispatchSetSelected(selected: Boolean) {
        if (behavior.dispatchState) super.dispatchSetSelected(selected)
    }

    override fun dispatchSetPressed(pressed: Boolean) {
        if (behavior.dispatchState) super.dispatchSetPressed(pressed)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        val handled = onDispatchTouchEvent?.invoke(event) ?: false
        return if (!handled) super.dispatchTouchEvent(event) else true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        return if (processTouchEvent(event)) true else super.onTouchEvent(event)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        eventOnLayout.fire()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        eventOnDraw.fire(canvas)
    }
}
