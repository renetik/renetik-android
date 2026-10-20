package renetik.android.preset

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.core.base.CSTestApplication
import renetik.android.core.kotlin.collections.first
import renetik.android.core.lang.variable.assign
import renetik.android.event.lifecycle.CSModel
import renetik.android.event.lifecycle.destruct
import renetik.android.json.toJson
import renetik.android.preset.CSPreset.Companion.CSPreset
import renetik.android.preset.property
import renetik.android.preset.model.ClearPresetItemId
import renetik.android.preset.model.NotFoundPresetItem
import renetik.android.preset.model.TestCSPresetItemList
import renetik.android.preset.model.defaultCategory
import renetik.android.preset.model.manageItems
import renetik.android.preset.property.max
import renetik.android.store.reload
import renetik.android.store.type.CSJsonObjectStore
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.CSAssert.assertContains

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = CSTestApplication::class)
class CSPresetSimpleTests {

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun presetPropertyPresetReload() = runTest {
        val parent = CSModel()
        val presetList = TestCSPresetItemList(ClearPresetItemId)

        val store = CSJsonObjectStore()
        val preset = CSPreset(
            parent, store, "preset1", presetList, { NotFoundPresetItem() }
        ).manageItems().init()
        val property = preset.property(parent, "preset1 property", 5)

        assertEquals(5, property.value)

        property.value = 10
        assertEquals(10, property.value)

        preset.reloadInternal()
        assertEquals(5, property.value)
    }

    @Test
    fun childPresetPropertyPresetReload() = runTest {
        val parent = CSModel()
        val presetList = TestCSPresetItemList(ClearPresetItemId)

        val preset = CSPreset(
            parent, CSJsonObjectStore(), "preset", presetList, { NotFoundPresetItem() }
        ).manageItems().init()
        val childPreset = CSPreset(
            parent, preset, "childPreset", presetList, { NotFoundPresetItem() }
        ).manageItems().init()
        val childPresetProperty = childPreset.property(parent, "preset1 property", 5)
        assertEquals(5, childPresetProperty.value)

        childPresetProperty.value = 10
        assertEquals(10, childPresetProperty.value)

        preset.reloadInternal()
        advanceUntilIdle()
        assertEquals(5, childPresetProperty.value)
    }

    @Test
    fun childPresetPropertySaveAsNewItemPresetReload() = runTest {
        val parent = CSModel()
        val preset = CSPreset(
            parent, CSJsonObjectStore(), "preset",
            TestCSPresetItemList(firstItemId = "clear preset item"),
            { NotFoundPresetItem() }
        ).manageItems().init()
        val firstItemId = "clear childPreset item"
        val childPreset = CSPreset(
            parent, preset, "childPreset",
            TestCSPresetItemList(firstItemId = firstItemId),
            { NotFoundPresetItem() }, defaultItemId = firstItemId
        ).manageItems().init()
        val childPresetProperty = childPreset.property(parent, "childPresetProperty", 5)
        assertEquals(5, childPresetProperty.value)
        advanceUntilIdle()
        assertContains(
            preset.store.data.toJson(),
            // Change: Not saving preset item on load and store load
//            """"childPreset preset current":"clear childPreset item"""",
            """"childPreset preset store":{}}""",
        )

        childPresetProperty.value = 10
        assertEquals(10, childPresetProperty.value)
        val item = childPreset.list.createItem(
            "childPresetProperty item", category = defaultCategory)
        childPreset.save(item)
        childPreset.listItem assign item
        advanceUntilIdle()
        assertEquals("""{
  "childPreset preset current": "childPresetProperty item",
  "childPreset preset store": {
    "childPresetProperty": 10,
    "preset title": "title"
  }
}""", preset.store.data.toJson(formatted = true))

        preset.reloadInternal()
        advanceUntilIdle()
        assert(expected = 1, preset.store.data.size)
        // Change: Not saving preset item on load and store load
//        assert(
//            expected = "clear childPreset item",
//            actual = preset.store.data["childPreset preset current"]
//        )
        assert(
            expected = """{}""",
            actual = preset.store.data["childPreset preset store"]?.toJson()
        )
        assertEquals(5, childPresetProperty.value)
    }

    @Test
    fun presetPropertyMax() {
        val parent = CSModel()
        val presetList = TestCSPresetItemList(ClearPresetItemId)

        val store = CSJsonObjectStore()
        store.reload("""{"test preset store":{"key":10}}""")
        val preset = CSPreset(
            parent, store, "test", presetList, { NotFoundPresetItem() }
        ).manageItems().init()
        val property = preset.property(parent, "key", 5)
        assertEquals(10, property.value)

        val propertyMax = preset.property(parent, "key", 5).max(7)
        assertEquals(7, propertyMax.value)
    }

    @Test
    fun presetLoadingDefaultDataOnInit() {
        val parent = CSModel()
        val presetList = TestCSPresetItemList("DefaultItemId")
        presetList.items(defaultCategory).first!!.store.reload("""{"key":100}""")
        val preset = CSPreset(
            parent, CSJsonObjectStore(), "test", presetList, { NotFoundPresetItem() }
        ).manageItems().init()
        val property = preset.property(parent, "key", 5)
        assertEquals(100, property.value)
    }


    @Test
    fun presetIsModifiedTest() = runTest {
        val parent = CSModel()
        val store = CSJsonObjectStore()
        val presetList = TestCSPresetItemList(ClearPresetItemId)
        val preset = CSPreset(parent, store, "test", presetList, { NotFoundPresetItem() })
            .manageItems().init()
        assertEquals(1, preset.registrations.size)

        val propertyParent = CSModel()
        val property = preset.property(propertyParent, "key", 10).trackModified()

        val isModifiedParent = CSModel()
        val isModified = preset.isModified(parent = isModifiedParent)
        assertEquals(3, preset.registrations.size)

        assertEquals(false, isModified.value)
        property assign 6
        advanceUntilIdle()
        assertEquals(true, isModified.value)
        property assign 10
        advanceUntilIdle()
        assertEquals(false, isModified.value)

        isModifiedParent.destruct()
        assertEquals(1, preset.registrations.size)
    }

    @Test
    fun aPropertyCountsAsModifiedOnlyWhileItsConditionHolds() {
        val parent = CSModel()
        val presetList = TestCSPresetItemList(ClearPresetItemId)
        val preset = CSPreset(
            parent, CSJsonObjectStore(), "test", presetList, { NotFoundPresetItem() }
        ).init()
        val child = CSPreset(parent, preset, "child", presetList, { NotFoundPresetItem() }).init()
        var isTracked = false
        child.title.trackModifiedIf { isTracked }

        child.title assign "Renamed"

        assertEquals(false, child.isModified)
        assertEquals(false, preset.isModified)
        isTracked = true
        assertEquals(true, child.isModified)
        assertEquals(true, preset.isModified)
        parent.destruct()
    }
}
