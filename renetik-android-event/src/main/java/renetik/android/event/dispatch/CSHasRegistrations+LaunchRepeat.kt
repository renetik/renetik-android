package renetik.android.event.dispatch

import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import renetik.android.core.kotlin.debugClassName
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistration.Companion.CSRegistration
import renetik.android.event.registration.plus
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

fun CSHasRegistrations.launchRepeat(
    dispatcher: CoroutineContext = Main,
    delay: Duration = ZERO,
    after: Duration = delay,
    start: Boolean = true,
    function: suspend () -> Unit,
): CSRegistration {
    var job: JobRegistration? = null
    lateinit var registration: CSRegistration
    fun launch() {
        job?.cancel()
        job = dispatcher.launch(debugClassName) { jobRegistration ->
            if (after > ZERO) delay(after)
            while (!registration.isCanceled && registration.isActive &&
                jobRegistration.isActive) {
                function()
                if (delay > ZERO) delay(delay) else yield()
            }
        }
    }
    registration = CSRegistration(
        isActive = false,
        onResume = { launch() },
        onPause = { job?.cancel() },
        onCancel = { job?.cancel() }
    )
    val registered = this + registration
    if (start) registered.resume()
    return registered
}

fun CSHasRegistrations.launchRepeat(
    delay: Int,
    start: Boolean = true,
    dispatcher: CoroutineContext = Main,
    function: suspend () -> Unit,
): CSRegistration = launchRepeat(
    dispatcher = dispatcher,
    delay = delay.milliseconds,
    after = delay.milliseconds,
    start = start,
    function = function
)

fun CSHasRegistrations.launchRepeat(
    dispatcher: CoroutineContext,
    delay: Int,
    after: Int = delay,
    start: Boolean = true,
    function: suspend () -> Unit,
): CSRegistration = launchRepeat(
    dispatcher = dispatcher,
    delay = delay.milliseconds,
    after = after.milliseconds,
    start = start,
    function = function
)

fun CSHasRegistrations.launchRepeat(
    after: Int,
    delay: Int,
    start: Boolean = true,
    dispatcher: CoroutineContext = Main,
    function: suspend () -> Unit,
): CSRegistration = launchRepeat(
    dispatcher = dispatcher,
    delay = delay.milliseconds,
    after = after.milliseconds,
    start = start,
    function = function
)
