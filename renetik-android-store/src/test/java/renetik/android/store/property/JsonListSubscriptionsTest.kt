package renetik.android.store.property

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.event.lifecycle.destruct
import renetik.android.store.SimpleJsonObjectStore
import renetik.android.store.dataListProperty
import renetik.android.store.type.CSJsonObjectStore

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class JsonListSubscriptionsTest {
    @Test fun replacementAndDisposalCancelSubscriptionsAndOldListsCannotWriteBack() {
        val store = CSJsonObjectStore()
        val property = store.dataListProperty<SimpleJsonObjectStore>("items")
        var previous = SimpleJsonObjectStore(string = "first")
        property.value = listOf(previous)
        repeat(10) { index ->
            val current = SimpleJsonObjectStore(string = "item $index")
            property.value = listOf(current)
            assertFalse(previous.eventChanged.isListened)
            assertTrue(current.eventChanged.isListened)
            previous.string = "obsolete"
            assertSame(current, property.value.single())
            current.string = "changed $index"
            assertEquals("changed $index", store.getJsonObjectList("items", SimpleJsonObjectStore::class)!!.single().string)
            previous = current
        }
        (property as renetik.android.event.lifecycle.CSHasDestruct).destruct()
        assertFalse(previous.eventChanged.isListened)
    }
}
