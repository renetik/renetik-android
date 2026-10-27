package renetik.android.ui.picker

import android.os.SystemClock
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.shawnlin.numberpicker.NumberPicker.DESCENDING
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import renetik.android.core.lang.variable.assign
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.TestApplication
import renetik.android.testing.context
import renetik.android.ui.view.isVisible
import java.util.concurrent.TimeUnit.MILLISECONDS

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CSNumberPickerTest {

    private val WheelLength = 400
    private val WheelWidth = 200

    // Seven 64 px items with 24 px selected text. BandReleaseOffset is past half an item and more
    // than a text size from both items' centres, where NumberPicker 2.4.13 reported the value
    // flipping.
    private val BandReleaseOffset = 36
    private val SixItemLength = 73

    private val picker = CSNumberPicker(context)

    @Test
    fun valueSetAndGetWithinRange() {
        picker.min(0).max(10)
        picker.value = 7
        assert(expected = 7, actual = picker.value)
        assert(expected = 0, actual = picker.minValue)
        assert(expected = 10, actual = picker.maxValue)
    }

    @Test
    fun wheelConfiguration() {
        picker.circulate(true)
        assertTrue(picker.wrapSelectorWheel)
        picker.circulate(false)
        assertFalse(picker.wrapSelectorWheel)
    }

    @Test
    fun loadDisplaysDataAndSelection() {
        picker.load(listOf("alpha", "beta", "gamma"), selected = 1)
        assert(expected = 0, actual = picker.minValue)
        assert(expected = 2, actual = picker.maxValue)
        assert(expected = 3, actual = picker.displayedValues.size)
        assert(expected = "beta", actual = picker.displayedValues[picker.value])
        assertTrue(picker.isVisible)
    }

    @Test
    fun indexPropertySyncsToPicker() {
        picker.min(0).max(5)
        val selection = property(0)
        picker.index(selection)
        selection assign 3
        assert(expected = 3, actual = picker.value)
    }

    @Test
    fun loadWithPropertyUpdatesPickerSelection() {
        val choice = property("two")
        picker.load(choice, listOf("one" to "one", "two" to "two", "three" to "three"))
        assert(expected = 1, actual = picker.value)
    }

    @Test
    fun dragReleasedInsideBandChangesIndexOnce() {
        picker.min(0).max(99)
        picker.index assign 50
        val indexes = mutableListOf<Int>()
        picker.index.onChange { indexes += it }
        picker.layOutWheel(itemCount = 7)
        picker.dragAndRelease(BandReleaseOffset)
        assert(expected = listOf(49), actual = indexes)
    }

    @Test
    fun descendingIndexBindingKeepsValueEqualToProperty() {
        picker.order = DESCENDING
        picker.min(40).max(999)
        val tempo = property(120)
        picker.index(tempo)
        picker.layOutWheel(itemCount = 6)
        tempo assign 999
        assert(expected = 999, actual = picker.value)
        tempo assign 40
        assert(expected = 40, actual = picker.value)
        picker.dragAndRelease(SixItemLength)
        assert(expected = 41, actual = picker.value)
        assert(expected = picker.value, actual = tempo.value)
    }

    private fun CSNumberPicker.layOutWheel(itemCount: Int) {
        wheelItemCount = itemCount
        setTextSize(16f)
        setSelectedTextSize(24f)
        FrameLayout(context).addView(this)
        measure(makeMeasureSpec(WheelWidth, EXACTLY),
            makeMeasureSpec(WheelLength, EXACTLY))
        layout(0, 0, WheelWidth, WheelLength)
    }

    private fun CSNumberPicker.dragAndRelease(offset: Int) {
        val downTime = SystemClock.uptimeMillis()
        var position = WheelLength / 2
        touch(ACTION_DOWN, downTime, position)
        position += ViewConfiguration.get(context).scaledTouchSlop + 1
        repeat(offset + 1) {
            advanceClock(50)
            touch(ACTION_MOVE, downTime, position++)
        }
        advanceClock(200)
        touch(ACTION_UP, downTime, position - 1)
        repeat(100) {
            advanceClock(16)
            computeScroll()
        }
    }

    private fun CSNumberPicker.touch(action: Int, downTime: Long, position: Int) {
        val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action,
            WheelWidth / 2f, position.toFloat(), 0)
        dispatchTouchEvent(event)
        event.recycle()
    }

    private fun advanceClock(millis: Long) = ShadowLooper.idleMainLooper(millis, MILLISECONDS)
}
