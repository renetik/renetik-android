package renetik.android.preset

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.core.base.CSTestApplication
import renetik.android.core.lang.variable.assign
import renetik.android.event.lifecycle.CSModel
import renetik.android.json.CSJson
import renetik.android.preset.CSPreset.Companion.CSPreset
import renetik.android.preset.model.ClearPresetItemId
import renetik.android.preset.model.NotFoundPresetItem
import renetik.android.preset.model.TestCSPresetItemList
import renetik.android.preset.model.userCategory
import renetik.android.preset.property.CSPresetProperty
import renetik.android.store.type.CSJsonObjectStore

/** An inner preset whose parent stores only what it changes, so the rest follows its item. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = CSTestApplication::class)
class CSPresetOverridesTest {
    private val parent = CSModel()
    private val sessions = TestCSPresetItemList(ClearPresetItemId)
    private val singles = TestCSPresetItemList("single")
    private val session = sessions.createItem("session", userCategory)
    private val single get() = singles.items.first()
    private lateinit var preset: CSPreset<CSPresetItem, TestCSPresetItemList>
    private lateinit var track: CSPreset<CSPresetItem, TestCSPresetItemList>
    private lateinit var volume: CSPresetProperty<Int>
    private lateinit var pan: CSPresetProperty<Int>

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        CSJson.forceString = true
        single.store.reload(mapOf("volume" to "1", "pan" to "2"))
        preset = CSPreset(
            parent, CSJsonObjectStore(), "preset", sessions,
            notFoundItem = { NotFoundPresetItem() }
        ).init()
        track = trackPreset("track")
        volume = track.property(parent, "volume", 5).trackModified()
        pan = track.property(parent, "pan", 6)
    }

    @After
    fun tearDown() {
        parent.onDestruct()
        CSJson.forceString = false
        Dispatchers.resetMain()
    }

    private fun trackPreset(key: String): CSPreset<CSPresetItem, TestCSPresetItemList> =
        CSPreset(parent, preset, key, singles, notFoundItem = { NotFoundPresetItem() })
            .apply { overrides.isEnabled = true }.init()

    private fun loadSessionStoringLateTrack(stored: Map<String, Any?>) {
        session.store.reload(
            mapOf("late preset current" to single.id, "late preset store" to stored))
        preset.listItem assign session
        preset.reloadInternal()
    }

    private fun loadSession(stored: Map<String, Any?>, linkedId: String = single.id) {
        session.store.reload(mapOf(track.listItem.key to linkedId, track.store.key to stored))
        preset.listItem assign session
        preset.reloadInternal()
    }

    @Test
    fun aStoredKeyLiesOverItsItemAndLeavesTheParentUnmodified() {
        loadSession(mapOf("pan" to "9"))

        assertEquals(1, volume.value)
        assertEquals(9, pan.value)
        assertFalse(preset.isModified)
        assertEquals(mapOf("pan" to "9"), track.overrides.changes)
    }

    @Test
    fun aKeyThatIsNotStoredFollowsItsItem() {
        loadSession(mapOf("pan" to "9"))

        single.store.set("volume", "3")
        preset.reloadInternal()

        assertEquals(3, volume.value)
        assertEquals(9, pan.value)
    }

    @Test
    fun aPresetLoadedFromItsLinkHasNoChanges() {
        loadSession(emptyMap())

        assertEquals(emptyMap<String, Any?>(), track.overrides.changes)
    }

    @Test
    fun aValueChangedAfterLoadJoinsTheStoredKeys() {
        loadSession(mapOf("pan" to "9"))

        volume.value = 7

        assertEquals(mapOf("pan" to "9", "volume" to "7"), track.overrides.changes)
    }

    @Test
    fun aValueChangedBackToItsItemIsNoChange() {
        loadSession(mapOf("pan" to "9"))

        pan.value = 2

        assertEquals(emptyMap<String, Any?>(), track.overrides.changes)
    }

    @Test
    fun aKeyRemovedFromItsItemStaysRemovedAfterReload() {
        loadSession(emptyMap())

        pan.clear()
        val changes = track.overrides.changes
        assertEquals(mapOf("pan" to null), changes)
        loadSession(changes)

        assertEquals(6, pan.value)
    }

    @Test
    fun aPresetCreatedAfterItsParentLoadedLiesItsStoredKeysOverItsItem() {
        loadSessionStoringLateTrack(mapOf("pan" to "9"))

        val late = trackPreset("late")

        assertEquals(1, late.property(parent, "volume", 5).value)
        assertEquals(9, late.property(parent, "pan", 6).value)
        assertEquals(mapOf("pan" to "9"), late.overrides.changes)
    }

    @Test
    fun aPresetRestoredFromAnEarlierRunStoresOnlyItsChanges() {
        loadSessionStoringLateTrack(mapOf("pan" to "9"))
        preset.store.set("late preset store",
            mapOf("volume" to "1", "pan" to "9", "written by load" to "8"))

        val late = trackPreset("late")
        val volume = late.property(parent, "volume", 5)

        assertEquals(mapOf("pan" to "9"), late.overrides.changes)
        volume.value = 7
        assertEquals(mapOf("pan" to "9", "volume" to "7"), late.overrides.changes)
    }

    @Test
    fun reloadingThePresetFromItsItemDropsWhatItsParentStored() {
        loadSession(mapOf("pan" to "9"))

        track.reloadInternal()

        assertEquals(2, pan.value)
        assertEquals(emptyMap<String, Any?>(), track.overrides.changes)
    }

    @Test
    fun aPresetWithoutItsItemLoadsOnlyWhatItsParentStored() {
        loadSession(mapOf("pan" to "9"), linkedId = "missing")

        assertEquals(5, volume.value)
        assertEquals(9, pan.value)
    }

    @Test
    fun aPresetItsParentItemStoresWholeLoadsOnlyThatData() {
        track.overrides.isStoredWholeBy = { it == session }

        loadSession(mapOf("pan" to "9"))

        assertEquals(5, volume.value)
        assertEquals(9, pan.value)
        assertFalse(preset.isModified)
    }

    @Test
    fun aPresetWithoutOverridesLoadsOnlyWhatItsParentStored() {
        track.overrides.isEnabled = false

        loadSession(mapOf("pan" to "9"))

        assertEquals(5, volume.value)
    }
}
