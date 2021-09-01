package renetik.android.framework.event.property

import renetik.android.framework.event.CSEvent.CSEventRegistration
import renetik.android.framework.lang.CSProperty

interface CSEventProperty<T> : CSProperty<T> {
    fun onBeforeChange(value: (T) -> Unit): CSEventRegistration
    fun onChange(value: (T) -> Unit): CSEventRegistration
    fun value(newValue: T, fire: Boolean = true): CSEventProperty<T>
    fun apply(): CSEventProperty<T>
}