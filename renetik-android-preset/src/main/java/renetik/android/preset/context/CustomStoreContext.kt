package renetik.android.preset.context

import renetik.android.core.lang.ArgFun
import renetik.android.core.lang.CSHasId
import renetik.android.event.change.invoke
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.CSModel
import renetik.android.event.lifecycle.parent
import renetik.android.event.registration.CSRegistration
import renetik.android.json.obj.CSJsonObjectInterface
import renetik.android.preset.CSPreset
import renetik.android.store.CSStore
import renetik.android.store.context.CSStoreContext
import renetik.android.store.nullDoubleProperty
import renetik.android.store.nullFloatProperty
import renetik.android.store.nullIntProperty
import renetik.android.store.nullListItemProperty
import renetik.android.store.nullStringProperty
import renetik.android.store.operation
import renetik.android.store.property
import renetik.android.store.property.CSStoreProperty
import renetik.android.store.property.listenLoad
import renetik.android.store.property.value.CSHasIdListValueStoreProperty
import renetik.android.store.property.value.CSIntListValueStoreProperty

class CustomStoreContext(
    parent: CSHasDestruct? = null,
    val store: CSStore,
    private val hasId: CSHasId? = null,
    override val key: String? = null,
) : CSModel(parent), CSStoreContext {
    override val id = hasId?.id?.let { id -> key?.let { "$id $it" } ?: id } ?: key ?: ""
    override val data: CSJsonObjectInterface = store
    private val childContexts = mutableListOf<CSStoreContext>()
    private val properties = mutableListOf<CSStoreProperty<*>>()
    private val presets = mutableListOf<CSPreset<*, *>>()

    private fun <T : CSStoreContext> T.init() = apply {
        childContexts += this
        eventDestruct { childContexts -= this }
    }

    override fun context(parent: CSHasDestruct, key: String?) =
        (key?.let { CustomStoreContext(parent, store, this, key) }
            ?: CustomStoreContext(parent, store, hasId, this.key)).init()

    override fun appContext(parent: CSHasDestruct, key: String?) =
        AppStoreContext(parent, this, key).init()

    override fun memoryContext(parent: CSHasDestruct, key: String?) =
        RuntimeStoreContext(parent, this, key).init()

    override fun onChange(function: (Unit) -> Unit): CSRegistration =
        store.eventLoaded.listen { function(Unit) }

    fun add(preset: CSPreset<*, *>) {
        presets += preset
        preset.eventDestruct { if (!isDestructed) presets -= preset }
    }

    fun <T : CSStoreProperty<*>> add(property: T): T = property.apply {
        properties += this
        eventDestruct { properties -= this }
    }

    override fun clear() = store.operation {
        properties.toList().forEach { it.clear() }
        childContexts.toList().onEach { it.clear() }
        presets.toList().onEach { it.clear() }
    }

    fun storeKey(key: String) = if (id.isNotBlank()) "$id $key" else key

    override fun property(
        key: String, default: String, onChange: ArgFun<String>?,
    ) = add(store.property(this, storeKey(key), default, onChange))

    override fun property(
        key: String, default: Boolean, onChange: ArgFun<Boolean>?,
    ) = add(store.property(this, storeKey(key), default, onChange))

    override fun property(
        key: String, default: Float, onChange: ArgFun<Float>?,
    ) = add(store.property(this, storeKey(key), default, onChange))

    override fun property(
        key: String, default: Int, onChange: ArgFun<Int>?
    ) = add(store.property(this, storeKey(key), default, onChange))

    override fun property(
        key: String, default: () -> Int, onChange: ArgFun<Int>?
    ) = add(store.property(this, storeKey(key), default, onChange))

    override fun <T> property(
        key: String, values: () -> Collection<T>, default: () -> T, onChange: ArgFun<T>?
    ) = add(store.property(this, storeKey(key), values, default, onChange))

    override fun nullIntProperty(
        key: String, default: Int?, onChange: ((value: Int?) -> Unit)?
    ) = add(store.nullIntProperty(this, storeKey(key), default, onChange))

    override fun nullFloatProperty(
        key: String, default: Float?, onChange: ((value: Float?) -> Unit)?
    ) = add(store.nullFloatProperty(this, storeKey(key), default, onChange))

    override fun nullDoubleProperty(
        key: String, default: Double?, onChange: ((value: Double?) -> Unit)?
    ) = add(store.nullDoubleProperty(this, storeKey(key), default, onChange))

    override fun nullStringProperty(
        key: String, default: String?, onChange: ((value: String?) -> Unit)?
    ) = add(store.nullStringProperty(this, storeKey(key), default, onChange))

    override fun <T> nullListItemProperty(
        key: String, values: List<T>, default: T?, onChange: ((value: T?) -> Unit)?
    ) = add(store.nullListItemProperty(storeKey(key), values, default, onChange)
        .parent(this).listenLoad())

    override fun property(
        key: String, default: List<Int>, onChange: ArgFun<List<Int>>?
    ) = add(CSIntListValueStoreProperty(store, storeKey(key), default, onChange)
        .parent(this).listenLoad())

    override fun <T : CSHasId> property(
        key: String, values: List<T>,
        default: List<T>, onChange: ArgFun<List<T>>?
    ) = add(CSHasIdListValueStoreProperty(store, storeKey(key), default, onChange = onChange)
        .parent(this).listenLoad())
}