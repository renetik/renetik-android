@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.event.change

import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.tuples.CSQuadruple
import renetik.android.core.lang.tuples.CSQuintuple
import renetik.android.core.lang.tuples.CSSixtuple
import renetik.android.event.property.CSSafeHasChangeValue
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrationsMap

private class ChangeSource<T>(
    val value: () -> T,
    val listen: ((T) -> Unit) -> CSRegistration,
)

private fun <T> CSHasChangeValue<T>.changeSource(): ChangeSource<T> =
    ChangeSource(value = { value }, listen = { onChange(it) })

private fun <T> CSSafeHasChangeValue<T>.unsafeChangeSource(): ChangeSource<T> =
    ChangeSource(value = { value }, listen = { onUnsafeChange(it) })

private fun <Argument1, Argument2> onUnsafeChangeSerialized(
    id: String,
    delivery: CSSafeChangeDelivery,
    first: ChangeSource<Argument1>,
    second: ChangeSource<Argument2>,
    onUnsafeChange: (Argument1, Argument2) -> Unit,
): CSRegistration {
    val registrations = CSRegistrationsMap(id)
    val emitter = delivery.emitter<Pair<Argument1, Argument2>>(registrations) {
        onUnsafeChange(it.first, it.second)
    }
    var value1 = first.value()
    var value2 = second.value()
    registrations.register(first.listen { newValue ->
        emitter.enqueue {
            value1 = newValue
            value1 to value2
        }
    })
    registrations.register(second.listen { newValue ->
        emitter.enqueue {
            value2 = newValue
            value1 to value2
        }
    })
    emitter.update {
        value1 = first.value()
        value2 = second.value()
    }
    return registrations
}

private fun <Argument1, Argument2, Argument3> onUnsafeChangeSerialized(
    id: String,
    delivery: CSSafeChangeDelivery,
    first: ChangeSource<Argument1>,
    second: ChangeSource<Argument2>,
    third: ChangeSource<Argument3>,
    onUnsafeChange: (Argument1, Argument2, Argument3) -> Unit,
): CSRegistration {
    val registrations = CSRegistrationsMap(id)
    val emitter = delivery.emitter<Triple<Argument1, Argument2, Argument3>>(registrations) {
        onUnsafeChange(it.first, it.second, it.third)
    }
    var value1 = first.value()
    var value2 = second.value()
    var value3 = third.value()
    registrations.register(first.listen { newValue ->
        emitter.enqueue {
            value1 = newValue
            Triple(value1, value2, value3)
        }
    })
    registrations.register(second.listen { newValue ->
        emitter.enqueue {
            value2 = newValue
            Triple(value1, value2, value3)
        }
    })
    registrations.register(third.listen { newValue ->
        emitter.enqueue {
            value3 = newValue
            Triple(value1, value2, value3)
        }
    })
    emitter.update {
        value1 = first.value()
        value2 = second.value()
        value3 = third.value()
    }
    return registrations
}

private fun <Argument1, Argument2, Argument3, Argument4> onUnsafeChangeSerialized(
    id: String,
    delivery: CSSafeChangeDelivery,
    first: ChangeSource<Argument1>,
    second: ChangeSource<Argument2>,
    third: ChangeSource<Argument3>,
    fourth: ChangeSource<Argument4>,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4) -> Unit,
): CSRegistration {
    val registrations = CSRegistrationsMap(id)
    val emitter = delivery.emitter<CSQuadruple<Argument1, Argument2, Argument3, Argument4>>(
        registrations
    ) {
        onUnsafeChange(it.first, it.second, it.third, it.fourth)
    }
    var value1 = first.value()
    var value2 = second.value()
    var value3 = third.value()
    var value4 = fourth.value()
    registrations.register(first.listen { newValue ->
        emitter.enqueue {
            value1 = newValue
            CSQuadruple(value1, value2, value3, value4)
        }
    })
    registrations.register(second.listen { newValue ->
        emitter.enqueue {
            value2 = newValue
            CSQuadruple(value1, value2, value3, value4)
        }
    })
    registrations.register(third.listen { newValue ->
        emitter.enqueue {
            value3 = newValue
            CSQuadruple(value1, value2, value3, value4)
        }
    })
    registrations.register(fourth.listen { newValue ->
        emitter.enqueue {
            value4 = newValue
            CSQuadruple(value1, value2, value3, value4)
        }
    })
    emitter.update {
        value1 = first.value()
        value2 = second.value()
        value3 = third.value()
        value4 = fourth.value()
    }
    return registrations
}

