package renetik.android.json.data

import renetik.android.json.CSJsonListInterface
import renetik.android.json.parseJson
import renetik.android.json.toJsonString

@Suppress("unchecked_cast")
open class CSJsonList() : Iterable<Any?>, CSJsonListInterface {

    constructor(list: List<Any?>) : this() {
        load(list)
    }

    var index: Int? = null
    var key: String? = null
    internal val data = mutableListOf<Any?>()

    fun load(list: List<Any?>) = apply { data.addAll(list) }

    override fun toString() = toJsonString(formatted = true)

    override fun asList(): List<*> = data

    override fun iterator() = data.iterator()
}





