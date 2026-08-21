package renetik.android.event.dispatch

import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withTimeoutOrNull
import renetik.android.core.kotlin.throwCancellation
import renetik.android.core.logging.CSLog.logError
import renetik.android.event.registration.CSHasRegistrations
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.TimeSource

/**
 * A thread-safe, coroutine-based Throttler.
 *
 * It limits the execution frequency of the [action] to the duration specified in [after].
 * If multiple events occur during the [after] window, only the *latest* one is processed
 * (Conflation strategy).
 *
 * @param T The type of data passed to the action.
 * @param parent A scope wrapper (CSHasRegistrations) that controls the lifecycle of this throttler.
 * @param dispatcher The CoroutineContext where the action will run (default: Main).
 * @param action The actual work to perform (suspend function).
 * @param after The "cooldown" or "grouping" window duration.
 */
class CSThrottler<T>(
    parent: CSHasRegistrations,
    dispatcher: CoroutineContext = Main,
    private val action: suspend (T) -> Unit,
    private val after: Duration = ZERO,
) : (T) -> Unit {
    companion object {
        fun CSHasRegistrations.throttler(
            after: Duration, function: suspend () -> Unit
        ) = CSThrottler<Unit>(this, Main, { function() }, after)

        fun CSHasRegistrations.throttler(
            dispatcher: CoroutineContext = Main,
            after: Duration, function: suspend () -> Unit
        ) = CSThrottler<Unit>(this, dispatcher, { function() }, after)

        fun CSHasRegistrations.throttler(
            dispatcher: CoroutineContext = Main, function: suspend () -> Unit
        ) = CSThrottler<Unit>(this, dispatcher, { function() })

        fun CSHasRegistrations.throttler(
            function: suspend () -> Unit
        ) = CSThrottler<Unit>(this, Main, { function() })

        fun <T> CSHasRegistrations.throttler(
            after: Duration, function: suspend (T) -> Unit
        ) = CSThrottler(this, Main, function, after)

        fun <T> CSHasRegistrations.throttler(
            dispatcher: CoroutineContext = Main,
            after: Duration, function: suspend (T) -> Unit
        ) = CSThrottler(this, dispatcher, function, after)

        fun <T> CSHasRegistrations.throttler(
            dispatcher: CoroutineContext = Main, function: suspend (T) -> Unit
        ) = CSThrottler(this, dispatcher, function)

        fun <T> CSHasRegistrations.throttler(
            function: suspend (T) -> Unit
        ) = CSThrottler(this, Main, function)

        operator fun CSThrottler<Unit>.invoke() = invoke(Unit)
    }

    private val channel = Channel<T>(Channel.CONFLATED)

    init {
        parent.launch(dispatcher) {
            val clock = TimeSource.Monotonic
            while (it.isActive) {
                var param = try {
                    channel.receive()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: ClosedReceiveChannelException) {
                    break
                }
                if (after == ZERO) {
                    runCatching { action(param) }
                        .throwCancellation().onFailure(::logError)
                    continue
                }
                val mark = clock.markNow()
                while (it.isActive) {
                    val remaining = after - mark.elapsedNow()
                    if (remaining <= ZERO) break
                    val timeoutMs = remaining.inWholeMilliseconds.coerceAtLeast(1L)
                    param = withTimeoutOrNull(timeoutMs) { channel.receive() } ?: break
                }
                runCatching { action(param) }
                    .throwCancellation().onFailure(::logError)
            }
        }
    }

    override fun invoke(param: T) {
        channel.trySend(param)
    }
}
