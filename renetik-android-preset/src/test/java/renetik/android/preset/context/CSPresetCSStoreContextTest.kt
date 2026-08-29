package renetik.android.preset.context

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import renetik.android.core.android.content.temporaryFile
import renetik.android.core.base.CSTestApplication
import renetik.android.core.java.io.readString
import renetik.android.core.lang.CSEnvironment.app
import renetik.android.event.lifecycle.CSModel
import renetik.android.preset.CSPreset
import renetik.android.preset.init
import renetik.android.preset.model.ClearPresetItemId
import renetik.android.preset.model.NotFoundPresetItem
import renetik.android.preset.model.TestCSPresetItemList
import renetik.android.preset.model.manageItems
import renetik.android.store.context.CSHasStoreContext
import renetik.android.store.context.CSHasStoreContext.Companion.destructClear
import renetik.android.store.context.property
import renetik.android.store.dataProperty
import renetik.android.store.type.CSFileJsonStore.Companion.CSFileJsonStore
import renetik.android.store.type.CSJsonObjectStore
import renetik.android.testing.CSAssert.assert
import renetik.android.testing.CSAssert.assertContains
import renetik.android.testing.CSAssert.assertContainsNot
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = CSTestApplication::class)
class CSPresetCSStoreContextTest {

    class JsonValue : CSJsonObjectStore() {
        var text: String by dataProperty("text", "")
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val parent = CSModel()
    private val presetList = TestCSPresetItemList(ClearPresetItemId)
    private val testFile = app.temporaryFile()
    private val fileStore = CSFileJsonStore(testFile, isJsonPretty = false)
    private val preset = CSPreset(
        parent, fileStore, "preset1", presetList, { NotFoundPresetItem() }
    ).manageItems().init()
    private val storeParent = object : CSModel(parent), CSHasStoreContext {
        override val store = PresetStoreContext(
            this, id = "contextId", preset, presetId = "contextKey"
        )
    }

    private fun checkStoreFor(content: String) = runBlocking {
        delay(2.seconds)
        fileStore.waitForWriteFinish()
        assertContains(actual = testFile.readString()!!, content)
    }

    private fun checkStoreForNot(content: String) = runBlocking {
        delay(2.seconds)
        fileStore.waitForWriteFinish()
        assertContainsNot(actual = testFile.readString()!!, content)
    }

    @Test
    fun presetReload() = runTest {
        val property = storeParent.store.property("property", 5)
        assertEquals(5, property.value)
        checkStoreFor(""""preset1 preset store":{}""")
        property.value = 10
        assertEquals(10, property.value)
        checkStoreFor(""""preset1 preset store":{"contextKey property":10}""")
        preset.reloadInternal()
        assertEquals(5, property.value)
        checkStoreFor(""""preset1 preset store":{}""")
    }

    @Test
    fun storeClear() = runTest {
        var propertyValue: Int? = null
        val property = storeParent.store.property("property", 5)
        property.onChange { propertyValue = it }
        property.value = 10
        assert(expected = 10, property.value)
        assert(expected = 10, propertyValue)
        checkStoreFor("""{"contextKey property":10}""")
        checkStoreForNot(""""preset1 preset store":{}""")
        storeParent.store.clear()
        assert(expected = 5, property.value)
        assert(expected = 5, propertyValue)
        checkStoreFor(""""preset1 preset store":{}""")
        checkStoreForNot("""{"contextKey property":10}""")
    }


    // We need to be able to destroy model first so no operations
    // happen when we clear properties to clear storage.
    @Test
    fun storeParentClearDestruct() = runTest {
        var propertyValue: Int? = null
        val property = storeParent.store.property("property", 5)
        property.onChange { propertyValue = it }
        property.value = 10
        assert(expected = 10, property.value)
        assert(expected = 10, propertyValue)
        checkStoreFor("""{"contextKey property":10}""")
        checkStoreForNot(""""preset1 preset store":{}""")
        storeParent.destructClear()
        assert(expected = 10, property.value)
        assert(expected = 10, propertyValue)
        checkStoreFor(""""preset1 preset store":{}""")
        checkStoreForNot("""{"contextKey property":10}""")
    }

    @Test
    fun jsonObjectProperty() = runTest {
        val property = storeParent.store.property<JsonValue>("property")

        assertEquals("", property.value.text)
        property.value = JsonValue().apply { text = "value" }

        assertEquals("value", property.value.text)
        checkStoreFor("""{"contextKey property":{"text":"value"}}""")
    }
}
