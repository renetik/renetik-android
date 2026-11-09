package renetik.android.store.type

import renetik.android.core.base.CSApplication.Companion.app
import renetik.android.core.java.io.readString
import renetik.android.core.java.io.write
import renetik.android.core.kotlin.changeIf
import renetik.android.core.lang.CSEnvironment
import renetik.android.json.CSJson
import renetik.android.json.parseJsonMap
import renetik.android.json.toJson
import java.io.File
import java.util.Collections.synchronizedMap

class CSSimpleFileJsonStore(
    val file: File,
    private val isJsonPretty: Boolean = CSEnvironment.isDebug
) : CSJsonObjectStore() {

    companion object {
        fun CSSimpleFileJsonStore(
            fileName: String,
            isJsonPretty: Boolean = CSJson.isJsonPretty,
        ) = CSSimpleFileJsonStore(
            File(app.filesDir, "$fileName.json"),
            isJsonPretty
        )
    }

    // Synchronized, so a save on another thread can copy it while properties change.
    override val data: MutableMap<String, Any?> =
        synchronizedMap(file.readString()?.parseJsonMap() ?: mutableMapOf())

    fun snapshotJson(): String = synchronized(data) { data.toMap() }
        .changeIf(isJsonPretty) { toSortedMap() }.toJson(formatted = isJsonPretty)

    fun save() = file.write(snapshotJson())
}