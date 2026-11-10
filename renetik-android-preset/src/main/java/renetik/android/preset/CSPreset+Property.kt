package renetik.android.preset

import renetik.android.core.lang.ArgFun
import renetik.android.core.lang.CSHasId
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.preset.Preset
import renetik.android.preset.property.value.CSBooleanValuePresetProperty
import renetik.android.preset.property.value.CSDoubleValuePresetProperty
import renetik.android.preset.property.value.CSFloatValuePresetProperty
import renetik.android.preset.property.value.CSHasIdListValuePresetProperty
import renetik.android.preset.property.value.CSIntListValuePresetProperty
import renetik.android.preset.property.value.CSIntValuePresetProperty
import renetik.android.preset.property.value.CSListItemValuePresetProperty
import renetik.android.preset.property.value.CSStringValuePresetProperty

fun Preset.property(
    parent: CSHasDestruct, key: String,
    getDefault: () -> String, onChange: ArgFun<String>? = null
) = add(CSStringValuePresetProperty(parent, this, key, getDefault, onChange))

fun Preset.property(
    parent: CSHasDestruct, key: String,
    default: String, onChange: ArgFun<String>? = null
) = add(CSStringValuePresetProperty(parent, this, key, default, onChange))

fun Preset.property(
    parent: CSHasDestruct, key: String,
    default: Boolean, onChange: ArgFun<Boolean>? = null
) = add(CSBooleanValuePresetProperty(parent, this, key, default, onChange))

fun Preset.property(
    parent: CSHasDestruct, key: String,
    default: Int, onChange: ArgFun<Int>? = null
) = add(CSIntValuePresetProperty(parent, this, key, default, onChange))

fun Preset.property(
    parent: CSHasDestruct, key: String,
    default: () -> Int, onChange: ArgFun<Int>? = null
) = add(CSIntValuePresetProperty(parent, this, key, default, onChange))

fun Preset.property(
    parent: CSHasDestruct, key: String,
    default: Float, onChange: ArgFun<Float>? = null
) = add(CSFloatValuePresetProperty(parent, this, key, default, onChange))

fun Preset.property(
    parent: CSHasDestruct, key: String,
    default: Double, onChange: ArgFun<Double>? = null
) = add(CSDoubleValuePresetProperty(parent, this, key, default, onChange))

fun Preset.property(
    parent: CSHasDestruct, key: String,
    default: List<Int>, onChange: ArgFun<List<Int>>? = null
) = add(CSIntListValuePresetProperty(parent, this, key, default, onChange))

fun Preset.intListProperty(
    parent: CSHasDestruct, key: String,
    getDefault: () -> List<Int>, onChange: ArgFun<List<Int>>? = null
) = add(CSIntListValuePresetProperty(parent, this, key, getDefault, onChange))

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, values: List<T>,
    default: T, onChange: ArgFun<T>? = null
) = add(CSListItemValuePresetProperty(parent, this, key, values, default, onChange))

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, values: List<T>,
    getDefault: () -> T, onChange: ArgFun<T>? = null
) = add(
    CSListItemValuePresetProperty(
        parent, this, key, { values }, getDefault, onChange
    )
)

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, values: () -> Collection<T>,
    getDefault: () -> T, onChange: ArgFun<T>? = null
) = add(
    CSListItemValuePresetProperty(
        parent, this, key, values, getDefault, onChange
    )
)

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, getValues: () -> List<T>,
    defaultIndex: Int, onChange: ArgFun<T>? = null
) = add(
    CSListItemValuePresetProperty(
        parent, this, key, getValues, { getValues()[defaultIndex] }, onChange
    )
)

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, getValues: () -> List<T>,
    default: T, onChange: ArgFun<T>? = null
) = add(
    CSListItemValuePresetProperty(
        parent, this, key, getValues, { default }, onChange
    )
)

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, values: List<T>,
    defaultIndex: Int, onChange: ArgFun<T>? = null
) = property(parent, key, values, values[defaultIndex], onChange)

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, values: Array<T>,
    default: T, onChange: ArgFun<T>? = null
) = property(parent, key, values.asList(), default, onChange)

fun <T> Preset.property(
    parent: CSHasDestruct, key: String, values: Array<T>,
    defaultIndex: Int, onChange: ArgFun<T>? = null
) = property(parent, key, values.asList(), values[defaultIndex], onChange)

fun <T : CSHasId> Preset.property(
    parent: CSHasDestruct, key: String, values: List<T>,
    default: List<T> = emptyList(), onChange: ArgFun<List<T>>? = null
) = add(CSHasIdListValuePresetProperty(parent, this, key, values, default, onChange))

fun <T : CSHasId> Preset.property(
    parent: CSHasDestruct, key: String, values: Array<T>,
    default: List<T> = emptyList(), onChange: ArgFun<List<T>>? = null
) = property(parent, key, values.asList(), default, onChange)