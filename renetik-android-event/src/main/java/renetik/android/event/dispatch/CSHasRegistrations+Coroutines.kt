package renetik.android.event.dispatch

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import renetik.android.core.base.CSApplication.Companion.app
import renetik.android.core.kotlin.className
import renetik.android.core.lang.variable.CSWeakVariable.Companion.weak
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.plus
import kotlin.coroutines.CoroutineContext

private class JobRegistrationWrapper(
    private val registration: CSRegistration
) : JobRegistration {
    override var job: Job? by weak(null)
    override val isActive: Boolean get() = registration.isActive
    override val isCanceled: Boolean get() = registration.isCanceled
    override val eventCancel = registration.eventCancel
    override fun resume() = registration.resume()
    override fun pause() = registration.pause()
    override fun cancel() = registration.cancel()
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CSHasRegistrations.launch(
    scope: CoroutineScope,
    context: CoroutineContext = Main,
    func: suspend (JobRegistration) -> Unit,
): JobRegistration {
    val newRegistration = CompletableDeferred<JobRegistrationWrapper>()
    val jobRegistration = context.launch(scope, className) { registration ->
        newRegistration.await().also {
            it.job = registration.job!!
            if (!it.isCanceled) {
                func(it)
                it.cancel()
            }
        }
    }
    newRegistration.complete(JobRegistrationWrapper(this + jobRegistration))
    return newRegistration.getCompleted()
}

fun CSHasRegistrations.launch(
    dispatcher: CoroutineContext = Main,
    func: suspend (JobRegistration) -> Unit,
): JobRegistration = launch(app.mainScope, dispatcher, func)

@OptIn(ExperimentalCoroutinesApi::class)
fun CSHasRegistrations.launch(
    key: String, dispatcher: CoroutineContext = Main,
    func: suspend (JobRegistration) -> Unit,
): JobRegistration {
    val newRegistration = CompletableDeferred<JobRegistrationWrapper>()
    val previous = registrations.map[key] as? JobRegistration
    registrations.cancel(key)
    val jobRegistration = dispatcher.launch(className) { registration ->
        previous?.waitToFinish()
        newRegistration.await().also {
            it.job = registration.job!!
            if (!it.isCanceled) {
                func(it)
                it.cancel()
            }
        }
    }
    newRegistration.complete(JobRegistrationWrapper(this + (key to jobRegistration)))
    return newRegistration.getCompleted()
}

fun CSHasRegistrations.launchIfNot(
    key: String,
    dispatcher: CoroutineContext = Main,
    func: suspend (CSRegistration) -> Unit,
): CSRegistration? {
    if (registrations.isActive(key)) return null
    return launch(key, dispatcher, func)
}