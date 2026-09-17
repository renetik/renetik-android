package renetik.android.store

import renetik.android.core.lang.ArgFun
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.parent
import renetik.android.store.CSStore
import renetik.android.store.property.CSStoreProperty
import renetik.android.store.property.listenLoad
import renetik.android.store.property.listenLoadOnce
import renetik.android.store.property.nullable.CSBooleanNullableStoreProperty
import renetik.android.store.property.nullable.CSDoubleNullableStoreProperty
import renetik.android.store.property.nullable.CSFloatNullableStoreProperty
import renetik.android.store.property.nullable.CSIntNullableStoreProperty
import renetik.android.store.property.nullable.CSLongNullableStoreProperty
import renetik.android.store.property.nullable.CSStringNullableStoreProperty

fun CSStore.nullStringProperty(
    key: String, default: String? = null,
    onChange: ArgFun<String?>? = null,
) = CSStringNullableStoreProperty(this, key, default, onChange)

fun CSStore.dataNullStringProperty(
    key: String, default: String? = null,
    onChange: ArgFun<String?>? = null,
) = nullStringProperty(key, default, onChange).listenLoadOnce()

fun CSStore.nullStringProperty(
    parent: CSHasDestruct,
    key: String, default: String? = null,
    onChange: ArgFun<String?>? = null,
) = CSStringNullableStoreProperty(this, key, default, onChange)
    .parent(parent).listenLoad()

fun CSStore.nullBoolProperty(
    key: String, default: Boolean? = null,
    onChange: ArgFun<Boolean?>? = null,
) = CSBooleanNullableStoreProperty(this, key, default, onChange)

fun CSStore.dataNullBoolProperty(
    key: String, default: Boolean? = null,
    onChange: ArgFun<Boolean?>? = null,
): CSStoreProperty<Boolean?> =
    nullBoolProperty(key, default, onChange).listenLoadOnce()

fun CSStore.nullIntProperty(
    key: String, default: Int? = null,
    onChange: ArgFun<Int?>? = null,
) = CSIntNullableStoreProperty(this, key, default, onChange)

fun CSStore.nullDoubleProperty(
    key: String, default: Double? = null,
    onChange: ArgFun<Double?>? = null,
) = CSDoubleNullableStoreProperty(this, key, default, onChange)

fun CSStore.dataNullIntProperty(
    key: String, default: Int? = null,
    onChange: ArgFun<Int?>? = null,
): CSStoreProperty<Int?> =
    nullIntProperty(key, default, onChange).listenLoadOnce()

fun CSStore.nullIntProperty(
    parent: CSHasDestruct,
    key: String, default: Int? = null,
    onChange: ArgFun<Int?>? = null,
): CSStoreProperty<Int?> =
    CSIntNullableStoreProperty(this, key, default, onChange)
        .parent(parent).listenLoad()

fun CSStore.nullFloatProperty(
    key: String, default: Float? = null,
    onChange: ArgFun<Float?>? = null,
): CSStoreProperty<Float?> =
    CSFloatNullableStoreProperty(this, key, default, onChange)

fun CSStore.nullFloatProperty(
    parent: CSHasDestruct,
    key: String, default: Float? = null,
    onChange: ArgFun<Float?>? = null,
): CSStoreProperty<Float?> =
    CSFloatNullableStoreProperty(this, key, default, onChange)
        .parent(parent).listenLoad()

fun CSStore.nullLongProperty(
    key: String, default: Long? = null,
    onChange: ArgFun<Long?>? = null,
): CSStoreProperty<Long?> =
    CSLongNullableStoreProperty(this, key, default, onChange)

fun CSStore.nullLongProperty(
    parent: CSHasDestruct,
    key: String, default: Long? = null,
    onChange: ArgFun<Long?>? = null,
): CSStoreProperty<Long?> =
    CSLongNullableStoreProperty(this, key, default, onChange)
        .parent(parent).listenLoad()

fun CSStore.nullDoubleProperty(
    parent: CSHasDestruct,
    key: String, default: Double? = null,
    onChange: ArgFun<Double?>? = null,
): CSStoreProperty<Double?> =
    CSDoubleNullableStoreProperty(this, key, default, onChange)
        .parent(parent).listenLoad()