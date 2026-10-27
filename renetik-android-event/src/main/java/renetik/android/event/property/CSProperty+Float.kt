package renetik.android.event.property

fun <P : CSProperty<Float>> P.min(value: Float): P = apply {
    if (this.value < value) this.value(value, fire = false)
}

fun <P : CSProperty<Float>> P.max(value: Float): P = apply {
    if (this.value > value) this.value(value, fire = false)
}

fun <P : CSProperty<Float>> P.range(range: ClosedRange<Float>): P = apply {
    min(range.start).max(range.endInclusive)
}