package renetik.android.ui.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

/** Draws [icon] at the text size in the text color, so it follows auto-sized text. */
class CSTextIconSpan(icon: Drawable) : ReplacementSpan() {
    private val icon = icon.mutate()

    override fun getSize(
        paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?
    ): Int {
        fm?.let { paint.getFontMetricsInt(it) }
        return iconWidth(paint)
    }

    override fun draw(
        canvas: Canvas, text: CharSequence?, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        val height = iconHeight(paint)
        val metrics = paint.fontMetricsInt
        val iconTop = y + (metrics.ascent + metrics.descent - height) / 2
        icon.setBounds(0, 0, iconWidth(paint), height)
        icon.setTint(paint.color)
        canvas.save()
        canvas.translate(x, iconTop.toFloat())
        icon.draw(canvas)
        canvas.restore()
    }

    private fun iconHeight(paint: Paint) = paint.textSize.roundToInt()

    private fun iconWidth(paint: Paint) =
        (iconHeight(paint) * icon.intrinsicWidth / icon.intrinsicHeight.toFloat()).roundToInt()
}

fun SpannableStringBuilder.appendIcon(icon: Drawable): SpannableStringBuilder =
    append(" ", CSTextIconSpan(icon), SPAN_EXCLUSIVE_EXCLUSIVE)
