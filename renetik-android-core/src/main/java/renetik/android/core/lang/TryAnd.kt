package renetik.android.core.lang

import renetik.android.core.logging.CSLog.logError
import renetik.android.core.logging.CSLog.logWarn

// TODO:!!! Review thi api and its uses

inline fun <reified E : Throwable> catch(block: () -> Unit): Result<Unit> = try {
    Result.success(block())
} catch (e: Throwable) {
    if (e is E) Result.failure(e) else throw e
}

inline fun catchAll(block: () -> Unit): Result<Unit> = catch<Throwable>(block)

inline fun <reified E : Throwable> catchAllExceptWarn(block: () -> Unit): Result<Unit> = try {
    Result.success(block())
} catch (e: Throwable) {
    if (e is E) throw e
    logWarn(e)
    Result.failure(e)
}

inline fun <reified E : Throwable> catchWarn(block: () -> Unit): Result<Unit> = try {
    Result.success(block())
} catch (e: Throwable) {
    if (e is E) {
        logWarn(e)
        Result.failure(e)
    } else throw e
}

inline fun <ReturnType> catchReturn(
    tryFunction: () -> ReturnType, onExceptionReturn: (Throwable) -> ReturnType,
): ReturnType = try {
    tryFunction()
} catch (e: Throwable) {
    onExceptionReturn(e)
}

inline fun <ReturnType> catchReturnNull(
    tryFunction: () -> ReturnType,
): ReturnType? = catchReturn(tryFunction) { null }

inline fun <reified ExceptionType : Throwable, ReturnType> catchWarnReturn(
    message: String? = null,
    tryFunction: () -> ReturnType, onExceptionReturn: (ExceptionType) -> ReturnType,
): ReturnType {
    return try {
        tryFunction()
    } catch (e: Throwable) {
        if (e is ExceptionType) {
            logWarn(e) { "$message" }
            onExceptionReturn(e)
        } else throw e
    }
}

inline fun <ReturnType, reified ExceptionType : Throwable>
        catchWarnReturn(onExceptionReturn: ReturnType, tryFunction: () -> ReturnType) =
    catchWarnReturn<ExceptionType, ReturnType>(message = null, tryFunction) { onExceptionReturn }

inline fun <ReturnType> catchAllWarnReturn(
    onExceptionReturn: ReturnType, tryFunction: () -> ReturnType,
): ReturnType =
    catchWarnReturn<Exception, ReturnType>(message = null, tryFunction) { onExceptionReturn }

inline fun catchAllWarn(tryFunction: () -> Unit) = catchAllWarnReturn(Unit, tryFunction)

inline fun <ReturnType>
        catchAllWarnReturnNull(message: String? = null, tryFunction: () -> ReturnType)
        : ReturnType? = catchWarnReturn<Exception, ReturnType?>(message, tryFunction) { null }

inline fun <ReturnType, reified ExceptionType : Throwable> catchErrorReturn(
    tryFunction: () -> ReturnType, onExceptionReturn: (ExceptionType) -> ReturnType,
): ReturnType = try {
    tryFunction()
} catch (e: Throwable) {
    if (e is ExceptionType) {
        logError(e)
        onExceptionReturn(e)
    } else throw e
}

inline fun <reified ExceptionType : Throwable, ReturnType>
        catchErrorReturn(onExceptionReturn: ReturnType, tryFunction: () -> ReturnType) =
    catchErrorReturn<ReturnType, ExceptionType>(tryFunction) { onExceptionReturn }

inline fun <reified ExceptionType : Throwable> catchError(tryFunction: () -> Unit) =
    catchErrorReturn<ExceptionType, Unit>(Unit, tryFunction)

inline fun <ReturnType>
        catchAllErrorReturn(onExceptionReturn: ReturnType, tryFunction: () -> ReturnType) =
    catchErrorReturn<ReturnType, Throwable>(tryFunction) { onExceptionReturn }

inline fun catchAllError(tryFunction: () -> Unit) = catchAllErrorReturn(Unit, tryFunction)

inline fun <reified ExceptionType : Throwable, ReturnType>
        catchErrorReturnNull(tryFunction: () -> ReturnType): ReturnType? =
    catchErrorReturn<ExceptionType, ReturnType?>(null, tryFunction)

fun <ReturnType> tryAndFinally(tryFunction: () -> ReturnType, finally: () -> Unit): ReturnType {
    try {
        return tryFunction()
    } finally {
        finally()
    }
}
