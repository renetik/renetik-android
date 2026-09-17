package renetik.android.core.lang.result

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import renetik.android.core.kotlin.finally
import renetik.android.core.kotlin.throwCancellation

class ResultCancellationTest {

    @Test
    fun finallyRunsAndPreservesSuccessfulResult() {
        var finallyCalled = false

        val result = Result.success("value").finally { finallyCalled = true }

        assertTrue(finallyCalled)
        assertEquals("value", result.getOrThrow())
    }

    @Test
    fun finallyRunsAndPreservesFailedResult() {
        val exception = IllegalStateException("boom")
        var finallyCalled = false

        val result = Result.failure<String>(exception).finally { finallyCalled = true }

        assertTrue(finallyCalled)
        assertSame(exception, result.exceptionOrNull())
    }

    @Test
    fun finallyRunsBeforeCancellationIsRethrown() {
        val cancellation = CancellationException("stop")
        var finallyCalled = false

        val thrown = assertThrows(CancellationException::class.java) {
            Result.failure<Unit>(cancellation)
                .finally { finallyCalled = true }
                .throwCancellation()
        }

        assertTrue(finallyCalled)
        assertSame(cancellation, thrown)
    }

    @Test
    fun successfulResultReturnsContainedCSResult() {
        val contained = CSResult.success("value")

        val result = Result.success(contained).getOrFailure()

        assertSame(contained, result)
    }

    @Test
    fun exceptionBecomesFailureWithContext() {
        val exception = IllegalStateException("boom")

        val result = Result.failure<CSResult<String>>(exception)
            .getOrFailure("operation failed")

        assertTrue(result.isFailure)
        assertSame(exception, result.throwable)
        assertEquals("operation failed", result.message)
    }

    @Test
    fun exceptionMessageIsUsedWhenContextIsNotProvided() {
        val exception = IllegalStateException("boom")

        val result = Result.failure<CSResult<String>>(exception).getOrFailure()

        assertTrue(result.isFailure)
        assertSame(exception, result.throwable)
        assertEquals("boom", result.message)
    }

    @Test
    fun cancellationExceptionPropagates() {
        val cancellation = CancellationException("stop")

        val thrown = assertThrows(CancellationException::class.java) {
            Result.failure<CSResult<String>>(cancellation).getOrFailure()
        }

        assertSame(cancellation, thrown)
    }

    @Test
    fun cancellationExceptionPropagatesBeforeExceptionMapping() {
        val cancellation = CancellationException("stop")
        var mapperCalled = false

        val thrown = assertThrows(CancellationException::class.java) {
            Result.failure<CSResult<String>>(cancellation).getOrFailure {
                mapperCalled = true
                IllegalStateException(it)
            }
        }

        assertSame(cancellation, thrown)
        assertFalse(mapperCalled)
    }

    @Test
    fun explicitCancelResultIsPreserved() {
        val contained = CSResult.cancel<String>()

        val result = Result.success(contained).getOrFailure()

        assertSame(contained, result)
        assertTrue(result.isCancel)
    }
}
