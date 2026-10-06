package renetik.android.preset

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.core.base.CSTestApplication
import renetik.android.event.lifecycle.CSModel
import renetik.android.preset.CSPreset.Companion.CSPreset
import renetik.android.preset.model.ClearPresetItemId
import renetik.android.preset.model.NotFoundPresetItem
import renetik.android.preset.model.TestCSPresetItemList
import renetik.android.preset.model.userCategory
import renetik.android.store.type.CSJsonObjectStore
import renetik.android.testing.CSAssert.assert

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = CSTestApplication::class)
class CSPresetSaveStepTest {

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val preset = CSPreset(
        CSModel(), CSJsonObjectStore(), "preset",
        TestCSPresetItemList(ClearPresetItemId), { NotFoundPresetItem() }
    ).init()

    @Test
    fun saveRunsTheStepBeforeEventSave() = runTest {
        val calls = mutableListOf<String>()
        val item = preset.list.createItem("saved", userCategory)
        preset.onSaveToItem = { saved, edit ->
            calls += "step ${saved.id}"
            edit(saved.store)
        }
        preset.eventSave.listen { calls += "eventSave ${it.id}" }

        preset.save(item) { it.set("edited", "true") }

        assert(expected = listOf("step saved", "eventSave saved"), actual = calls)
        assert(expected = "true", actual = item.store.getString("edited"))
    }

    @Test
    fun aFailingStepReachesTheCallerWithoutEventSave() = runTest {
        var isEventSaveFired = false
        val item = preset.list.createItem("saved", userCategory)
        preset.onSaveToItem = { _, _ -> error("Copy failed") }
        preset.eventSave.listen { isEventSaveFired = true }

        val result = runCatching { preset.save(item) }

        assert(expected = "Copy failed", actual = result.exceptionOrNull()?.message)
        assert(expected = false, actual = isEventSaveFired)
    }
}
