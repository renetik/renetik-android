package renetik.android.preset.property

import renetik.android.event.property.CSProperty
import renetik.android.event.property.computedFrom

fun <T : CSProperty<Int>> T.coerce(min: Int, max: Int) =
    computedFrom(from = { it.coerceIn(min, max) })

fun <T : CSProperty<Int>> T.coerce(range: ClosedRange<Int>) =
    computedFrom(from = { it.coerceIn(range) })

@JvmName("coerceFloat")
fun <T : CSProperty<Float>> T.coerce(range: ClosedRange<Float>) =
    computedFrom(from = { it.coerceIn(range) })

fun <T : CSProperty<Int>> T.max(value: Int) =
    computedFrom(from = { it.coerceAtMost(value) })

fun <T : CSProperty<Float?>> T.max(value: Float?) =
    computedFrom(from = { from ->
        value?.let { from?.coerceAtMost(value) } ?: from
    })

fun <T : CSProperty<Int>> T.min(value: Int) =
    computedFrom(from = { it.coerceAtLeast(value) })

fun <T : CSProperty<Float?>> T.min(value: Float?) =
    computedFrom(from = { from ->
        value?.let { from?.coerceAtLeast(value) } ?: from
    })