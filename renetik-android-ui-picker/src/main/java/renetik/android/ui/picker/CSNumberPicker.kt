package renetik.android.ui.picker

import android.content.Context
import android.util.AttributeSet
import com.shawnlin.numberpicker.NumberPicker
import com.shawnlin.numberpicker.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.property.CSProperty
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.ui.R

/**
 * Number picker wheel view.
 */
class CSNumberPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NumberPicker(context, attrs, defStyleAttr) {

    val index: CSProperty<Int> = property(value) { value = it }
    val eventOnScroll = event<Int>()
    private var isScrolling: Boolean = false
    var dispatchState: Boolean

    init {
        setOnScrollListener { _, scrollState ->
            isScrolling = scrollState != SCROLL_STATE_IDLE
            if (!isScrolling) index.value(value)
            eventOnScroll.fire(scrollState)
        }
        setOnValueChangedListener { _, _, newValue ->
            if (!isScrolling) index.value(newValue)
        }
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CSLayout, 0, 0)
        dispatchState = attributes.getBoolean(R.styleable.CSLayout_dispatchState, true)
        attributes.recycle()
    }

    override fun dispatchSetActivated(activated: Boolean) {
        if (dispatchState) super.dispatchSetActivated(activated)
    }

    override fun dispatchSetSelected(selected: Boolean) {
        if (dispatchState) super.dispatchSetSelected(selected)
    }

    override fun dispatchSetPressed(pressed: Boolean) {
        if (dispatchState) if (!isSelected) super.dispatchSetPressed(pressed)
    }
}