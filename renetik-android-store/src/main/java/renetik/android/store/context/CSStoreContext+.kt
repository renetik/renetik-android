package renetik.android.store.context

import renetik.android.core.lang.ArgFun
import renetik.android.store.property.CSStoreProperty
import renetik.android.store.type.CSJsonObjectStore

fun <T> CSStoreContext.property(
    key: String, getValues: () -> List<T>,
    defaultIndex: Int = 0, onChange: ArgFun<T>? = null
) = property(key, getValues, { getValues()[defaultIndex] }, onChange)

fun <T> CSStoreContext.property(
    key: String, getValues: () -> List<T>, default: T, onChange: ArgFun<T>? = null
) = property(key, getValues, { default }, onChange)

fun <T> CSStoreContext.property(
    key: String, values: List<T>, defaultIndex: Int, onChange: ArgFun<T>? = null
) = property(key, { values }, values[defaultIndex], onChange)

fun <T> CSStoreContext.property(
    key: String, values: List<T>, default: T, onChange: ArgFun<T>? = null
) = property(key, { values }, { default }, onChange)

fun <T> CSStoreContext.property(
    key: String, values: List<T>, default: () -> T, onChange: ArgFun<T>? = null
) = property(key, { values }, default, onChange)

inline fun <reified T> CSStoreContext.property(
    key: String, default: T, noinline onChange: ArgFun<T>? = null
) where T : Enum<T> = property(key, enumValues<T>().toList(), default, onChange)

inline fun <reified T : CSJsonObjectStore> CSStoreContext.property(
    key: String, noinline onChange: ArgFun<T>? = null
): CSStoreProperty<T> = property(key, T::class, onChange)
