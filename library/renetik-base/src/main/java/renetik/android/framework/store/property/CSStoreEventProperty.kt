package renetik.android.framework.store.property

import renetik.android.framework.event.property.CSEventProperty
import renetik.android.framework.store.CSStoreInterface

interface CSStoreEventProperty<T> : CSEventProperty<T> {
    val store: CSStoreInterface
    val key: String
    fun set(store: CSStoreInterface, value: T) /// This set should go to base class
    fun saveTo(store: CSStoreInterface) = set(store, value)
    fun save() = set(store, value)
}