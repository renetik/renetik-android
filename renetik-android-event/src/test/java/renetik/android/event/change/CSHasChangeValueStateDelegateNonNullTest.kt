package renetik.android.event.change

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import renetik.android.core.lang.variable.assign
import renetik.android.event.lifecycle.CSModel
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.testing.CSAssert.assert

@RunWith(RobolectricTestRunner::class)
class CSHasChangeValueStateDelegateNonNullTest {

    @Test
    fun retainsLatestNonNullValueWithoutObserversAndIgnoresNullChanges() {
        val source = property<String?>("first")
        val retained = source.stateDelegateNonNull(CSModel())

        source assign "second"
        source assign null
        assert(expected = "second", actual = retained.value)

        val changes = mutableListOf<String>()
        retained.onChange { changes.add(it) }
        source assign "third"
        source assign null
        source assign "third"

        assert(expected = "third", actual = retained.value)
        assert(expected = listOf("third"), actual = changes)
    }

    @Test
    fun stopsUpdatingWhenParentRegistrationsAreCancelled() {
        val parent = CSModel()
        val source = property<String?>("first")
        val retained = source.stateDelegateNonNull(parent)

        parent.registrations.cancel()
        source assign "second"

        assert(expected = "first", actual = retained.value)
    }

    @Test(expected = IllegalStateException::class)
    fun requiresAnInitialNonNullValue() {
        property<String?>(null).stateDelegateNonNull()
    }
}
