@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.event.change

import renetik.android.core.lang.ArgFun
import renetik.android.core.lang.tuples.CSSixtuple
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.destruct
import renetik.android.event.registration.CSHasRegistrations

fun <Argument, Return : CSHasDestruct>
        CSHasChangeValue<Argument>.stateDelegateDestruct(
    parent: CSHasRegistrations? = null,
    fromValue: (Argument) -> Return,
    onChange: ArgFun<Return>? = null
): CSHasChangeValue<Return> = stateDelegate(
    parent, fromValueAndPrevious = { type, previous ->
        previous?.destruct(); fromValue(type)
    }, onChange)

fun <Argument> CSHasChangeValue<Argument>.stateDelegate(
    parent: CSHasRegistrations? = null, onChange: ArgFun<Argument>? = null,
): CSHasChangeValue<Argument> = stateDelegate(parent, fromValue = { it }, onChange)

fun <T : Any> CSHasChangeValue<T?>.stateDelegateNonNull(
    parent: CSHasRegistrations? = null,
): CSHasChangeValue<T> {
    var previous = checkNotNull(value)
    return stateDelegate(parent, fromValue = { current ->
        if (current != null) previous = current
        previous
    })
}

fun <Argument1, Argument2, Argument3, Argument4, Argument5, Argument6>
        CSSixtuple<CSHasChangeValue<Argument1>, CSHasChangeValue<Argument2>,
                CSHasChangeValue<Argument3>, CSHasChangeValue<Argument4>,
                CSHasChangeValue<Argument5>, CSHasChangeValue<Argument6>>.stateDelegate(
    parent: CSHasRegistrations? = null,
    onChange: ArgFun<CSSixtuple<Argument1, Argument2, Argument3, Argument4, Argument5, Argument6>>? = null
): CSHasChangeValue<CSSixtuple<Argument1, Argument2, Argument3, Argument4, Argument5, Argument6>> =
    stateDelegate(parent, fromValues = { item1, item2, item3, item4, item5, item6 ->
        CSSixtuple(item1, item2, item3, item4, item5, item6)
    }, onChange)
