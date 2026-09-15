package renetik.android.core.lang.result

import renetik.android.core.lang.result.CSResult.Companion.failure

fun <Value> Result<CSResult<Value>>.getOrFailure(
    failureMessage: String? = null,
): CSResult<Value> = getOrElse { failure(it, failureMessage ?: it.message) }

fun <Value> Result<CSResult<Value>>.getOrFailure(
    exception: (Throwable) -> Throwable
): CSResult<Value> = getOrElse { failure(exception(it)) }
