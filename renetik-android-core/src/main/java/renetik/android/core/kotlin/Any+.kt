package renetik.android.core.kotlin

import renetik.android.core.lang.CSHasId

inline fun <T : Any> T.then(function: (T) -> Unit): Unit = function(this)

inline fun <T : Any> T.changeIf(
    condition: Boolean, function: (T).() -> T
) = if (condition) function(this) else this

inline fun <T : Any> T.letIf(
    condition: Boolean, function: (T) -> T
) = if (condition) function(this) else this

inline fun <T : Any, P : Any> T.changeIfNotNull(
    parameter: P?, function: (T).(P) -> T
) = if (parameter != null) function(this, parameter) else this

inline fun <T : Any> T.changeIf(
    condition: (T) -> Boolean, change: (T) -> T
): T = if (condition(this)) change(this) else this

inline fun <T : Any> T.changeIfNullable(
    condition: (T) -> Boolean, change: (T) -> T?
): T? = if (condition(this)) change(this) else this

inline fun <T : Any> T.alsoIf(condition: Boolean, function: (T) -> Unit) =
    also { if (condition) function(it) }

inline fun <T : Any> T.applyIf(condition: Boolean, function: (T).() -> Unit) =
    apply { if (condition) function(this) }

inline val <T : Any> T.debugClassName: String get() = this::class.simpleName ?: "undefined-class-name"

fun Any?.toId() = (this as? CSHasId)?.id ?: toString()

