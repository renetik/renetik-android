package renetik.android.core.lang.result

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import renetik.android.core.kotlin.throwCancellation

class ResultCancellationTest {

    @Test
    fun successfulResultReturnsContainedCSResult() {
        val contained = CSResult.success("value")

        val result = Result.success(contained).throwCancellation().getOrFailResult()

        assertSame(contained, result)
    }

    @Test
    fun exceptionBecomesFailureWithContext() {
        val exception = IllegalStateException("boom")

        val result = Result.failure<CSResult<String>>(exception)
            .throwCancellation()
            .getOrFailResult("operation failed")

        assertTrue(result.isFailure)
        assertSame(exception, result.throwable)
        assertEquals("operation failed", result.message)
    }

    @Test
    fun cancellationExceptionPropagates() {
        val cancellation = CancellationException("stop")

        val thrown = assertThrows(CancellationException::class.java) {
            Result.failure<CSResult<String>>(cancellation).throwCancellation().getOrFailResult()
        }

        assertSame(cancellation, thrown)
    }

    @Test
    fun explicitCancelResultIsPreserved() {
        val contained = CSResult.cancel<String>()

        val result = Result.success(contained).throwCancellation().getOrFailResult()

        assertSame(contained, result)
        assertTrue(result.isCancel)
    }
}
