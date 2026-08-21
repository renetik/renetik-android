package renetik.android.core.lang.result

import renetik.android.core.lang.result.CSResult.Companion.failure

fun <Value> Result<CSResult<Value>>.getOrFailResult(
    failureMessage: String? = null,
): CSResult<Value> = getOrElse { failure(it, failureMessage) }
