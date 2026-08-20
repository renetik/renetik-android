package renetik.android.core.lang.result

import kotlinx.coroutines.CancellationException
import renetik.android.core.kotlin.exception
import renetik.android.core.lang.result.CSResult.State.Cancel
import renetik.android.core.lang.result.CSResult.State.Failure
import renetik.android.core.lang.result.CSResult.State.Success
import kotlin.coroutines.CoroutineContext

data class CSResult<Value>(
    val state: State,
    val value: Value? = null,
    var throwable: Throwable? = null,
    val message: String? = null,
    val code: Int? = null,
) {
    enum class State { Success, Cancel, Failure; }

    val isSuccess get() = state == Success
    val isFailure get() = state == Failure
    val isCancel get() = state == Cancel

    suspend inline fun ifSuccess(
        crossinline function: suspend (Value) -> Unit
    ): CSResult<Value> = ifSuccess(null, function)

    suspend inline fun ifSuccess(
        dispatcher: CoroutineContext?,
        crossinline function: suspend (Value) -> Unit
    ): CSResult<Value> =
        if (isSuccess) runCatching {
            dispatcher { function(value!!); this }
        }.getOrElse {
            if (it is CancellationException) cancel() else failure(it)
        }
        else this

    suspend inline fun <T> ifSuccessReturn(
        crossinline function: suspend (Value) -> CSResult<T>
    ): CSResult<T> = ifSuccessReturn(null, function)

    suspend inline fun <T> ifSuccessReturn(
        dispatcher: CoroutineContext?,
        crossinline function: suspend (Value) -> CSResult<T>
    ): CSResult<T> =
        if (isSuccess) runCatching {
            dispatcher { function(value!!) }
        }.getOrElse {
            if (it is CancellationException) cancel() else failure(it)
        }
        else CSResult(state, throwable = throwable,
            message = message, code = code)

    suspend inline fun ifNotSuccess(
        crossinline function: suspend () -> Unit
    ): CSResult<Value> = ifNotSuccess(null, function)

    suspend inline fun ifNotSuccess(
        dispatcher: CoroutineContext?,
        crossinline function: suspend () -> Unit
    ): CSResult<Value> = apply {
        if (isFailure || isCancel) dispatcher { function() }
    }

    suspend inline fun ifFailure(
        crossinline function: suspend (CSResult<Value>) -> Unit
    ): CSResult<Value> = ifFailure(null, function)

    suspend inline fun ifFailure(
        dispatcher: CoroutineContext?,
        crossinline function: suspend (CSResult<Value>) -> Unit
    ): CSResult<Value> = apply {
        if (isFailure) dispatcher { function(this) }
    }

    suspend inline fun ifCancel(
        crossinline function: suspend () -> Unit
    ) = ifCancel(null, function)

    suspend inline fun ifCancel(
        dispatcher: CoroutineContext?,
        crossinline function: suspend () -> Unit
    ) = apply {
        if (isCancel) dispatcher { function() }
    }

    companion object {
        val success: CSResult<Unit> = CSResult(Success, Unit)
        val failure: CSResult<Unit> = CSResult(Failure, Unit)
        val cancel: CSResult<Unit> = CSResult(Cancel, Unit)

        fun <Value> success(value: Value) = CSResult(Success, value)
        fun <Value> cancel() = CSResult<Value>(Cancel)

        fun <Value> failure(
            throwable: Throwable? = null,
            message: String? = null, code: Int? = null
        ) = CSResult<Value>(
            Failure, throwable = throwable,
            message = message, code = code
        )

        fun <Value> failure(message: String): CSResult<Value> =
            CSResult(Failure, throwable = exception(message), message = message)

        fun <Value> failure(code: Int, message: String? = null): CSResult<Value> =
            CSResult(Failure, throwable = exception(message), code = code, message = message)
    }
}