private fun <Argument1, Argument2, Argument3, Argument4, Argument5>
        onUnsafeChangeSerialized(
    id: String,
    delivery: CSSafeChangeDelivery,
    first: ChangeSource<Argument1>,
    second: ChangeSource<Argument2>,
    third: ChangeSource<Argument3>,
    fourth: ChangeSource<Argument4>,
    fifth: ChangeSource<Argument5>,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4, Argument5) -> Unit,
): CSRegistration {
    val registrations = CSRegistrationsMap(id)
    val emitter = delivery.emitter<CSQuintuple<Argument1, Argument2,
            Argument3, Argument4, Argument5>>(registrations) {
        onUnsafeChange(it.first, it.second, it.third, it.fourth, it.fifth)
    }
    var value1 = first.value()
    var value2 = second.value()
    var value3 = third.value()
    var value4 = fourth.value()
    var value5 = fifth.value()
    registrations.register(first.listen { newValue ->
        emitter.enqueue {
            value1 = newValue
            CSQuintuple(value1, value2, value3, value4, value5)
        }
    })
    registrations.register(second.listen { newValue ->
        emitter.enqueue {
            value2 = newValue
            CSQuintuple(value1, value2, value3, value4, value5)
        }
    })
    registrations.register(third.listen { newValue ->
        emitter.enqueue {
            value3 = newValue
            CSQuintuple(value1, value2, value3, value4, value5)
        }
    })
    registrations.register(fourth.listen { newValue ->
        emitter.enqueue {
            value4 = newValue
            CSQuintuple(value1, value2, value3, value4, value5)
        }
    })
    registrations.register(fifth.listen { newValue ->
        emitter.enqueue {
            value5 = newValue
            CSQuintuple(value1, value2, value3, value4, value5)
        }
    })
    emitter.update {
        value1 = first.value()
        value2 = second.value()
        value3 = third.value()
        value4 = fourth.value()
        value5 = fifth.value()
    }
    return registrations
}

private fun <Argument1, Argument2, Argument3, Argument4, Argument5, Argument6>
        onUnsafeChangeSerialized(
    id: String,
    delivery: CSSafeChangeDelivery,
    first: ChangeSource<Argument1>,
    second: ChangeSource<Argument2>,
    third: ChangeSource<Argument3>,
    fourth: ChangeSource<Argument4>,
    fifth: ChangeSource<Argument5>,
    sixth: ChangeSource<Argument6>,
    onUnsafeChange: (
        Argument1, Argument2, Argument3, Argument4, Argument5, Argument6
    ) -> Unit,
): CSRegistration {
    val registrations = CSRegistrationsMap(id)
    val emitter = delivery.emitter<CSSixtuple<Argument1, Argument2,
            Argument3, Argument4, Argument5, Argument6>>(registrations) {
        onUnsafeChange(it.first, it.second, it.third, it.fourth, it.fifth, it.sixth)
    }
    var value1 = first.value()
    var value2 = second.value()
    var value3 = third.value()
    var value4 = fourth.value()
    var value5 = fifth.value()
    var value6 = sixth.value()
    registrations.register(first.listen { newValue ->
        emitter.enqueue {
            value1 = newValue
            CSSixtuple(value1, value2, value3, value4, value5, value6)
        }
    })
    registrations.register(second.listen { newValue ->
        emitter.enqueue {
            value2 = newValue
            CSSixtuple(value1, value2, value3, value4, value5, value6)
        }
    })
    registrations.register(third.listen { newValue ->
        emitter.enqueue {
            value3 = newValue
            CSSixtuple(value1, value2, value3, value4, value5, value6)
        }
    })
    registrations.register(fourth.listen { newValue ->
        emitter.enqueue {
            value4 = newValue
            CSSixtuple(value1, value2, value3, value4, value5, value6)
        }
    })
    registrations.register(fifth.listen { newValue ->
        emitter.enqueue {
            value5 = newValue
            CSSixtuple(value1, value2, value3, value4, value5, value6)
        }
    })
    registrations.register(sixth.listen { newValue ->
        emitter.enqueue {
            value6 = newValue
            CSSixtuple(value1, value2, value3, value4, value5, value6)
        }
    })
    emitter.update {
        value1 = first.value()
        value2 = second.value()
        value3 = third.value()
        value4 = fourth.value()
        value5 = fifth.value()
        value6 = sixth.value()
    }
    return registrations
}

