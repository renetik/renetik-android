@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.core.lang.result

import renetik.android.core.kotlin.throwCancellation
import renetik.android.core.lang.result.CSResult.Companion.failure
import renetik.android.core.lang.result.CSResult.Companion.success

inline fun <Value> Result<CSResult<Value>>.getOrFailure(
    failureMessage: String? = null,
): CSResult<Value> = throwCancellation()
    .getOrElse { failure(it, failureMessage ?: it.message) }

inline fun <Value> Result<CSResult<Value>>.getOrFailure(
    exception: (Throwable) -> Throwable
): CSResult<Value> = throwCancellation().getOrElse { failure(exception(it)) }

inline fun <R, T : R> Result<T>.toResult(
    failureMessage: String? = null): CSResult<T> = fold(::success,
    onFailure = { ex -> failureMessage?.let { failure(ex, it) } ?: failure(ex) })

inline fun <R, T : R> Result<T>.toResult(
    onFailure: (Throwable) -> String): CSResult<T> = fold(::success,
    onFailure = { ex -> failure(ex, onFailure(ex)) })
