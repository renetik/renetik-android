package renetik.android.core.lang.result

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.milliseconds

class CSResultTest {

    @Test
    fun successCallbacksRunAndReturnTheSameResult() = runTest {
        var received: String? = null
        val result = CSResult.success("value")

        val returned = result.ifSuccess { received = it }

        assertSame(result, returned)
        assertEquals("value", received)
    }

    @Test
    fun callbacksAreGatedByState() = runTest {
        var successCalls = 0
        var failureCalls = 0
        var cancelCalls = 0
        var notSuccessCalls = 0

        CSResult.failure<String>("failed")
            .ifSuccess { successCalls++ }
            .ifFailure { failureCalls++ }
            .ifCancel { cancelCalls++ }
            .ifNotSuccess { notSuccessCalls++ }

        CSResult.cancel<String>()
            .ifSuccess { successCalls++ }
            .ifFailure { failureCalls++ }
            .ifCancel { cancelCalls++ }
            .ifNotSuccess { notSuccessCalls++ }

        assertEquals(0, successCalls)
        assertEquals(1, failureCalls)
        assertEquals(1, cancelCalls)
        assertEquals(2, notSuccessCalls)
    }

    @Test
    fun ifSuccessReturnMapsSuccessAndPropagatesFailureMetadata() = runTest {
        val success = CSResult.success("5").ifSuccessReturn { CSResult.success(it.toInt()) }
        val failure = CSResult.failure<String>(code = 404, message = "missing")
            .ifSuccessReturn { CSResult.success(it.length) }

        assertTrue(success.isSuccess)
        assertEquals(5, success.value)
        assertTrue(failure.isFailure)
        assertEquals(404, failure.code)
        assertEquals("missing", failure.message)
        assertNotNull(failure.throwable)
    }

    @Test
    fun failureFactoriesAlwaysProvideAThrowable() {
        assertNotNull(CSResult.failure.throwable)
        assertNotNull(CSResult.failure<String>().throwable)
        assertNotNull(CSResult.failure<String>("failed").throwable)
        assertNotNull(CSResult.failure<String>(404).throwable)
    }

    @Test
    fun thrownExceptionBecomesFailureResult() = runTest {
        val exception = IllegalStateException("boom")

        val result = CSResult.success("value").ifSuccess {
            throw exception
        }

        assertTrue(result.isFailure)
        assertSame(exception, result.throwable)
    }

    @Test
    fun cancellationExceptionBecomesCancelResult() = runTest {
        val result = CSResult.success("value").ifSuccess {
            throw CancellationException("stop")
        }

        assertTrue(result.isCancel)
        assertFalse(result.isSuccess)
    }

    @Test
    fun dispatcherContextIsUsedForCallback() = runTest {
        var coroutineName: String? = null

        CSResult.success("value").ifSuccess(CoroutineName("result-test")) {
            coroutineName = currentCoroutineContext()[CoroutineName]?.name
        }

        assertEquals("result-test", coroutineName)
    }

    @Test
    fun waitForReturnsWhenConditionBecomesTrue() = runBlocking {
        var attempts = 0

        CSCoroutines.waitFor(timeout = 50.milliseconds, delay = 1.milliseconds) {
            attempts++ >= 3
        }

        assertEquals(4, attempts)
    }

    @Test
    fun waitForThrowsTimeoutException() {
        val error = assertThrows(TimeoutException::class.java) {
            runBlocking {
                CSCoroutines.waitFor(
                    timeout = 20.milliseconds,
                    delay = 5.milliseconds,
                    message = "never ready"
                ) { false }
            }
        }

        assertEquals("never ready", error.message)
    }
}
