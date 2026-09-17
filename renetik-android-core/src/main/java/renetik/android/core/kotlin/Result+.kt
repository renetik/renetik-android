@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.core.kotlin

import kotlinx.coroutines.CancellationException
import renetik.android.core.logging.CSLog.logError
import renetik.android.core.logging.CSLog.logWarn

inline fun <reified T : Throwable> Result<*>.onFailureOf(onFailure: (T) -> Unit) = apply {
    exceptionOrNull()?.also { if (it is T) onFailure(it) }
}

inline fun <T> Result<T>.throwCancellation() = apply {
    onFailureOf<CancellationException> { throw it }
}

inline fun <T> Result<T>.finally(action: () -> Unit) = apply {
    action()
}

inline fun <T> Result<T>.logError() = apply { onFailure { logError(it) } }
inline fun <T> Result<T>.logWarn() = apply { onFailure { logWarn(it) } }

inline fun <R, T : R> Result<T>.getOrDefault(function: () -> R): R =
    getOrNull() ?: function()
