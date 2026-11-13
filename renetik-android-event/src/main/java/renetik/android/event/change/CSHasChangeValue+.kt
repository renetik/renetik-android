@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalAtomicApi::class)

package renetik.android.event.change

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Main
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.value.CSSafeValue
import renetik.android.core.lang.value.CSValue
import renetik.android.event.change.CSHasChangeValue.Companion.emptyNullable
import renetik.android.event.dispatch.launch
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.destruct
import renetik.android.event.property.CSLateProperty
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistration.Companion.CSRegistration
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.event.registration.plus
import renetik.android.event.registration.register
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KProperty

fun <T> CSHasChangeValue<T>.safeValue(
    parent: CSHasRegistrations
): CSSafeValue<T> = let { property ->
    object : CSSafeValue<T> {
        @Volatile
        override var value: T = property.value
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

        init {
            parent + property.onChange { value = it }
        }
    }
}

@JvmName("destructPreviousNullable")
inline fun <T : CSHasDestruct, P : CSHasChangeValue<out T?>> P.destructPrevious() =
    apply { onChangeFrom { it?.destruct() } }

inline fun <T : CSHasDestruct, P : CSHasChangeValue<out T>> P.destructPrevious() =
    apply { onChangeFrom { it.destruct() } }

inline val <T> CSHasChangeValue<T>?.nullable: CSHasChangeValue<T?>
    get() = this?.delegate(fromValue = { it }) ?: emptyNullable()

inline fun <T> CSHasChangeValue<T?>.isNull(): CSHasChangeValue<Boolean> = isSetTo(null)

inline fun <T> CSHasChangeValue<T?>.isNotNull(): CSHasChangeValue<Boolean> = !isSetTo(null)

inline infix fun <T> CSHasChangeValue<T>.isSetTo(value: T): CSHasChangeValue<Boolean> =
    this@isSetTo.delegate(fromValue = { it == value })

inline infix fun <T> CSHasChangeValue<T>.isEqualTo(other: CSHasChangeValue<T>): CSHasChangeValue<Boolean> =
    (this to other).delegate(fromValues = { first, second -> first == second })

inline infix fun <T> CSHasChangeValue<T>.isTrue(
    crossinline condition: (T) -> Boolean
): CSHasChangeValue<Boolean> = this@isTrue.delegate(fromValue = { condition(it) })

inline infix fun <T> CSHasChangeValue<T>.isFalse(
    crossinline condition: (T) -> Boolean
): CSHasChangeValue<Boolean> = this@isFalse.delegate(fromValue = { !condition(it) })

inline fun <T> CSHasChangeValue<T>.isSetTo(parent: CSHasRegistrations, value: T)
        : CSHasChangeValue<Boolean> = stateDelegate(parent, fromValue = { it == value })

inline infix fun <T> CSHasChangeValue<T>.isNotSetTo(value: T): CSHasChangeValue<Boolean> =
    this@isNotSetTo.delegate(fromValue = { it != value })

inline fun <reified T> CSHasChangeValue<*>.isOfType(): CSHasChangeValue<Boolean> =
    this@isOfType.delegate(fromValue = { it is T })

inline fun <reified T> CSHasChangeValue<*>.asType(): CSHasChangeValue<T?> =
    this@asType.delegate(fromValue = { it as? T })

inline fun <reified T> CSHasChangeValue<*>.onType(
    crossinline function: (T) -> Unit
): CSRegistration = action {
    (it as? T)?.also { type -> function(type) }
}

inline fun <T> CSHasChangeValue<T>.onValue(function: (T) -> Unit) {
    val lateProperty = (this as? CSLateProperty<T>)
    if (lateProperty != null) lateProperty.lateValue?.let(function)
    else function(value)
}

inline fun <T> CSHasChangeValue<T>.action(noinline function: (T) -> Unit): CSRegistration {
    onValue(function)
    return onChange(function)
}

