@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.core.kotlin.primitives

import renetik.android.core.android.content.dpToPixelF
import renetik.android.core.lang.CSEnvironment.app
import renetik.android.core.lang.value.CSValue
import java.math.RoundingMode
import java.math.RoundingMode.CEILING
import java.math.RoundingMode.UP
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

val Float.Companion.Empty get() = MAX_VALUE
val Float.isWhole: Boolean get() = this % 1.0 == 0.0
val Float.isEmpty get() = this == Float.Empty
val Float.isSet get() = !this.isEmpty
fun Float.ifEmpty(function: (Float) -> Unit) = apply {
    if (this.isEmpty) function(this)
}

fun Float.ifSet(function: (Float) -> Unit) = apply {
    if (this.isSet) function(this)
}

fun Float.roundToStep(step: Float): Float = (this / step).roundToLong() * step
fun Float.roundToStep(step: Double): Double = (this / step).roundToLong() * step
fun Float.roundToStep(step: Int): Int = (this / step.toFloat()).toInt() * step

inline fun Float.roundToDecimalPlaces(decimalPlaces: Int): Float {
    val factor = 10.0.pow(decimalPlaces.toDouble())
    return (this * factor).roundToInt() / factor.toFloat()
}

inline fun Float.roundToDecimal(
    decimalPlaces: Int, mode: RoundingMode = UP,
): Float = formatRoundDecimal(
    "#." + Array(decimalPlaces) { "#" }.joinToString(separator = ""), mode
).toFloat()

inline fun Float.formatDecimal(n: Int): String {
    return "%.${n}f".format(this)
}

/**
 * With [minimum] to [maximum] decimals in the default locale, without grouping, rounded first so
 * that zero never carries a sign.
 */
fun Float.formatDecimals(minimum: Int, maximum: Int = minimum): String =
    NumberFormat.getInstance().apply {
        minimumFractionDigits = minimum
        maximumFractionDigits = maximum
        isGroupingUsed = false
    }.format(roundTo(maximum))

//fun Float.roundTo(decimals: Int): Float =
//    formatDecimal(decimals).toFloat()

fun Float.roundTo(decimals: Int): Float {
    val factor = 10f.pow(decimals)
    return (this * factor).roundToInt() / factor
}

inline fun Float.removeToDecimal(n: Int): Float {
    return formatDecimal(n).toFloat()
}

inline fun Float.formatRoundDecimal(
    format: String = "#.##",
    mode: RoundingMode = CEILING,
): String = DecimalFormat(format).apply { roundingMode = mode }.format(this)

inline val Float.rest: Float
    get() = toString().let {
        if ("." in it) {
            val value = it.substringAfter(".")
            value.toInt() / value.length.toFloat()
        } else 0f
    }

inline fun Float.rest(value: Int): Float =
    if (value > 0) this % value else this

inline fun Float.rest(value: CSValue<Int>): Float = rest(value.value)
inline fun CSValue<Float>.rest(value: CSValue<Int>): Float = this.value.rest(value.value)

inline fun Float.ceil(): Int = ceil(this).toInt()
inline fun Float.min(minimum: Float) = coerceAtLeast(minimum)
inline fun Float.max(maximum: Float) = coerceAtMost(maximum)
inline fun Float.coerceTo(range: ClosedFloatingPointRange<Float>) =
    min(range.start).max(range.endInclusive)


inline val Float.dp: Float get() = app.dpToPixelF(this)
inline val Float.dpf: Float get() = app.dpToPixelF(this)