package renetik.android.preset

import renetik.android.core.lang.ArgFun
import renetik.android.event.lifecycle.CSHasRegistrationsHasDestruct
import renetik.android.preset.Preset
import renetik.android.preset.property.nullable.CSBooleanNullablePresetProperty
import renetik.android.preset.property.nullable.CSDoubleNullablePresetProperty
import renetik.android.preset.property.nullable.CSFloatNullablePresetProperty
import renetik.android.preset.property.nullable.CSIntNullablePresetProperty
import renetik.android.preset.property.nullable.CSLongNullablePresetProperty
import renetik.android.preset.property.nullable.CSStringNullablePresetProperty

fun Preset.nullStringProperty(
    parent: CSHasRegistrationsHasDestruct, key: String, default: String? = null,
    onChange: ((value: String?) -> Unit)? = null
) = add(CSStringNullablePresetProperty(parent, this, key, default, onChange))

fun Preset.nullBoolProperty(
    parent: CSHasRegistrationsHasDestruct, key: String, default: Boolean? = null,
    onChange: ArgFun<Boolean?>? = null
) = add(CSBooleanNullablePresetProperty(parent, this, key, default, onChange))

fun Preset.nullIntProperty(
    parent: CSHasRegistrationsHasDestruct, key: String, default: Int? = null,
    onChange: ((value: Int?) -> Unit)? = null
) = add(CSIntNullablePresetProperty(parent, this, key, default, onChange))

fun Preset.nullFloatProperty(
    parent: CSHasRegistrationsHasDestruct, key: String, default: Float? = null,
    onChange: ((value: Float?) -> Unit)? = null
) = add(CSFloatNullablePresetProperty(parent, this, key, default, onChange))

fun Preset.nullLongProperty(
    parent: CSHasRegistrationsHasDestruct, key: String, default: Long? = null,
    onChange: ((value: Long?) -> Unit)? = null
) = add(CSLongNullablePresetProperty(parent, this, key, default, onChange))

fun Preset.nullDoubleProperty(
    parent: CSHasRegistrationsHasDestruct, key: String, default: Double? = null,
    onChange: ((value: Double?) -> Unit)? = null
) = add(CSDoubleNullablePresetProperty(parent, this, key, default, onChange))