@JvmName("onUnsafeChangeWithSafeSecond")
fun <Argument1, Argument2, Item1, Item2> Pair<Item1, Item2>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSSafeHasChangeValue<Argument2> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.unsafeChangeSource(),
        onUnsafeChange)

@JvmName("onUnsafeChangeWithSafeThird")
fun <Argument1, Argument2, Argument3, Item1, Item2, Item3>
        Triple<Item1, Item2, Item3>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSSafeHasChangeValue<Argument3> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWithSafeFourth")
fun <Argument1, Argument2, Argument3, Argument4,
        Item1, Item2, Item3, Item4>
        CSQuadruple<Item1, Item2, Item3, Item4>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSHasChangeValue<Argument3>,
              Item4 : CSSafeHasChangeValue<Argument4> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.changeSource(), fourth.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWithSafeFifth")
fun <Argument1, Argument2, Argument3, Argument4, Argument5,
        Item1, Item2, Item3, Item4, Item5>
        CSQuintuple<Item1, Item2, Item3, Item4, Item5>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4, Argument5) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSHasChangeValue<Argument3>,
              Item4 : CSHasChangeValue<Argument4>,
              Item5 : CSSafeHasChangeValue<Argument5> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.changeSource(), fourth.changeSource(), fifth.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWithSafeSixth")
fun <Argument1, Argument2, Argument3, Argument4, Argument5, Argument6,
        Item1, Item2, Item3, Item4, Item5, Item6>
        CSSixtuple<Item1, Item2, Item3, Item4, Item5, Item6>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (
        Argument1, Argument2, Argument3, Argument4, Argument5, Argument6
    ) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSHasChangeValue<Argument3>,
              Item4 : CSHasChangeValue<Argument4>,
              Item5 : CSHasChangeValue<Argument5>,
              Item6 : CSSafeHasChangeValue<Argument6> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.changeSource(), fourth.changeSource(), fifth.changeSource(), sixth.unsafeChangeSource(),
        onUnsafeChange)

@JvmName("onUnsafeChangeWith2Safe2")
fun <Argument1, Argument2, Item1, Item2> Pair<Item1, Item2>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2) -> Unit,
): CSRegistration
        where Item1 : CSSafeHasChangeValue<Argument1>,
              Item2 : CSSafeHasChangeValue<Argument2> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.unsafeChangeSource(), second.unsafeChangeSource(),
        onUnsafeChange)

@JvmName("onUnsafeChangeWith3Safe2")
fun <Argument1, Argument2, Argument3, Item1, Item2, Item3>
        Triple<Item1, Item2, Item3>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSSafeHasChangeValue<Argument2>,
              Item3 : CSSafeHasChangeValue<Argument3> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.unsafeChangeSource(),
        third.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWith4Safe2")
fun <Argument1, Argument2, Argument3, Argument4,
        Item1, Item2, Item3, Item4>
        CSQuadruple<Item1, Item2, Item3, Item4>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSSafeHasChangeValue<Argument3>,
              Item4 : CSSafeHasChangeValue<Argument4> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.unsafeChangeSource(), fourth.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWith5Safe2")
