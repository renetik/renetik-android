@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.core.kotlin.collections

import renetik.android.core.lang.value.CSValue

inline fun <T> Iterable<T>.find(predicate: (T) -> Boolean): T? =
    firstOrNull(predicate)

inline fun <T> Iterable<T>.contains(predicate: (T) -> Boolean): Boolean =
    find(predicate) != null

inline fun <T> Iterable<T>.forEachWithPrevious(function: (item: T, previous: T?) -> Unit) {
    var previous: T? = null
    for (item in this) {
        function(item, previous)
        previous = item
    }
}

inline fun <reified T> Iterable<*>.firstOfType(): T? =
    firstOrNull { it is T } as? T

inline operator fun <T> Iterable<T>.contains(value: CSValue<T>): Boolean =
    this.contains(value.value)