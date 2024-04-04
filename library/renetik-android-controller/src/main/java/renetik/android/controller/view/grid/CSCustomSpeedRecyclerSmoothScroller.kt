package renetik.android.controller.view.grid

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

open class CSCustomRecyclerSmoothScroller(context: Context, animationDuration: Int) :
    LinearSmoothScroller(context) {

    private val millisecondsPerInch: Float = 1000f / animationDuration.toFloat()

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float =
        millisecondsPerInch / displayMetrics.densityDpi
}