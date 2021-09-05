package renetik.android.framework.event.property

import renetik.android.framework.lang.CSId
import renetik.android.framework.store.property.CSListStoreEventProperty
import renetik.android.framework.store.property.nullable.CSItemNullableStoreEventProperty
import renetik.android.framework.store.property.value.CSItemStoreEventProperty

interface CSPropertyStoreInterface {
    fun property(
        key: String, value: String,
        onChange: ((value: String) -> Unit)? = null
    ): CSEventProperty<String>

    fun property(key: String, value: Boolean,
                 onChange: ((value: Boolean) -> Unit)? = null)
            : CSEventProperty<Boolean>

    fun nullableProperty(key: String, default: Boolean?,
                         onChange: ((value: Boolean?) -> Unit)? = null)
            : CSEventProperty<Boolean?>

    fun lateBooleanProperty(key: String, onChange: ((value: Boolean) -> Unit)? = null)
            : CSEventProperty<Boolean>

    fun property(
        key: String, value: Int,
        onChange: ((value: Int) -> Unit)? = null
    ): CSEventProperty<Int>

    fun nullableProperty(
        key: String, default: Int? = null,
        onChange: ((value: Int?) -> Unit)? = null
    ): CSEventProperty<Int?>

    fun property(
        key: String, value: Double,
        onChange: ((value: Double) -> Unit)? = null
    ): CSEventProperty<Double>

    fun property(
        key: String, value: Float,
        onChange: ((value: Float) -> Unit)? = null
    ): CSEventProperty<Float>

    fun <T> property(
        key: String, values: List<T>, value: T,
        onChange: ((value: T) -> Unit)? = null
    ): CSItemStoreEventProperty<T>

    fun <T> nullableProperty(
        key: String, values: List<T>, default: T?,
        onChange: ((value: T?) -> Unit)? = null
    ): CSItemNullableStoreEventProperty<T>

    fun <T> property(
        key: String, values: List<T>, defaultIndex: Int,
        onChange: ((value: T) -> Unit)? = null
    ) = property(key, values, values[defaultIndex], onChange)

    fun <T> property(
        key: String, values: Array<T>, value: T,
        onChange: ((value: T) -> Unit)? = null
    ) = property(key, values.asList(), value, onChange)

    fun <T> property(
        key: String, values: Array<T>, valueIndex: Int,
        onChange: ((value: T) -> Unit)? = null
    ) = property(key, values.asList(), values[valueIndex], onChange)

    fun <T : CSId> property(
        key: String, values: Iterable<T>, value: List<T>,
        onChange: ((value: List<T>) -> Unit)? = null
    ): CSListStoreEventProperty<T>

    fun <T : CSId> property(
        key: String, values: Array<T>, value: List<T>,
        onChange: ((value: List<T>) -> Unit)? = null
    ) = property(key, values.asList(), value, onChange)
}