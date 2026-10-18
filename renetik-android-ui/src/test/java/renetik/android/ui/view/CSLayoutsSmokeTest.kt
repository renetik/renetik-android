package renetik.android.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.ui.R
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.TestApplication
import renetik.android.testing.context

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CSLayoutsSmokeTest {

    @Test
    fun linearLayoutCreatesMeasuresAndLays() {
        val layout = CSLinearLayout(context)
        layout.add(View(context))
        layout.measure(makeMeasureSpec(100, EXACTLY), makeMeasureSpec(100, EXACTLY))
        layout.layout(0, 0, 100, 100)
        assert(expected = 1, actual = layout.childCount)
        assertTrue(layout.hasSize)
    }

    @Test
    fun frameLayoutCreatesMeasuresAndLays() {
        val layout = CSFrameLayout(context)
        layout.add(View(context))
        layout.measure(makeMeasureSpec(80, EXACTLY), makeMeasureSpec(40, EXACTLY))
        layout.layout(0, 0, 80, 40)
        assert(expected = 1, actual = layout.childCount)
        assertTrue(layout.hasSize)
    }

    @Test
    fun frameLayoutFiresTouchEvent() {
        val layout = CSFrameLayout(context)
        var touches = 0
        layout.eventOnTouch.listen { touches += 1 }
        layout.dispatchTouchEvent(
            android.view.MotionEvent.obtain(0, 0, android.view.MotionEvent.ACTION_DOWN, 1f, 1f, 0))
        assertTrue(touches > 0)
    }

    @Test
    fun frameLayoutResourcesDeclareTheirRootIdentities() {
        val inflater = LayoutInflater.from(context)
        mapOf(
            R.layout.cs_frame_match to R.id.cs_frame_match,
            R.layout.cs_frame_match_wrap to R.id.cs_frame_match_wrap,
            R.layout.cs_frame_wrap to R.id.cs_frame_wrap,
            R.layout.cs_frame_wrap_match to R.id.cs_frame_wrap_match,
        ).forEach { (layout, id) ->
            assert(expected = id, actual = inflater.inflate(layout, null, false).id)
        }
    }
}
