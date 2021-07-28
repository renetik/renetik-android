package renetik.android.view.extensions

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import renetik.android.content.attributeFloat
import renetik.android.content.dpToPixel


fun <T : View> T.backgroundColor(@ColorInt value: Int) = apply {
    setBackgroundColor(value)
}

fun <T : View> T.backgroundTint(@ColorInt value: Int) = apply {
    backgroundTintList = ColorStateList.valueOf(value)
}

@RequiresApi(Build.VERSION_CODES.M)
fun <T : View> T.foregroundTint(@ColorInt value: Int) = apply {
    foregroundTintList = ColorStateList.valueOf(value)
}

fun <T : View> T.backgroundRoundedWithColor(@ColorInt value: Int, radius: Float = 8f) {
    val shape = GradientDrawable()
    shape.cornerRadius = radius
    shape.color = ColorStateList.valueOf(value)
    background = shape
}

fun <T : View> T.paddingDp(left: Int, top: Int, right: Int, bottom: Int) = apply {
    setPadding(
        context.dpToPixel(left), context.dpToPixel(top),
        context.dpToPixel(right), context.dpToPixel(bottom)
    )
}

fun <T : View> T.paddingDp(horizontal: Int = -1, vertical: Int = -1) = apply {
    if (horizontal >= 0) paddingHorizontal(dp = horizontal)
    if (vertical >= 0) paddingVertical(dp = horizontal)
}

fun <T : View> T.padding(dp: Int = -1, px: Int = -1) = apply {
    val pixelValue = if (dp > -0) context.dpToPixel(dp) else if (px >= 0) px else 0
    paddingDp(pixelValue, pixelValue, pixelValue, pixelValue)
}

fun <T : View> T.paddingHorizontal(dp: Int = -1, px: Int = -1) = apply {
    val pixelValue = if (dp > -0) context.dpToPixel(dp) else if (px >= 0) px else 0
    setPadding(pixelValue, paddingTop, pixelValue, paddingBottom)
}

fun <T : View> T.paddingVertical(dp: Int = -1, px: Int = -1) = apply {
    val pixelValue = if (dp > -0) context.dpToPixel(dp) else if (px >= 0) px else 0
    setPadding(paddingLeft, pixelValue, paddingRight, pixelValue)
}

fun View.disabledByAlphaIf(condition: Boolean) {
    disabledIf(condition)
    alpha = if (condition) context.attributeFloat(android.R.attr.disabledAlpha) else 1F
}