fun <Argument1, Argument2, Argument3, Argument4, Argument5,
        Item1, Item2, Item3, Item4, Item5>
        CSQuintuple<Item1, Item2, Item3, Item4, Item5>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4, Argument5) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSHasChangeValue<Argument3>,
              Item4 : CSSafeHasChangeValue<Argument4>,
              Item5 : CSSafeHasChangeValue<Argument5> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.changeSource(), fourth.unsafeChangeSource(), fifth.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWith6Safe2")
fun <Argument1, Argument2, Argument3, Argument4, Argument5, Argument6,
        Item1, Item2, Item3, Item4, Item5, Item6>
        CSSixtuple<Item1, Item2, Item3, Item4, Item5, Item6>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (
        Argument1, Argument2, Argument3, Argument4, Argument5, Argument6
    ) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSHasChangeValue<Argument3>,
              Item4 : CSHasChangeValue<Argument4>,
              Item5 : CSSafeHasChangeValue<Argument5>,
              Item6 : CSSafeHasChangeValue<Argument6> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.changeSource(), fourth.changeSource(), fifth.unsafeChangeSource(), sixth.unsafeChangeSource(),
        onUnsafeChange)

@JvmName("onUnsafeChangeWith3Safe3")
fun <Argument1, Argument2, Argument3, Item1, Item2, Item3>
        Triple<Item1, Item2, Item3>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3) -> Unit,
): CSRegistration
        where Item1 : CSSafeHasChangeValue<Argument1>,
              Item2 : CSSafeHasChangeValue<Argument2>,
              Item3 : CSSafeHasChangeValue<Argument3> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.unsafeChangeSource(), second.unsafeChangeSource(),
        third.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWith4Safe3")
fun <Argument1, Argument2, Argument3, Argument4,
        Item1, Item2, Item3, Item4>
        CSQuadruple<Item1, Item2, Item3, Item4>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSSafeHasChangeValue<Argument2>,
              Item3 : CSSafeHasChangeValue<Argument3>,
              Item4 : CSSafeHasChangeValue<Argument4> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.unsafeChangeSource(),
        third.unsafeChangeSource(), fourth.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWith5Safe3")
fun <Argument1, Argument2, Argument3, Argument4, Argument5,
        Item1, Item2, Item3, Item4, Item5>
        CSQuintuple<Item1, Item2, Item3, Item4, Item5>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (Argument1, Argument2, Argument3, Argument4, Argument5) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSSafeHasChangeValue<Argument3>,
              Item4 : CSSafeHasChangeValue<Argument4>,
              Item5 : CSSafeHasChangeValue<Argument5> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.unsafeChangeSource(), fourth.unsafeChangeSource(), fifth.unsafeChangeSource(), onUnsafeChange)

@JvmName("onUnsafeChangeWith6Safe3")
fun <Argument1, Argument2, Argument3, Argument4, Argument5, Argument6,
        Item1, Item2, Item3, Item4, Item5, Item6>
        CSSixtuple<Item1, Item2, Item3, Item4, Item5, Item6>.onUnsafeChange(
    delivery: CSSafeChangeDelivery = CSSafeChangeDelivery.Conflated,
    onUnsafeChange: (
        Argument1, Argument2, Argument3, Argument4, Argument5, Argument6
    ) -> Unit,
): CSRegistration
        where Item1 : CSHasChangeValue<Argument1>,
              Item2 : CSHasChangeValue<Argument2>,
              Item3 : CSHasChangeValue<Argument3>,
              Item4 : CSSafeHasChangeValue<Argument4>,
              Item5 : CSSafeHasChangeValue<Argument5>,
              Item6 : CSSafeHasChangeValue<Argument6> =
    onUnsafeChangeSerialized(this.debugClassName, delivery, first.changeSource(), second.changeSource(),
        third.changeSource(), fourth.unsafeChangeSource(), fifth.unsafeChangeSource(),
        sixth.unsafeChangeSource(),
        onUnsafeChange)
