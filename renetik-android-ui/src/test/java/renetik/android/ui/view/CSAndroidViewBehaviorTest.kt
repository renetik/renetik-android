package renetik.android.ui.view

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import androidx.appcompat.widget.AppCompatTextView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.TestApplication
import renetik.android.testing.context

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CSAndroidViewBehaviorTest {

    private class Subject(
        minWidth: Int = -1, maxWidth: Int = -1, minHeight: Int = -1, maxHeight: Int = -1
    ) {
        val view = View(context)
        val behavior = CSAndroidViewBehavior(view, null).also {
            it.minWidth = minWidth; it.maxWidth = maxWidth
            it.minHeight = minHeight; it.maxHeight = maxHeight
        }
        val passes = mutableListOf<Pair<Int, Int>>()

        fun measure(width: Int, height: Int, swapAxes: Boolean = false) = behavior.measure(
            makeMeasureSpec(width, EXACTLY), makeMeasureSpec(height, EXACTLY), swapAxes
        ) { widthSpec, heightSpec ->
            passes += widthSpec to heightSpec
            // View.measure caches by spec, onMeasure in the real views does not.
            view.forceLayout()
            view.measure(widthSpec, heightSpec)
        }
    }

    private fun exactly(size: Int) = makeMeasureSpec(size, EXACTLY)

    @Test
    fun unconstrainedMeasuresOnce() = Subject().run {
        measure(100, 40)
        assert(expected = 1, actual = passes.size)
        assert(expected = exactly(100) to exactly(40), actual = passes[0])
        assert(expected = 100, actual = view.measuredWidth)
        assert(expected = 40, actual = view.measuredHeight)
    }

    @Test
    fun withinBoundsMeasuresOnce() = Subject(minWidth = 50, maxWidth = 150).run {
        measure(100, 40)
        assert(expected = 1, actual = passes.size)
        assert(expected = 100, actual = view.measuredWidth)
    }

    @Test
    fun maxWidthClampsOnSecondPass() = Subject(maxWidth = 50).run {
        measure(100, 40)
        assert(expected = 2, actual = passes.size)
        assert(expected = exactly(50) to exactly(40), actual = passes[1])
        assert(expected = 50, actual = view.measuredWidth)
        assert(expected = 40, actual = view.measuredHeight)
    }

    @Test
    fun minWidthExpandsOnSecondPass() = Subject(minWidth = 150).run {
        measure(100, 40)
        assert(expected = exactly(150) to exactly(40), actual = passes[1])
        assert(expected = 150, actual = view.measuredWidth)
    }

    @Test
    fun maxHeightClampsOnSecondPass() = Subject(maxHeight = 20).run {
        measure(100, 40)
        assert(expected = exactly(100) to exactly(20), actual = passes[1])
        assert(expected = 20, actual = view.measuredHeight)
    }

    @Test
    fun minHeightExpandsOnSecondPass() = Subject(minHeight = 80).run {
        measure(100, 40)
        assert(expected = exactly(100) to exactly(80), actual = passes[1])
        assert(expected = 80, actual = view.measuredHeight)
    }

    @Test
    fun bothAxesClampInOneSecondPass() = Subject(maxWidth = 50, maxHeight = 20).run {
        measure(100, 40)
        assert(expected = 2, actual = passes.size)
        assert(expected = exactly(50) to exactly(20), actual = passes[1])
    }

    @Test
    fun minWinsOverMaxWhenBothWouldApply() = Subject(minWidth = 150, maxWidth = 50).run {
        measure(100, 40)
        assert(expected = 150, actual = view.measuredWidth)
    }

    @Test
    fun swapAxesFeedsSwappedSpecsToTheView() = Subject().run {
        measure(100, 40, swapAxes = true)
        assert(expected = exactly(40) to exactly(100), actual = passes[0])
        assert(expected = 40, actual = view.measuredWidth)
        assert(expected = 100, actual = view.measuredHeight)
    }

    /** maxWidth bounds the view's own width, which a quarter turn shows as on screen height. */
    @Test
    fun swapAxesConstrainsInTheViewsOwnFrame() = Subject(maxWidth = 25).run {
        measure(100, 40, swapAxes = true)
        assert(expected = exactly(25) to exactly(100), actual = passes[1])
        assert(expected = 25, actual = view.measuredWidth)
        assert(expected = 100, actual = view.measuredHeight)
    }

    @Test
    fun quarterTurnRotationNormalisesTheAngle() {
        val view = View(context)
        fun rotatedTo(degrees: Float): Boolean {
            view.rotation = degrees
            return view.isQuarterTurnRotation
        }
        assert(expected = false, actual = rotatedTo(0f))
        assert(expected = true, actual = rotatedTo(90f))
        assert(expected = false, actual = rotatedTo(180f))
        assert(expected = true, actual = rotatedTo(270f))
        assert(expected = false, actual = rotatedTo(360f))
        assert(expected = true, actual = rotatedTo(-90f))
        assert(expected = true, actual = rotatedTo(-270f))
        assert(expected = true, actual = rotatedTo(450f))
    }

    /**
     * CSImageView is the only swapAxes caller left. Note it reports the pre-rotation box and
     * does not swap the measured size back, so it only lands correctly when the parent centres
     * it and tolerates the overflow, the way the FrameLayout in the vertical piano key does.
     */
    @Test
    fun rotatedImageViewMeasuresSwapped() {
        val view = CSImageView(context)
        view.measure(exactly(100), exactly(40))
        assert(expected = 100, actual = view.measuredWidth)
        assert(expected = 40, actual = view.measuredHeight)

        view.rotation = 270f
        view.forceLayout()
        view.measure(exactly(100), exactly(40))
        assert(expected = 40, actual = view.measuredWidth)
        assert(expected = 100, actual = view.measuredHeight)
    }

    /**
     * CSTextView does not pass swapAxes, so a rotated one measures like a plain TextView and
     * keeps the box its parent sized it for. Vertical text belongs in CSVerticalTextView,
     * which swaps the measured size back instead of reporting the rotated box.
     */
    @Test
    fun rotatedTextViewMeasuresLikeAPlainTextView() {
        val view = CSTextView(context)
        view.rotation = 90f
        view.measure(exactly(100), exactly(40))
        assert(expected = 100, actual = view.measuredWidth)
        assert(expected = 40, actual = view.measuredHeight)
    }

    @Test
    fun clipToOutlineDefaultsToTheViewsCurrentValue() {
        val view = View(context).apply { clipToOutline = true }
        CSAndroidViewBehavior(view, null)
        assert(expected = true, actual = view.clipToOutline)
    }

    @Test
    fun clipToOutlineDefaultCanBeOverriddenPerView() {
        val view = View(context).apply { clipToOutline = true }
        CSAndroidViewBehavior(view, null, defaultClipToOutline = false)
        assert(expected = false, actual = view.clipToOutline)
        assert(expected = false, actual = CSTextView(context).clipToOutline)
    }

    /** A leaf view overriding dispatchSet* to nothing at all still takes the parent's state. */
    private class MutedTextView(context: Context) : AppCompatTextView(context) {
        override fun dispatchSetSelected(selected: Boolean) = Unit
        override fun dispatchSetActivated(activated: Boolean) = Unit
        override fun dispatchSetPressed(pressed: Boolean) = Unit
    }

    /**
     * ViewGroup dispatches by calling child.setSelected/setActivated/setPressed, and those
     * set the child's own flag and refreshDrawableState() before calling dispatchSet*, which
     * only walks the child's own children. So a leaf cannot gate its own state there, and
     * dropping the overrides from CSTextView/CSImageView/CSEmptyView changed nothing.
     */
    @Test
    fun leafKeepsTakingParentStateWithDispatchSetFullyMuted() {
        val parent = CSFrameLayout(context)
        val muted = MutedTextView(context)
        val plain = CSTextView(context)
        parent.addView(muted)
        parent.addView(plain)

        parent.isSelected = true
        assert(expected = true, actual = muted.isSelected)
        assert(expected = true, actual = plain.isSelected)

        parent.isActivated = true
        assert(expected = true, actual = muted.isActivated)
        assert(expected = true, actual = plain.isActivated)

        parent.isPressed = true
        assert(expected = true, actual = muted.isPressed)
        assert(expected = true, actual = plain.isPressed)
    }

    /** dispatchState only stops a ViewGroup passing state on, never its own state. */
    @Test
    fun dispatchStateFalseStopsChildrenNotTheViewItself() {
        val parent = CSFrameLayout(context)
        val child = CSTextView(context)
        parent.addView(child)
        parent.behavior.dispatchState = false

        parent.isSelected = true
        assert(expected = true, actual = parent.isSelected)
        assert(expected = false, actual = child.isSelected)

        parent.isPressed = true
        assert(expected = true, actual = parent.isPressed)
        assert(expected = false, actual = child.isPressed)
    }
}