inline fun <T> CSHasChangeValue<T>.actionLaunch(
    dispatcher: CoroutineDispatcher = Main,
    crossinline function: suspend (T) -> Unit): CSRegistration {
    val registrations = CSRegistrationsMap(debugClassName)
    registrations + action { param ->
        registrations.launch(dispatcher) { function(param) }
    }
    return registrations
}

inline fun <T> CSHasChangeValue<T>.actionLaunch(
    key: String, dispatcher: CoroutineDispatcher = Main,
    crossinline function: suspend (T) -> Unit): CSRegistration {
    val registrations = CSRegistrationsMap(debugClassName)
    registrations + action { param ->
        registrations.launch(key, dispatcher) { function(param) }
    }
    return registrations
}

inline fun <T> CSHasChangeValue<T>.onChangeFrom(
    crossinline function: (from: T) -> Unit,
): CSRegistration {
    var value = this.value
    return onChange { function(value); value = it }
}

inline fun <T> CSHasChangeValue<T?>.onChangeNotNull(
    crossinline function: (from: T) -> Unit,
): CSRegistration = onChange { it?.let(function) }

inline fun <T> CSHasChangeValue<T>.onChangeFromTo(
    crossinline function: (from: T, to: T) -> Unit,
): CSRegistration {
    var value = this.value
    return onChange { function(value, it); value = it }
}

inline fun <T> CSHasChangeValue<T>.actionFromTo(
    crossinline function: (from: T?, to: T) -> Unit,
): CSRegistration {
    function(null, value)
    var value = this.value
    return onChange { function(value, it); value = it }
}

inline fun <T> CSHasChangeValue<out T?>.onNull(crossinline function: () -> Unit) =
    onChange { if (it == null) function() }

inline fun <T> CSHasChangeValue<out T?>.onNotNull(crossinline function: () -> Unit) =
    onChange { if (it != null) function() }

inline fun <T> CSHasChangeValue<out T?>.onNotNull(crossinline function: (T) -> Unit) =
    onChange { if (it != null) function(it) }

inline fun <T> CSHasChangeValue<T?>.actionNull(crossinline function: () -> Unit) =
    action { if (it == null) function() }

inline fun <T> CSHasChangeValue<T?>.actionNotNull(crossinline function: () -> Unit) =
    action { if (it != null) function() }

inline fun <T> CSHasChangeValue<T?>.actionNotNull(crossinline function: (T) -> Unit) =
    action { if (it != null) function(it) }

inline fun <Value> CSHasChangeValue<Value>.onChangeTo(
    value: Value, crossinline onChange: () -> Unit
): CSRegistration =
    onChange { if (this.value == value) onChange() }


@JvmName("onChangeChild")
inline fun <ParentValue, ChildValue> CSHasChangeValue<ParentValue>.onChange(
    crossinline child: (ParentValue) -> CSHasChange<ChildValue>,
    noinline onChange: (ChildValue) -> Unit
): CSRegistration {
    var childRegistration: CSRegistration? = null
    val parentRegistration = action {
        childRegistration?.cancel()
        childRegistration = child(value).onChange(onChange)
    }
    return CSRegistration(isActive = true, onCancel = {
        parentRegistration.cancel()
        childRegistration?.cancel()
    })
}

@JvmName("onChangeChildNullable")
inline fun <ParentValue, ChildValue> CSHasChangeValue<ParentValue>.onChange(
    crossinline nullableChild: (ParentValue) -> CSHasChange<ChildValue>?,
    crossinline onChange: (ChildValue) -> Unit
): CSRegistration {
    var childRegistration: CSRegistration? = null
    val parentRegistration = action { parentValue ->
        childRegistration?.cancel()
        childRegistration = nullableChild(parentValue)?.onChange { childValue ->
            onChange(childValue)
        }
    }
    return CSRegistration(isActive = true, onCancel = {
        parentRegistration.cancel()
        childRegistration?.cancel()
    })
}

