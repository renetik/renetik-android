package renetik.android.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import renetik.android.core.logging.CSLog.logWarn
import renetik.android.ui.view.touch.logPointerIndexBugOrThrow

class CSViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    var isSwipePagingEnabled = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = try {
        if (isSwipePagingEnabled) super.onTouchEvent(event) else false
    } catch (e: IllegalArgumentException) {
        e.logPointerIndexBugOrThrow()
        false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = try {
        if (isSwipePagingEnabled) super.onInterceptTouchEvent(event) else false
    } catch (e: IllegalArgumentException) {
        e.logPointerIndexBugOrThrow()
        false
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        try {
            super.onRestoreInstanceState(state)
        } catch (e: IllegalStateException) {
            logWarn(e) { "Fragment state restoration failed, resetting to default state" }
        }
    }
}