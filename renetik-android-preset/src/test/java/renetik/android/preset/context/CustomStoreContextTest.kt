package renetik.android.preset.context

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.core.base.CSTestApplication
import renetik.android.event.lifecycle.CSModel
import renetik.android.json.toJson
import renetik.android.store.context.CSHasStoreContext
import renetik.android.store.context.CSHasStoreContext.Companion.destructClear
import renetik.android.store.type.CSJsonObjectStore

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = CSTestApplication::class)
class CustomStoreContextTest {
    private val parent = CSModel()
    private val jsonStore = CSJsonObjectStore()
    private val storeParent = object : CSModel(parent), CSHasStoreContext {
        override val store = CustomStoreContext(this, store = jsonStore, key = "context")
    }

    @Test
    fun storeParentClear() {
        val property = storeParent.store.property("property", 5)
        property.value = 10
        assertEquals("""{"context property":10}""", jsonStore.toJson())
        storeParent.store.clear()
        assertEquals("""{}""", jsonStore.toJson())
    }

    // We need to be able to destroy model first so no operations
    // happen when we clear properties to clear storage.
    @Test
    fun storeParentClearDestruct() {
        val property = storeParent.store.property("property", 5)
        property.value = 10
        assertEquals("""{"context property":10}""", jsonStore.toJson())
        storeParent.destructClear()
        assertEquals("""{}""", jsonStore.toJson())
    }
}
