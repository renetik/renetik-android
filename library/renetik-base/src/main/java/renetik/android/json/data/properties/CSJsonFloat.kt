package renetik.android.json.data.properties

import renetik.android.framework.lang.CSProperty
import renetik.android.json.data.CSJsonMap
import renetik.android.json.data.extensions.getDouble
import renetik.android.json.data.extensions.put

class CSJsonFloat(val data: CSJsonMap, private val key: String) : CSProperty<Float> {
    var float: Float?
        get() = data.getDouble(key)?.toFloat()
        set(value) {
            data.put(key, value)
        }

    override var value: Float
        get() = float ?: 0F
        set(value) {
            float = value
        }

    override fun toString() = "$value"
}