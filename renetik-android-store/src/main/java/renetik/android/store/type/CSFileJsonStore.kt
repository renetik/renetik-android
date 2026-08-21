@file:OptIn(ExperimentalCoroutinesApi::class)

package renetik.android.store.type

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import renetik.android.core.base.CSApplication.Companion.app
import renetik.android.core.java.io.readString
import renetik.android.core.java.io.writeAtomic
import renetik.android.core.kotlin.changeIf
import renetik.android.core.kotlin.collections.reload
import renetik.android.core.kotlin.throwCancellation
import renetik.android.core.lang.CSEnvironment.isDebug
import renetik.android.core.lang.value.isFalse
import renetik.android.core.lang.variable.setFalse
import renetik.android.core.lang.variable.setTrue
import renetik.android.core.logging.CSLog.logError
import renetik.android.core.logging.CSLog.logInfo
import renetik.android.event.change.waitForTrue
import renetik.android.event.dispatch.JobRegistration
import renetik.android.event.dispatch.launch
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.onDestructed
import renetik.android.event.property.CSAtomicProperty
import renetik.android.json.CSJson
import renetik.android.json.parseJsonMap
import renetik.android.json.toJson
import java.io.File
import kotlin.time.Duration.Companion.seconds

class CSFileJsonStore(
    parent: CSHasDestruct? = null,
    val file: File,
    private val isPretty: Boolean = isDebug,
) : CSJsonObjectStore() {
    companion object {
        val SAVE_DELAY = 1.seconds
        fun CSFileJsonStore(
            parent: CSHasDestruct? = null,
            fileName: String,
            isJsonPretty: Boolean = CSJson.isJsonPretty,
        ) = CSFileJsonStore(
            parent, File(app.filesDir, "$fileName.json"),
            isJsonPretty
        )

        fun CSFileJsonStore(
            fileName: String,
            isJsonPretty: Boolean = CSJson.isJsonPretty,
        ) = CSFileJsonStore(
            null, File(app.filesDir, "$fileName.json"),
            isJsonPretty
        )

        fun CSFileJsonStore(
            file: File, isJsonPretty: Boolean = isDebug,
        ) = CSFileJsonStore(null, file, isJsonPretty)
    }

    override val data: MutableMap<String, Any?> = mutableMapOf()

    override fun save(key: String, value: Any?) {
        super.save(key, value)
        dispatcher.launch {
            dataToSave[key] = value
            saveChannel.trySend(Unit)
        }
    }

    override fun clear(key: String) {
        super.clear(key)
        dispatcher.launch {
            dataToSave.remove(key)
            saveChannel.trySend(Unit)
        }
    }

    override fun clear() {
        if (data.isEmpty()) return
        file.delete()
        super.clear()
        dispatcher.launch {
            dataToSave.clear()
            saveChannel.trySend(Unit)
        }
    }

    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val dataToSave: MutableMap<String, Any?> = mutableMapOf()
    private var writerRegistration: JobRegistration? = null
    private var isWriteFinished = CSAtomicProperty(parent, false)
    private val saveChannel = Channel<Unit>(capacity = CONFLATED)

    init {
        file.readString()?.parseJsonMap()?.also {
            data.reload(it)
            dataToSave.reload(it)
        }
        startSaving()
        parent?.onDestructed(::close)
    }

    fun restart() {
        logInfo()
        writerRegistration?.cancel()
        startSaving()
    }

    private fun startSaving() {
        writerRegistration = dispatcher.launch {
            runCatching {
                for (signal in saveChannel) {
                    isWriteFinished.setFalse()
                    delay(SAVE_DELAY)
                    saveData().onFailure {
                        if (it is OutOfMemoryError || it is CancellationException) throw it
                        else logError(it)
                    }
                    isWriteFinished.setTrue()
                }
            }.onFailure {
                if (it is CancellationException &&
                    (!saveChannel.isEmpty || isWriteFinished.isFalse)) {
                    saveData().onFailure(::onFailure)
                } else onFailure(it)
            }.throwCancellation()
        }
    }

    private fun saveData() = runCatching {
        val data = dataToSave.changeIf(isPretty) { toSortedMap() }
        val string = data.toJson(formatted = isPretty)
        file.writeAtomic(string)
    }

    private fun onFailure(it: Throwable) {
        if (it !is CancellationException) logError(it)
    }

    @Suppress("unused")
    suspend fun waitForWriteFinish() = isWriteFinished.waitForTrue()

    fun close(wait: Boolean = true) {
        saveChannel.close()
        if (wait) runBlocking { writerRegistration?.waitToFinish() }
        else writerRegistration?.cancel()
    }
}
