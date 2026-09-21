package renetik.android.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withSave
import androidx.core.widget.TextViewCompat
import renetik.android.ui.R
import renetik.android.ui.R.styleable.CSLayout_isRotatedClockwise
import renetik.android.ui.widget.text
import kotlin.math.abs

/**
 * TextView drawn a quarter turn round, reading bottom to top, or top to bottom with
 * [isRotatedClockwise]. It measures to its on screen box, so `android:gravity` and padding
 * work in screen terms: `start`/`end` move the text across the column, `top`/`bottom` along it.
 *
 * Several lines stack across the column, and horizontal gravity also sets how they line up
 * against each other along the run.
 */
class CSVerticalTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private val textBounds = Rect()
    private var minTextSize = 12
    private var maxTextSize = 112

    var isRotatedClockwise: Boolean = false
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    var isAutoSized: Boolean = false
        set(value) {
            field = value
            if (value) requestLayout()
        }

    init {
        includeFontPadding = false
        context.theme.obtainStyledAttributes(attrs, R.styleable.CSLayout, 0, 0).let {
            isRotatedClockwise = it.getBoolean(CSLayout_isRotatedClockwise, isRotatedClockwise)
            it.recycle()
        }
        @SuppressLint("RestrictedApi")
        run {
            isAutoSized = autoSizeTextType == 1
            minTextSize = autoSizeMinTextSize
            maxTextSize = autoSizeMaxTextSize
            super.setAutoSizeTextTypeWithDefaults(TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val content = if (text.isNullOrEmpty()) hint else text()
        if (isAutoSized && parentHeight > 0 && parentWidth > 0 && !content.isNullOrEmpty()) {
            adjustTextSize(parentHeight, parentWidth, content.toString())
        }
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    private fun adjustTextSize(parentHeight: Int, parentWidth: Int, content: String) {
        val targetTextWidth =
            parentHeight.toFloat() - (paddingTop + paddingBottom)
        val targetTextHeight = parentWidth.toFloat() - (paddingLeft + paddingRight)
        val testPaint = paint
        testPaint.textSize = 100f
        testPaint.getTextBounds(content, 0, content.length, textBounds)
        val currentVisualHeight = textBounds.height().toFloat()
        val currentVisualWidth = textBounds.width().toFloat()
        if (currentVisualHeight > 0 && currentVisualWidth > 0) {
            val scaleX = targetTextWidth / currentVisualWidth
            val scaleY = targetTextHeight / currentVisualHeight
            var newSize = 100f * minOf(scaleX, scaleY) * 0.9f
            newSize = newSize.coerceIn(minTextSize.toFloat(), maxTextSize.toFloat())
            if (abs(textSize - newSize) > 1f) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize)
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    override fun onDraw(canvas: Canvas) {
        val isHint = text.isNullOrEmpty()
        if (isHint && hint.isNullOrEmpty()) return
        val textLayout = if (isHint) makeHintLayout() else layout ?: return

        // Rotated run: its height is the on-screen width, its width the on-screen height.
        // Placed by its ink, not by the full width line, so that the alignment the layout
        // applied along the run does not decide where the text sits. Gravity decides.
        val lines = 0 until textLayout.lineCount
        val inkStart = lines.minOf(textLayout::getLineLeft)
        val inkEnd = lines.maxOf(textLayout::getLineRight)
        val blockWidth = textLayout.height.toFloat()
        val blockHeight = inkEnd - inkStart
        val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
        val left = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL ->
                paddingLeft + (width - paddingLeft - paddingRight - blockWidth) / 2f
            Gravity.RIGHT -> width - paddingRight - blockWidth
            else -> paddingLeft.toFloat()
        }
        val top = when (absoluteGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.CENTER_VERTICAL ->
                paddingTop + (height - paddingTop - paddingBottom - blockHeight) / 2f
            Gravity.BOTTOM -> height - paddingBottom - blockHeight
            else -> paddingTop.toFloat()
        }

        canvas.withSave {
            if (isRotatedClockwise) {
                translate(width.toFloat(), 0f)
                rotate(90f)
                translate(top - inkStart, width - left - blockWidth)
            } else {
                translate(0f, height.toFloat())
                rotate(-90f)
                translate(height - inkEnd - top, left)
            }
            paint.color = if (isHint) currentHintTextColor else currentTextColor
            paint.drawableState = drawableState
            textLayout.draw(this)
        }
    }

    private fun makeHintLayout(): Layout {
        val availableWidth = height - paddingTop - paddingBottom
        val alignment = layout?.alignment ?: Layout.Alignment.ALIGN_NORMAL
        return StaticLayout.Builder.obtain(hint, 0, hint.length, paint, availableWidth)
            .setAlignment(alignment)
            .setIncludePad(includeFontPadding)
            .build()
    }
}