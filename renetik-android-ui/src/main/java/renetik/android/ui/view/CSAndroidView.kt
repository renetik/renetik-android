package renetik.android.ui.view

import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import renetik.android.ui.R
import renetik.android.ui.R.styleable.CSLayout_clipToOutline
import renetik.android.ui.R.styleable.CSLayout_dispatchState
import renetik.android.ui.R.styleable.CSLayout_goneIfHeightUntil
import renetik.android.ui.R.styleable.CSLayout_goneIfWidthUntil
import renetik.android.ui.R.styleable.CSLayout_maxHeight
import renetik.android.ui.R.styleable.CSLayout_maxWidth
import renetik.android.ui.R.styleable.CSLayout_minHeight
import renetik.android.ui.R.styleable.CSLayout_minWidth

interface CSAndroidView {
    val behavior: CSAndroidViewBehavior
}

class CSAndroidViewBehavior(
    val view: View, attrs: AttributeSet?,
    defaultClipToOutline: Boolean = view.clipToOutline
) {
    var minWidth = -1
    var maxWidth = -1
    var minHeight = -1
    var maxHeight = -1
    var dispatchState = true

    init {
        val attributes =
            view.context.theme.obtainStyledAttributes(attrs, R.styleable.CSLayout, 0, 0)
        try {
            view.clipToOutline =
                attributes.getBoolean(CSLayout_clipToOutline, defaultClipToOutline)
            minWidth = attributes.getDimensionPixelSize(CSLayout_minWidth, -1)
            maxWidth = attributes.getDimensionPixelSize(CSLayout_maxWidth, -1)
            minHeight = attributes.getDimensionPixelSize(CSLayout_minHeight, -1)
            maxHeight = attributes.getDimensionPixelSize(CSLayout_maxHeight, -1)
            dispatchState = attributes.getBoolean(CSLayout_dispatchState, true)

            val goneIfWidthUntil =
                attributes.getDimensionPixelSize(CSLayout_goneIfWidthUntil, -1)
            val goneIfHeightUntil =
                attributes.getDimensionPixelSize(CSLayout_goneIfHeightUntil, -1)
            if (goneIfWidthUntil != -1) view.gone(view.screenWidth <= goneIfWidthUntil)
            else if (goneIfHeightUntil != -1) view.gone(view.screenHeight <= goneIfHeightUntil)
        } finally {
            attributes.recycle()
        }
    }

    // TODO: Improve measurement performance by constraining EXACTLY, AT_MOST, and UNSPECIFIED
    //  inputs before the first pass. This changes sizing semantics and needs physical-device tests.
    inline fun measure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        swapAxes: Boolean = false,
        measureView: (widthMeasureSpec: Int, heightMeasureSpec: Int) -> Unit
    ) {
        val initialWidthSpec = if (swapAxes) heightMeasureSpec else widthMeasureSpec
        val initialHeightSpec = if (swapAxes) widthMeasureSpec else heightMeasureSpec
        measureView(initialWidthSpec, initialHeightSpec)

        var constrainedWidthSpec = initialWidthSpec
        if (minWidth != -1 && view.measuredWidth < minWidth)
            constrainedWidthSpec = makeMeasureSpec(minWidth, EXACTLY)
        else if (maxWidth != -1 && view.measuredWidth > maxWidth)
            constrainedWidthSpec = makeMeasureSpec(maxWidth, EXACTLY)

        var constrainedHeightSpec = initialHeightSpec
        if (minHeight != -1 && view.measuredHeight < minHeight)
            constrainedHeightSpec = makeMeasureSpec(minHeight, EXACTLY)
        else if (maxHeight != -1 && view.measuredHeight > maxHeight)
            constrainedHeightSpec = makeMeasureSpec(maxHeight, EXACTLY)

        if (constrainedWidthSpec != initialWidthSpec || constrainedHeightSpec != initialHeightSpec)
            measureView(constrainedWidthSpec, constrainedHeightSpec)
    }
}
