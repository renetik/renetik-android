@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.store

import renetik.android.core.logging.CSLog.logError
import renetik.android.json.parseJsonMap

inline fun <T : CSStore> T.load(store: CSStore, key: String) = apply {
    store.data[key]?.also {
        @Suppress("UNCHECKED_CAST")
        (it as? Map<String, Any?>)?.also(::load)
            ?: logError("Key is not store Map: $key")
    }
}

inline fun <T : CSStore> T.load(store: CSStore) = apply { load(store.data) }
inline fun <T : CSStore> T.reload(store: CSStore) = apply { reload(store.data) }
inline fun <T : CSStore> T.reload(json: String) = apply { reload(json.parseJsonMap()!!) }
inline val <T : CSStore> T.isEmpty get() = data.isEmpty()
inline val <T : CSStore> T.isNotEmpty get() = data.isNotEmpty()

inline fun <T : CSStore, R> T.operation(func: (T) -> R) {
    if (!startOperation()) {
        func(this); return
    }
    try {
        func(this)
    } finally {
        stopOperation()
    }
}