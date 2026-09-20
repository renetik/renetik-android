package renetik.android.ui.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.Gravity
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.TestApplication
import renetik.android.testing.context

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CSVerticalTextViewTest {

    private fun autoSizing(text: CharSequence?, hint: CharSequence? = null) =
        CSVerticalTextView(context).apply {
            this.text = text
            this.hint = hint
            setTextSize(COMPLEX_UNIT_PX, 30f)
            isAutoSized = true
            measure(makeMeasureSpec(40, EXACTLY), makeMeasureSpec(400, EXACTLY))
        }

    // adjustTextSize puts its 100f probe size on the paint before measuring the glyphs, and
    // Robolectric's Paint.getTextBounds reports 0x0 so it stops right there. That leaves
    // textSize == 100f as the signal that the branch ran, and 30f that it did not. The gate
    // used to read !content.isNotEmpty(), which had these two backwards.

    @Test
    fun autoSizeRunsForText() {
        assert(expected = 100f, actual = autoSizing("C4").textSize)
    }

    @Test
    fun autoSizeRunsForHintWhenTextIsEmpty() {
        assert(expected = 100f, actual = autoSizing("", hint = "C4").textSize)
    }

    @Test
    fun autoSizeSkippedWhenThereIsNothingToSize() {
        assert(expected = 30f, actual = autoSizing("").textSize)
    }

    /** Empty text with no hint leaves content null, which used to throw inside the gate. */
    @Test
    fun nullTextAndNullHintDoNotThrow() {
        assert(expected = 30f, actual = autoSizing(null).textSize)
    }

    /** Unlike a rotated plain TextView, this reports the on screen box, not the rotated one. */
    @Test
    fun measuredSizeIsSwappedBackToTheOnScreenBox() {
        val view = CSVerticalTextView(context).apply { text = "C4" }
        view.measure(makeMeasureSpec(40, EXACTLY), makeMeasureSpec(400, EXACTLY))
        assert(expected = 40, actual = view.measuredWidth)
        assert(expected = 400, actual = view.measuredHeight)
    }

    private class Op(val kind: Char, val a: Float, val b: Float)

    private class RecordingCanvas(bitmap: Bitmap) : Canvas(bitmap) {
        val ops = mutableListOf<Op>()
        override fun translate(dx: Float, dy: Float) {
            ops += Op('t', dx, dy); super.translate(dx, dy)
        }

        override fun rotate(degrees: Float) {
            ops += Op('r', degrees, 0f); super.rotate(degrees)
        }
    }

    /**
     * Where the text ink actually lands on screen: replays the transform onDraw sets up
     * through a Matrix, so the expected values below do not depend on onDraw's arithmetic.
     */
    private fun inkOnScreen(
        gravity: Int, padding: Rect = Rect(), clockwise: Boolean = false,
        text: CharSequence = "A", width: Int = 40, height: Int = 300
    ): RectF {
        val view = CSVerticalTextView(context).apply {
            this.text = text
            isRotatedClockwise = clockwise
            this.gravity = gravity
            setPadding(padding.left, padding.top, padding.right, padding.bottom)
            measure(makeMeasureSpec(width, EXACTLY), makeMeasureSpec(height, EXACTLY))
            layout(0, 0, width, height)
        }
        val canvas = RecordingCanvas(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888))
        view.draw(canvas)
        val rotateAt = canvas.ops.indexOfFirst { it.kind == 'r' }
        val transform = Matrix()
        for (op in canvas.ops.subList(rotateAt - 1, rotateAt + 2))
            if (op.kind == 't') transform.preTranslate(op.a, op.b) else transform.preRotate(op.a)
        val layout = view.layout
        val lines = 0 until layout.lineCount
        return RectF(
            lines.minOf(layout::getLineLeft), 0f,
            lines.maxOf(layout::getLineRight), layout.height.toFloat()
        ).also { transform.mapRect(it) }
    }

    private val tolerance = 0.01f

    @Test
    fun horizontalGravityPlacesTextAcrossTheColumn() {
        assertEquals(0f, inkOnScreen(Gravity.START).left, tolerance)
        assertEquals(40f, inkOnScreen(Gravity.END).right, tolerance)
        val centred = inkOnScreen(Gravity.CENTER)
        assertEquals((40f - centred.width()) / 2f, centred.left, tolerance)
    }

    @Test
    fun verticalGravityPlacesTextAlongTheColumn() {
        assertEquals(0f, inkOnScreen(Gravity.TOP).top, tolerance)
        assertEquals(300f, inkOnScreen(Gravity.BOTTOM).bottom, tolerance)
        val centred = inkOnScreen(Gravity.CENTER)
        assertEquals((300f - centred.height()) / 2f, centred.top, tolerance)
    }

    @Test
    fun paddingInsetsTextFromTheColumnEdges() {
        assertEquals(7f, inkOnScreen(Gravity.START, Rect(7, 0, 0, 0)).left, tolerance)
        assertEquals(31f, inkOnScreen(Gravity.END, Rect(0, 0, 9, 0)).right, tolerance)
        assertEquals(11f, inkOnScreen(Gravity.TOP, Rect(0, 11, 0, 0)).top, tolerance)
        assertEquals(287f, inkOnScreen(Gravity.BOTTOM, Rect(0, 0, 0, 13)).bottom, tolerance)
    }

    /** Rotation direction decides which way the run reads, not where it sits. */
    @Test
    fun bothRotationDirectionsLandInTheSamePlace() {
        val gravity = Gravity.END or Gravity.TOP
        val padding = Rect(0, 5, 9, 0)
        val counterClockwise = inkOnScreen(gravity, padding, clockwise = false)
        val clockwise = inkOnScreen(gravity, padding, clockwise = true)
        assertEquals(counterClockwise.left, clockwise.left, tolerance)
        assertEquals(counterClockwise.top, clockwise.top, tolerance)
        assertEquals(counterClockwise.right, clockwise.right, tolerance)
        assertEquals(counterClockwise.bottom, clockwise.bottom, tolerance)
    }

    /** Several lines are placed as one block, keeping how they line up against each other. */
    @Test
    fun multiLineTextIsPlacedAsOneBlock() {
        val single = inkOnScreen(Gravity.CENTER, text = "A")
        val multi = inkOnScreen(Gravity.CENTER, text = "A\nBBB")
        assert(expected = true, actual = multi.width() > single.width())
        assertEquals((300f - multi.height()) / 2f, multi.top, tolerance)
        assertEquals((40f - multi.width()) / 2f, multi.left, tolerance)
    }
}
