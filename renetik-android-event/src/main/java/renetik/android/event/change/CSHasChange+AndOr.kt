package renetik.android.event.change

import renetik.android.core.kotlin.primitives.isTrue
import renetik.android.core.lang.value.CSValue
import renetik.android.core.lang.value.ifTrue
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrationImpl

@JvmName("BooleanAndCSHasChangeValueBoolean")
infix fun Boolean.and(other: CSHasChangeValue<Boolean>): CSHasChangeValue<Boolean> =
    other.delegate(fromValue = { it && this })

@JvmName("CSHasChangeValueBooleanAndBoolean")
infix fun CSHasChangeValue<Boolean>.and(other: Boolean): CSHasChangeValue<Boolean> =
    this.delegate(fromValue = { it && other })

@JvmName("CSHasChangeValueBooleanAndCSHasChangeValueBoolean")
infix fun CSHasChangeValue<Boolean>.and(other: CSHasChangeValue<Boolean>) =
    (this to other).delegate(fromValues = { first, second -> first && second })

// Makes ambiguity
//@JvmName("CSHasChangeValueBooleanAndCSHasChangeValueBooleanNullable")
//infix fun CSHasChangeValue<Boolean>.and(other: CSHasChangeValue<Boolean?>) =
//    (this to other).delegate(fromValues = { first, second -> first && second.isTrue })

@JvmName("CSHasChangeAndCSValue")
infix fun CSHasChange<*>.and(other: CSValue<Boolean>): CSHasChange<Unit> {
    val self = this
    return object : CSHasChange<Unit> {
        override fun onChange(function: (Unit) -> Unit): CSRegistration =
            CSRegistration(self.onChange { if (other.value) function(Unit) })
    }
}

@JvmName("CSHasChangeValueAndCSHasChangeValueBoolean")
infix fun <T> CSHasChangeValue<T>.and(other: CSHasChangeValue<Boolean>): CSHasChangeValue<T> {
    val first = this
    return object : CSHasChangeValue<T> {
        override val value: T get() = first.value
        override fun onChange(function: (T) -> Unit) = CSRegistration(
            first.onChange { other.ifTrue { function(it) } },
            other.onTrue { function(value) },
        )
    }
}

@JvmName("CSHasChangeValueIfTrueCSHasChangeValueBoolean")
infix fun <T> CSHasChangeValue<T>.ifTrue(
    other: CSHasChangeValue<Boolean>
): CSHasChangeValue<T?> {
    val first = this
    return object : CSHasChangeValue<T?> {
        fun from(first: T, other: Boolean) =  if (other) first else null
        override val value: T? get() = from(first.value, other.value)
        override fun onChange(function: (T?) -> Unit): CSRegistrationImpl {
            val value = CSValueFunction(this, value, function)
            return CSRegistration(
                first.onChange { value(from(it, other.value)) },
                other.onChange { value(from(first.value, it)) },
            )
        }
    }
}


@JvmName("CSHasChangeValueIfFalseCSHasChangeValueBoolean")
infix fun <T> CSHasChangeValue<T>.ifFalse(
    other: CSHasChangeValue<Boolean>
): CSHasChangeValue<T?> {
    val first = this
    return object : CSHasChangeValue<T?> {
        fun from(first: T, other: Boolean) =  if (!other) first else null
        override val value: T? get() = from(first.value, other.value)
        override fun onChange(function: (T?) -> Unit): CSRegistrationImpl {
            val value = CSValueFunction(this, value, function)
            return CSRegistration(
                first.onChange { value(from(it, other.value)) },
                other.onChange { value(from(first.value, it)) },
            )
        }
    }
}

infix fun CSHasChangeValue<Boolean>.or(other: CSHasChangeValue<Boolean>) =
    (this to other).delegate(fromValues = { first, second -> first || second })

infix fun <T> CSHasChangeValue<T>.or(second: CSHasChange<*>): CSHasChangeValue<T> {
    val first = this
    return object : CSHasChangeValue<T> {
        override val value: T get() = first.value
        override fun onChange(function: (T) -> Unit) = CSRegistration(
            first.onChange { function(it) },
            second.onChange { function(value) },
        )
    }
}

infix fun CSHasChange<*>.or(other: CSHasChange<*>): CSHasChange<Unit> {
    val self = this
    return object : CSHasChange<Unit> {
        override fun onChange(function: (Unit) -> Unit) = CSRegistration(
            self.onChange { function(Unit) },
            other.onChange { function(Unit) },
        )
    }
}
