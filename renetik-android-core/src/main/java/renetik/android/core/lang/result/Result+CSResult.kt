package renetik.android.core.lang.result

import renetik.android.core.kotlin.throwCancellation
import renetik.android.core.lang.result.CSResult.Companion.failure

fun <Value> Result<CSResult<Value>>.getOrFailure(
    failureMessage: String? = null,
): CSResult<Value> = throwCancellation()
    .getOrElse { failure(it, failureMessage ?: it.message) }

fun <Value> Result<CSResult<Value>>.getOrFailure(
    exception: (Throwable) -> Throwable
): CSResult<Value> = throwCancellation().getOrElse { failure(exception(it)) }
