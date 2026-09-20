package renetik.android.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire

open class CSLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), CSHasTouchEvent, CSAndroidView {
    private var onDispatchTouchEvent: ((event: MotionEvent) -> Boolean)? = null
    override val self: View get() = this
    override val behavior = CSAndroidViewBehavior(this, attrs)
    override val eventOnTouch: CSEvent<CSTouchEventArgs> = event<CSTouchEventArgs>()
    val eventOnDraw = event<Canvas>()
    private var eventOnLayout = event()

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        return if (processTouchEvent(event)) true else super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        eventOnDraw.fire(canvas)
    }

    override fun onLayout(
        changed: Boolean, left: Int, top: Int, right: Int, bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        eventOnLayout.fire()
    }
}
