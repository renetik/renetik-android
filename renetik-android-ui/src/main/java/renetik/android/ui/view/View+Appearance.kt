@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import renetik.android.core.android.content.attributeColor
import renetik.android.core.android.content.dpToPixel
import renetik.android.core.android.graphics.CSColor
import renetik.android.core.android.graphics.toAlpha
import renetik.android.event.change.CSHasChangeValue
import renetik.android.event.change.action
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistration.Companion.CSRegistration
import renetik.android.ui.R
import renetik.android.ui.widget.layoutMatch

fun <T : View> T.background(value: Drawable) = apply {
    background = value
}

fun <T : View> T.background(@DrawableRes value: Int) = apply {
    setBackgroundResource(value)
}

fun <T : View> T.backgroundColor(@ColorInt value: Int) = apply {
    setBackgroundColor(value)
}

fun <T : View> T.backgroundColorAttr(@AttrRes attribute: Int) =
    backgroundColor(context.attributeColor(attribute))

fun <T : View> T.background(value: CSColor) = apply {
    setBackgroundColor(value.color)
}

fun <T : View> T.backgroundTint(@ColorInt value: Int) = apply {
    backgroundTintList = ColorStateList.valueOf(value)
}

fun <T : View> T.backgroundAlpha(alpha: Double) = apply {
    background = background.toAlpha(alpha)
}

fun <T : View> T.foregroundAlpha(alpha: Double) = apply {
    foreground = foreground.toAlpha(alpha)
}

fun <T : View> T.foregroundTint(@ColorInt value: Int) = apply {
    foregroundTintList = ColorStateList.valueOf(value)
}

val <T : View> T.foregroundTint: Int get() = foregroundTintList!!.defaultColor

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

fun <T : View> T.paddingDp(horizontal: Float = -1f, vertical: Float = -1f) = apply {
    if (horizontal >= 0) paddingHorizontal(dp = horizontal)
    if (vertical >= 0) paddingVertical(dp = vertical)
}

fun <T : View> T.paddingPx(horizontal: Int = -1, vertical: Int = -1) = apply {
    if (horizontal >= 0) paddingHorizontal(px = horizontal)
    if (vertical >= 0) paddingVertical(px = vertical)
}

fun <T : View> T.padding(dp: Int = -1, px: Int = -1) = apply {
    val pixelValue = if (dp > -0) context.dpToPixel(dp) else if (px >= 0) px else 0
    paddingDp(pixelValue, pixelValue, pixelValue, pixelValue)
}

fun <T : View> T.paddingHorizontal(dp: Float = -1f, px: Int = -1) = apply {
    val pixelValue = if (dp > -0) context.dpToPixel(dp) else if (px >= 0) px else 0
    setPadding(pixelValue, paddingTop, pixelValue, paddingBottom)
}

fun <T : View> T.paddingVertical(dp: Float = -1f, px: Int = -1) = apply {
    val pixelValue = if (dp > -0) context.dpToPixel(dp) else if (px >= 0) px else 0
    setPadding(paddingLeft, pixelValue, paddingRight, pixelValue)
}

fun View.enabledByAlpha(condition: Boolean = true, disable: Boolean = true) =
    disabledByAlpha(!condition, disable)

fun View.disabledByAlpha(
    condition: Boolean = true, disable: Boolean = true, normal: Float = 1F
) {
    if (disable) disabledIf(condition)
    alphaToDisabled(condition, normal)
}

fun View.alphaToDisabled(value: Boolean = true, normal: Float = 1F) {
    alpha = if (value) context.disabledAlpha else normal
}

inline fun View.enabledIf(
    property: CSHasChangeValue<Boolean>
): CSRegistration = property.action { enabled(it) }

val Context.disabledAlpha
    get() = resources.getString(R.string.cs_disabled_alpha).toFloat()

inline fun <T> View.enabledByAlphaIf(
    property: CSHasChangeValue<T>, disable: Boolean = true,
    crossinline condition: (T) -> Boolean,
): CSRegistration = property.action { enabledByAlpha(condition(it), disable) }

fun View.enabledByAlphaIf(
    property: CSHasChangeValue<Boolean>, disable: Boolean = true
): CSRegistration = enabledByAlphaIf(property, disable) { it }

fun View.disabledByAlphaIf(
    property: CSHasChangeValue<Boolean>,
    disable: Boolean = true, normal: Float = 1F
): CSRegistration = disabledByAlphaIf(property, disable, normal) { it }

fun FrameLayout.disabledByOverlayIf(
    property: CSHasChangeValue<Boolean>
): CSRegistration {
    val overlay = View(context).apply {
        backgroundColorAttr(android.R.attr.windowBackground)
        alpha = 0.5f
        isClickable = true
    }
    val registration = property.action { isTrue ->
        if (isTrue) add(overlay, layoutMatch)
        else overlay.removeFromSuperview()
    }
    return CSRegistration(onCancel = {
        overlay.removeFromSuperview()
        registration.cancel()
    })
}

fun View.disabledByAlphaIfNot(
    property: CSHasChangeValue<Boolean>, disable: Boolean = true,
) = disabledByAlphaIf(property, disable) { !it }

fun <T> View.disabledByAlphaIf(
    property: CSHasChangeValue<T>, disable: Boolean = true,
    normal: Float = 1F, condition: (T) -> Boolean,
): CSRegistration = property.action { disabledByAlpha(condition(it), disable, normal) }

inline fun <T> View.enabledByAlphaIf(
    property1: CSHasChangeValue<T>, property2: CSHasChangeValue<*>,
    crossinline condition: (T) -> Boolean,
) = enabledByAlphaIf(property1, property2) { first, _ -> condition(first) }

fun <T, V> View.enabledByAlphaIf(
    property1: CSHasChangeValue<T>, property2: CSHasChangeValue<V>,
    condition: (T, V) -> Boolean,
): CSRegistration {
    fun update() = enabledByAlpha(condition(property1.value, property2.value))
    update()
    return CSRegistration(
        property1.onChange { update() }, property2.onChange { update() })
}

fun <T, V> View.disabledByAlphaIf(
    property1: CSHasChangeValue<T>, property2: CSHasChangeValue<V>,
    disable: Boolean = true, condition: (T, V) -> Boolean,
): CSRegistration {
    val originalAlpha = alpha
    fun update() = disabledByAlpha(
        condition(property1.value, property2.value), disable, originalAlpha
    )
    update()
    return CSRegistration(
        property1.onChange { update() }, property2.onChange { update() })
}

fun <T, V, X> View.disabledByAlphaIf(
    property1: CSHasChangeValue<T>, property2: CSHasChangeValue<V>,
    property3: CSHasChangeValue<X>, disable: Boolean = true,
    condition: (T, V, X) -> Boolean,
): CSRegistration {
    fun update() = disabledByAlpha(
        condition(property1.value, property2.value, property3.value), disable
    )
    update()
    return CSRegistration(property1.onChange { update() },
        property2.onChange { update() }, property3.onChange { update() })
}

fun View.layoutToMatchParent() = apply {
    layoutParams.width = MATCH_PARENT
    layoutParams.height = MATCH_PARENT
}