@JvmName("actionChild")
inline fun <ParentValue, ChildValue> CSHasChangeValue<ParentValue>.action(
    crossinline child: (ParentValue) -> CSHasChangeValue<ChildValue>,
    crossinline action: (ChildValue) -> Unit
): CSRegistration {
    var childRegistration: CSRegistration? = null
    val parentRegistration = action { parentValue ->
        childRegistration?.cancel()
        childRegistration = child(parentValue).action { childValue -> action(childValue) }
    }
    return CSRegistration(isActive = true, onCancel = {
        parentRegistration.cancel()
        childRegistration?.cancel()
    })
}

inline fun <ParentValue, ChildValue> CSHasChangeValue<ParentValue>.action(
    crossinline nullableChild: (ParentValue) -> CSHasChangeValue<ChildValue>?,
    crossinline action: ((ChildValue?) -> Unit)
): CSRegistration {
    var childRegistration: CSRegistration? = null
    val parentRegistration = action { parentValue ->
        childRegistration?.cancel()
        childRegistration = nullableChild(parentValue)?.let {
            it.action { childValue -> action(childValue) }
        } ?: run { action(null); null }
    }
    return CSRegistration(isActive = true, onCancel = {
        parentRegistration.cancel()
        childRegistration?.cancel()
    })
}

fun <V, Instance> CSHasChangeValue<V>.lazyDestructFactory(
    parent: CSHasRegistrations? = null,
    createInstance: (V) -> Instance
): CSValue<Instance> where Instance : CSHasDestruct =
    object : CSValue<Instance> {
        var instance: Instance? = null
        override val value: Instance
            get() {
                if (instance == null) action {
                    instance?.destruct()
                    instance = createInstance(it)
                }.also { parent?.register(it) }
                return instance!!
            }
    }

fun <V, Instance> CSHasRegistrations.lazyDestructFactory(
    property: () -> CSHasChangeValue<V>, create: (V) -> Instance
): CSValue<Instance> where Instance : CSHasDestruct =
    lazyFactory(property) { previousInstance, param ->
        previousInstance?.onDestruct()
        create(param)
    }

@JvmName("CSHasChangeValueLazyDestructFactory")
fun <V, Instance> CSHasChangeValue<V>.lazyDestructFactory(
    parent: CSHasRegistrations, create: (V) -> Instance
): CSValue<Instance> where Instance : CSHasDestruct =
    lazyFactory(parent) { previousInstance, param ->
        previousInstance?.onDestruct()
        create(param)
    }

fun <V, Instance> CSHasRegistrations.lazyFactory(
    property: () -> CSHasChangeValue<V>, create: (Instance?, V) -> Instance
): CSValue<Instance> = object : CSValue<Instance> {
    val self = this@lazyFactory
    var instance: Instance? = null
    override val value: Instance
        get() {
            if (instance == null) self + property().action {
                instance = create(instance, it)
            }
            return instance!!
        }
}

fun <V, Instance> CSHasChangeValue<V>.lazyFactory(
    parent: CSHasRegistrations, create: (Instance?, V) -> Instance
): CSValue<Instance> = object : CSValue<Instance> {
    val self = this@lazyFactory
    var instance: Instance? = null

    init {
        parent + self.onChange {
            instance = create(instance, it)
        }
    }

    override val value: Instance
        get() {
            if (instance == null)
                instance = create(instance, self.value)
            return instance!!
        }
}

val CSHasChangeValue<out Any?>.eventIsNull: CSHasChange<Unit>
    get() = this@eventIsNull.delegate(fromValue = { it == null }).eventIsTrue

val CSHasChangeValue<out Any?>.eventIsNotNull: CSHasChange<Unit>
    get() = this@eventIsNotNull.delegate(fromValue = { it != null }).eventIsTrue

fun <T> CSHasChangeValue<out T?>.eventIsNotNull(): CSHasChange<T> {
    val self = this
    return object : CSHasChange<T> {
        override fun onChange(function: (T) -> Unit): CSRegistration {
            var wasNull: Boolean = value == null
            return self.onChange {
                if (it != null) {
                    if (wasNull) {
                        function(it)
                        wasNull = false
                    }
                } else wasNull = true
            }
        }
    }
}