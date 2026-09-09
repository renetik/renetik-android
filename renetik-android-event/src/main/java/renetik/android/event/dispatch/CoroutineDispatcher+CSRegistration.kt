@file:OptIn(ExperimentalCoroutinesApi::class)

package renetik.android.event.dispatch

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import renetik.android.core.base.CSApplication.Companion.app
import renetik.android.core.kotlin.className
import renetik.android.core.lang.variable.CSWeakVariable.Companion.weak
import renetik.android.core.logging.CSLog.logError
import renetik.android.event.registration.CSRegistrationImpl
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

private class JobRegistrationImpl(
    isActive: Boolean = false,
) : CSRegistrationImpl(isActive), JobRegistration {
    override var job: Job? by weak(null)
    override fun onCancel() {
        super.onCancel()
        job?.takeIf { !it.isCompleted }?.cancel(
            CancellationException("$className cancel")
        )
    }
}

fun CoroutineContext.launch(
    scope: CoroutineScope, name: String? = null,
    func: suspend (JobRegistration) -> Unit,
): JobRegistration {
    val registration = JobRegistrationImpl(isActive = true)
    val context = name?.let(::CoroutineName)?.let { this + it } ?: this
    scope.launch(context) {
        try {
            if (isActive && !registration.isCanceled) {
                registration.job = coroutineContext.job
                func(registration)
            }
        } catch (ex: CancellationException) {
            registration.cancel()
            throw ex
        } catch (ex: Exception) {
            logError(ex)
        }
    }
    return registration
}

fun CoroutineContext.launch(
    name: String? = null, func: suspend (JobRegistration) -> Unit,
): JobRegistration = launch(app.mainScope, name, func)

fun CoroutineContext.launch(
    name: String? = null, after: Duration,
    func: suspend (JobRegistration) -> Unit,
): JobRegistration = launch(name) { delay(after); func(it) }

fun CoroutineScope.start(
    func: suspend (JobRegistration) -> Unit,
): JobRegistration {
    val registration = JobRegistrationImpl(isActive = true)
    val job = CompletableDeferred<Job>()
    job.complete(launch {
        job.await()
        registration.job = job.getCompleted()
        if (isActive && registration.isActive) func(registration)
    })
    return registration
}