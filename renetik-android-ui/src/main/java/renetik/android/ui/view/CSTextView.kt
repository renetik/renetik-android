package renetik.android.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event

class CSTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), CSHasTouchEvent, CSAndroidView {
    override val self = this
    override val behavior = CSAndroidViewBehavior(this, attrs, defaultClipToOutline = false)
    override val eventOnTouch: CSEvent<CSTouchEventArgs> = event<CSTouchEventArgs>()
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        behavior.measure(widthMeasureSpec, heightMeasureSpec,
            swapAxes = isQuarterTurnRotation) { widthSpec, heightSpec ->
            super.onMeasure(widthSpec, heightSpec)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        return if (processTouchEvent(event)) true else super.onTouchEvent(event)
    }
}
