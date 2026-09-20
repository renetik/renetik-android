package renetik.android.ui.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire

open class CSEmptyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes),
    CSHasTouchEvent, CSAndroidView, CSHasDrawEvent {

    override val self: View get() = this
    override val behavior = CSAndroidViewBehavior(this, attrs)
    override val eventOnTouch: CSEvent<CSTouchEventArgs> = event<CSTouchEventArgs>()

    var onDispatchTouchEvent: ((event: MotionEvent) -> Boolean)? = null
    val eventOnDraw: CSEvent<Canvas> = event<Canvas>()
    override fun listenOnDraw(listener: (Canvas) -> Unit) = eventOnDraw.listen(listener)
    val eventOnLayout = event()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        behavior.measure(widthMeasureSpec, heightMeasureSpec) { widthSpec, heightSpec ->
            super.onMeasure(widthSpec, heightSpec)
        }
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

    override fun onLayout(
        changed: Boolean, left: Int, top: Int, right: Int, bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) eventOnLayout.fire()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        eventOnDraw.fire(canvas)
    }
}
