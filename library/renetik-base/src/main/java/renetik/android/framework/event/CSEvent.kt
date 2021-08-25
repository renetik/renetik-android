package renetik.android.framework.event

import android.content.Context
import android.view.View
import renetik.android.framework.event.CSEvent.CSEventRegistration
import java.io.Closeable

@JvmName("eventWithType")
fun <T> event(): CSEvent<T> = CSEventImpl()

fun event(): CSEvent<Unit> = CSEventImpl()

fun CSEvent<Unit>.fire() = apply { fire(Unit) }

inline fun CSEvent<Unit>.listen(crossinline function: () -> Unit) =
    this.add { _, _ -> function() }

inline fun <T> CSEvent<T>.listen(crossinline function: (argument: T) -> Unit) =
    this.add { _, argument -> function(argument) }

inline fun <T> CSEvent<T>.listenOnce(crossinline listener: (argument: T) -> Unit) =
    add { registration, argument ->
        registration.cancel()
        listener(argument)
    }

interface CSEvent<T> {

    val isListened: Boolean

    fun add(listener: (registration: CSEventRegistration, argument: T) -> Unit): CSEventRegistration

    fun add(listener: CSEventListener<T>): CSEventRegistration

    fun cancel(listener: CSEventListener<T>)

    fun fire(argument: T)

    fun clear()

    interface CSEventRegistration {

        var isActive: Boolean

        fun cancel()
    }

    @Deprecated("Just for debugging")
    val registrations: List<CSEventRegistration>
}

interface CSEventListener<T> : CSEventRegistration {
    fun onEvent(argument: T)
}

fun CSEventRegistration.pause(): Closeable {
    isActive = false
    return Closeable { resume() }
}

fun CSEventRegistration.resume() = apply {
    isActive = true
}

interface CSEventOwner {
    fun register(registration: CSEventRegistration): CSEventRegistration
}

fun CSEventOwner.register(registration: CSEventRegistration?) =
    registration?.let { register(it) }

interface CSVisibleEventOwner {
    fun whileVisible(registration: CSEventRegistration): CSEventRegistration
}

fun CSVisibleEventOwner.whileShowing(registration: CSEventRegistration?) =
    registration?.let { whileVisible(it) }


interface CSHasParent {
    fun onAddedToParent()
    fun onRemovedFromParent()
}

interface CSViewInterface : CSContextInterface {
    val view: View
}

interface CSVisibility {
    fun updateVisibility()
    val isVisible: Boolean
    val onViewVisibilityChanged: CSEvent<Boolean>
}

fun CSVisibility.whileShowingTrue(function: (Boolean) -> Unit) {
    if (isVisible) function(true)
    onViewVisibilityChanged.listen { visible ->
        if (visible) function(true) else function(false)
    }
}

interface CSContextInterface : CSHasDestroy, CSEventOwner {
    val context: Context
}

interface CSHasDestroy {
    val onDestroy: CSEvent<Unit>
}