package renetik.android.core.lang.result

import renetik.android.core.logging.CSLog.logError
import renetik.android.core.logging.CSLog.logInfo
import renetik.android.core.logging.CSLog.logWarn

suspend fun <T> CSResult<T>.logError() = ifFailure { logError(it.throwable, it.message) }
suspend fun <T> CSResult<T>.logWarn() = ifFailure { logWarn(it.throwable, it.message) }
suspend fun <T> CSResult<T>.logInfo() = ifFailure { logInfo(it.throwable, it.message) }
