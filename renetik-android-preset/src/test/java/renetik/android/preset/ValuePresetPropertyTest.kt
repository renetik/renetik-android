package renetik.android.preset

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import renetik.android.event.lifecycle.CSModel
import renetik.android.preset.property
import renetik.android.preset.model.ClearPresetItemId
import renetik.android.preset.model.NotFoundPresetItem
import renetik.android.preset.model.TestCSPresetItemList
import renetik.android.preset.model.manageItems
import renetik.android.store.type.CSJsonObjectStore
import renetik.android.store.type.CSJsonObjectStore.Companion.CSJsonObjectStore

@RunWith(RobolectricTestRunner::class)
class ValuePresetPropertyTest {
    private val parent = CSModel()
    private val itemList = TestCSPresetItemList(ClearPresetItemId)
    private val store = CSJsonObjectStore()
    private val preset = CSPreset(
        parent, store, "preset1", itemList, { NotFoundPresetItem() }
    ).manageItems().init()

    @Test
    fun testStringProperty() {
        var eventCount = 0
        var value: String by preset.property(parent, "key", default = "initial") {
            eventCount += 1
        }
        assertEquals("initial", value)
        value = "initial"
        assertEquals(0, eventCount)
        value = "new value"

        preset.reloadInternal()
        assertEquals("initial", value)
    }

    @Test
    fun testPropertyFollowingPresetOnlyIfStored() {
        val property = preset.property(parent, "key", default = 1).followPresetOnlyIfStored()
        property.value = 5

        preset.reload(mapOf())
        assertEquals(5, property.value)

        preset.reload(mapOf("key" to "7"))
        assertEquals(7, property.value)
    }

    @Test
    fun testPropertyKeptOnItemReloadIsSavedToParentStore() {
        val property = preset.property(parent, "key", default = 1).followPresetOnlyIfStored()
        property.value = 5

        preset.reloadInternal()
        assertEquals(5, property.value)
        val savedPresetStore = CSJsonObjectStore(store.getMap(preset.store.key)!!)
        assertEquals(5, savedPresetStore.getInt("key"))
    }

//    @Test
//    fun testBooleanProperty() {
//        var value: Boolean by preset.property(parent, "key", default = false)
//        assertEquals(false, value)
//        value = true
//        assertEquals("""{"key":true}""", store.toJson())
//
//        store.reload(store.toJson())
//        val value2: Boolean by preset.property(parent, "key", default = false)
//        assertEquals(true, value2)
//    }
//
//    @Test
//    fun testIntProperty() {
//        var value: Int by preset.property(parent, "key", default = 5)
//        assertEquals(5, value)
//        value = 345
//        assertEquals("""{"key":345}""", store.toJson())
//
//        store.reload(store.toJson())
//        val value2: Int by preset.property(parent, "key", default = 10)
//        assertEquals(345, value2)
//    }
//
//    @Test
//    fun testFloatProperty() {
//        var eventCount = 0
//        var value: Float by preset.property(parent, "key", default = 1.5f) { eventCount += 1 }
//        assertEquals(1.5f, value)
//        value = 2.5f
//        assertEquals("""{"key":2.5}""", store.toJson())
//
//        store.reload("""{"key":2.3}""")
//        assertEquals(2, eventCount)
//
//        val value2: Float by preset.property(parent, "key", default = 542f)
//        assertEquals(2.3f, value2)
//    }
//
//    @Test
//    fun testDoubleProperty() {
//        var eventCount = 0
//        var value: Double by preset.property(parent, "key", default = 1.5) { eventCount += 1 }
//        assertEquals(1.5, value, 0.0)
//        value = 2.3
//        assertEquals("""{"key":"2.3"}""", store.toJson(forceString = true))
//
//        store.reload(store.toJson())
//        val value2: Double by preset.property(parent, "key", default = 5.5)
//        assertEquals(2.3, value2, 0.0)
//        assertEquals(1, eventCount)
//    }


}

