package renetik.android.preset

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.core.base.CSTestApplication
import renetik.android.core.lang.variable.assign
import renetik.android.event.lifecycle.CSModel
import renetik.android.preset.CSPreset.Companion.CSPreset
import renetik.android.preset.model.ClearPresetItemId
import renetik.android.preset.model.NotFoundPresetItem
import renetik.android.preset.model.TestCSPresetItemList
import renetik.android.preset.model.manageItems
import renetik.android.preset.model.userCategory
import renetik.android.preset.property.CSPresetProperty
import renetik.android.store.type.CSJsonObjectStore

/** An inner preset whose stored item id is not in its list keeps that id and is never modified. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = CSTestApplication::class)
class CSPresetNotFoundItemTest {
    private val parent = CSModel()
    private val list = TestCSPresetItemList(ClearPresetItemId)
    private val session = list.createItem("session", userCategory)
    private lateinit var preset: CSPreset<CSPresetItem, TestCSPresetItemList>
    private lateinit var child: CSPreset<CSPresetItem, TestCSPresetItemList>
    private lateinit var value: CSPresetProperty<Int>

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        // Inner presets hold the data of the item they loaded before, as app presets do.
        list.items.first().store.set("value", "1")
        preset = CSPreset(
            parent, CSJsonObjectStore(), "preset", list, { NotFoundPresetItem() }
        ).manageItems().init()
        child = CSPreset(parent, preset, "child", list, { NotFoundPresetItem() })
            .manageItems().init()
        value = child.property(parent, "value", 5).trackModified()
        session.store.reload(mapOf(
            child.listItem.key to "copied", child.store.key to mapOf("value" to "7")
        ))
        preset.listItem assign session
    }

    @After
    fun tearDown() {
        parent.onDestruct()
        Dispatchers.resetMain()
    }

    @Test
    fun anInnerPresetWithoutItsItemLoadsItsDataAndKeepsItsIdWhenSaved() = runTest {
        assertTrue(child.listItem.isNotFound)
        assertEquals(7, value.value)
        assertFalse(preset.isModified)

        preset.save(session)

        assertEquals("copied", session.store.getString(child.listItem.key))
    }

    @Test
    fun anInnerPresetWithoutItsItemIsNeverModifiedWhileItsParentIs() {
        value.value = 8

        assertFalse(child.isModified)
        assertTrue(preset.isModified)
    }

    @Test
    fun deletingTheSelectedItemLeavesThePresetNotFoundWithItsData() = runTest {
        preset.delete(session)

        assertTrue(preset.listItem.isNotFound)
        assertEquals(7, value.value)
        assertFalse(preset.isModified)
    }

    @Test
    fun selectingAnItemReplacesTheKeptId() = runTest {
        child.listItem assign list.items.first()

        preset.save(session)

        assertEquals(ClearPresetItemId, session.store.getString(child.listItem.key))
    }

    @Test
    fun linkingAnIdNotInTheListKeepsIt() = runTest {
        val linkedId = child.listItem.linkedId
        child.listItem assign list.items.first()

        child.listItem.link(linkedId)
        preset.save(session)

        assertEquals("copied", linkedId)
        assertTrue(child.listItem.isNotFound)
        assertEquals(linkedId, session.store.getString(child.listItem.key))
    }

    @Test
    fun deletingAnItemLeavesEveryPresetShowingItNotFound() = runTest {
        val other = CSPreset(parent, CSJsonObjectStore(), "other", list, { NotFoundPresetItem() })
            .manageItems().init()
        other.listItem assign session

        preset.delete(session)

        assertTrue(other.listItem.isNotFound)
        assertEquals(session.id, other.listItem.linkedId)
    }
}
