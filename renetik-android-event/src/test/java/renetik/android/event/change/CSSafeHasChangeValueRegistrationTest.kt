package renetik.android.event.change


import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks
import renetik.android.core.lang.tuples.CSQuadruple
import renetik.android.core.lang.tuples.CSSixtuple
import renetik.android.core.lang.tuples.to
import renetik.android.core.lang.variable.assign
import renetik.android.event.lifecycle.CSModel
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.event.property.CSSafeHasChangeValue
import renetik.android.event.property.CSSafeProperty.Companion.safe
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistration.Companion.CSRegistration
import renetik.android.testing.CSAssert.assert
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

@RunWith(RobolectricTestRunner::class)
class CSSafeHasChangeValueRegistrationTest {

    @Test
    fun normalValueAndSafeBooleanKeepsUnsafePropagation() {
        val parent = CSModel()
        val source = property(1)
        val gate = property(false).safe(parent)
        val gated = source and gate
        var unsafeValue: Int? = null
        var safeValue: Int? = null
        gated.onUnsafeChange { unsafeValue = it }
        gated.onChange { safeValue = it }

        source assign 2
        assert(expected = 2, actual = gated.value)
        assert(expected = null, actual = unsafeValue)
        assert(expected = null, actual = safeValue)

        Thread { gate assign true }.apply {
            start()
            join()
        }
        assert(expected = 2, actual = gated.value)
        assert(expected = 2, actual = unsafeValue)
        assert(expected = null, actual = safeValue)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 2, actual = safeValue)
    }

    @Test
    fun safeIdentityHasChangeValueKeepsUnsafePropagation() {
        val parent = CSModel()
        val source = property(1).safe(parent)
        val identity = source.safeStateDelegate(parent)
        var unsafeValue: Int? = null
        var safeValue: Int? = null
        identity.onUnsafeChange { unsafeValue = it }
        identity.onChange { safeValue = it }

        Thread { source assign 2 }.apply {
            start()
            join()
        }
        assert(expected = 2, actual = identity.value)
        assert(expected = 2, actual = unsafeValue)
        assert(expected = null, actual = safeValue)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 2, actual = safeValue)
    }

    @Test
    fun normalIdentityHasChangeValueKeepsUnsafePropagation() {
        val parent = CSModel()
        val sourceProperty = property(1)
        val source: CSHasChangeValue<Int> = sourceProperty
        var callbackValue: Int? = null
        val identity = source.safeStateDelegate(parent, onChange = { callbackValue = it })
        var unsafeValue: Int? = null
        var safeValue: Int? = null
        identity.onUnsafeChange { unsafeValue = it }
        identity.onChange { safeValue = it }

        Thread { sourceProperty assign 2 }.apply {
            start()
            join()
        }
        assert(expected = 2, actual = identity.value)
        assert(expected = 2, actual = unsafeValue)
        assert(expected = null, actual = safeValue)
        assert(expected = null, actual = callbackValue)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 2, actual = safeValue)
        assert(expected = 2, actual = callbackValue)
    }

    @Test
    fun safeFifthTupleOnChangeUsesUnsafePropagation() {
        val parent = CSModel()
        val item1 = property(1)
        val item2 = property(2)
        val item3 = property(3)
        val item4 = property(4)
        val item5 = property(5).safe(parent)
        var value: Int? = null

        (item1 to item2 to item3 to item4 to item5).onUnsafeChange { first, second, third, fourth, fifth ->
            value = first + second + third + fourth + fifth
        }
        Thread { item5 assign 6 }.apply {
            start()
            join()
        }
        assert(expected = 16, actual = value)
    }

    @Test
    fun tupleOnUnsafeChangeRefreshesValuesChangedWhileRegistering() {
        val parent = CSModel()
        val item1 = MutatesOnRegisterValue(initialValue = 1, valueAfterRegister = 2)
        val item2 = property(2).safe(parent)
        var value: Int? = null

        (item1 to item2).onUnsafeChange { first, second ->
            value = first + second
        }
        item2 assign 3

        assert(expected = 5, actual = value)
    }

    @Test
    fun tupleOnUnsafeChangeUsesDeclaredSourceChannel() {
        val runtimeSafeItem = ManualSafeValue(1)
        val item1: CSHasChangeValue<Int> = runtimeSafeItem
        val item2 = ManualSafeValue(2)
        var value: Pair<Int, Int>? = null

        (item1 to item2).onUnsafeChange { first, second ->
            value = first to second
        }

        runtimeSafeItem.unsafeValue(3)
        assert(expected = null, actual = value)

        runtimeSafeItem.changeValue(4)
        assert(expected = 4 to 2, actual = value)

        item2.unsafeValue(5)
        assert(expected = 4 to 5, actual = value)
    }

    @Test
    fun tupleOnUnsafeChangeBufferedDeliversEveryConcurrentChange() {
        val values = onUnsafeChangeConcurrentChanges(CSSafeChangeDelivery.Buffered)
        assert(expected = listOf(1 to 0, 1 to 1, 2 to 1), actual = values)
    }

    @Test
    fun tupleOnUnsafeChangeConflatedDropsIntermediateConcurrentChange() {
        val values = onUnsafeChangeConcurrentChanges(CSSafeChangeDelivery.Conflated)
        assert(expected = listOf(1 to 0, 2 to 1), actual = values)
    }

    // Drives three changes where the first callback blocks while owning the drainer, so the
    // remaining two are enqueued concurrently. Buffered keeps both; Conflated coalesces them
    // into the latest, dropping the (1, 1) intermediate.
    private fun onUnsafeChangeConcurrentChanges(
        delivery: CSSafeChangeDelivery,
    ): List<Pair<Int, Int>> {
        val item1 = ManualSafeValue(0)
        val item2 = ManualSafeValue(0)
        val firstCallbackStarted = CountDownLatch(1)
        val releaseFirstCallback = CountDownLatch(1)
        val values = Collections.synchronizedList(mutableListOf<Pair<Int, Int>>())

        (item1 to item2).onUnsafeChange(delivery) { first, second ->
            if (first == 1 && second == 0) {
                firstCallbackStarted.countDown()
                releaseFirstCallback.await(1, SECONDS)
            }
            values.add(first to second)
        }

        val firstThread = Thread { item1.unsafeValue(1) }
        firstThread.start()
        assert(expected = true, actual = firstCallbackStarted.await(1, SECONDS))

        // First callback is parked holding the drainer; these enqueue without draining.
        item2.unsafeValue(1)
        item1.unsafeValue(2)
        releaseFirstCallback.countDown()
        firstThread.join(1000)

        assert(expected = false, actual = firstThread.isAlive)
        return values.toList()
    }

    @Test
    fun safeFifthTupleHasChangeValueFromKeepsUnsafePropagation() {
        val parent = CSModel()
        val item1 = property(1)
        val item2 = property(2)
        val item3 = property(3)
        val item4 = property(4)
        val item5 = property(5).safe(parent)
        val combined = (item1 to item2 to item3 to item4 to item5)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth, fifth ->
                first + second + third + fourth + fifth
            })
        var unsafeValue: Int? = null
        var safeValue: Int? = null
        combined.onUnsafeChange { unsafeValue = it }
        combined.onChange { safeValue = it }

        Thread { item5 assign 6 }.apply {
            start()
            join()
        }
        assert(expected = 16, actual = combined.value)
        assert(expected = 16, actual = unsafeValue)
        assert(expected = null, actual = safeValue)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 16, actual = safeValue)
    }

    @Test
    fun safeLastTwoQuintupleHasChangeValueFromKeepsUnsafePropagation() {
        val parent = CSModel()
        val item1 = property(1)
        val item2 = property(2)
        val item3 = property(3).safe(parent)
        val item4 = property(4)
        val item5 = property(5).safe(parent)
        val combined = (item1 to item2 to item4 to item3 to item5)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, fourth, third, fifth ->
                first + second + fourth + third + fifth
            })
        var unsafeValue: Int? = null
        var safeValue: Int? = null
        combined.onUnsafeChange { unsafeValue = it }
        combined.onChange { safeValue = it }

        Thread { item3 assign 4; item5 assign 6 }.apply {
            start()
            join()
        }
        assert(expected = 17, actual = combined.value)
        assert(expected = 17, actual = unsafeValue)
        assert(expected = null, actual = safeValue)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 17, actual = safeValue)
    }

    @Test
    fun safeLastTwoSixtupleHasChangeValueFromKeepsUnsafePropagation() {
        val parent = CSModel()
        val item1 = property(1)
        val item2 = property(2)
        val item3 = property(3)
        val item4 = property(4).safe(parent)
        val item5 = property(5)
        val item6 = property(6).safe(parent)
        val combined = (item1 to item2 to item3 to item5 to item4 to item6)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fifth, fourth, sixth ->
                first + second + third + fifth + fourth + sixth
            })
        var unsafeValue: Int? = null
        var safeValue: Int? = null
        combined.onUnsafeChange { unsafeValue = it }
        combined.onChange { safeValue = it }

        Thread { item4 assign 5; item6 assign 7 }.apply {
            start()
            join()
        }
        assert(expected = 23, actual = combined.value)
        assert(expected = 23, actual = unsafeValue)
        assert(expected = null, actual = safeValue)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 23, actual = safeValue)
    }

    @Test
    fun safeTrailingTupleHasChangeValueIdentityKeepsUnsafePropagation() {
        val parent = CSModel()
        val item1 = property(1)
        val item2 = property(2)
        val item3 = property(3).safe(parent)
        val item4 = property(4).safe(parent)
        val item5 = property(5)
        val item6 = property(6).safe(parent)

        val safeThird = (item1 to item2 to item3).safeStateDelegate(parent)
        val safeFourth = (item1 to item2 to item5 to item4).safeStateDelegate(parent)
        val safeSixth = (item1 to item2 to item5 to item3 to item4 to item6)
            .safeStateDelegate(parent)
        var safeThirdUnsafe: Triple<Int, Int, Int>? = null
        var safeFourthUnsafe: CSQuadruple<Int, Int, Int, Int>? = null
        var safeSixthUnsafe: CSSixtuple<Int, Int, Int, Int, Int, Int>? = null
        var safeSixthSafe: CSSixtuple<Int, Int, Int, Int, Int, Int>? = null
        safeThird.onUnsafeChange { safeThirdUnsafe = it }
        safeFourth.onUnsafeChange { safeFourthUnsafe = it }
        safeSixth.onUnsafeChange { safeSixthUnsafe = it }
        safeSixth.onChange { safeSixthSafe = it }

        Thread { item3 assign 4; item4 assign 5; item6 assign 7 }.apply {
            start()
            join()
        }
        assert(expected = 4, actual = safeThird.value.third)
        assert(expected = 4, actual = safeThirdUnsafe?.third)
        assert(expected = 5, actual = safeFourth.value.fourth)
        assert(expected = 5, actual = safeFourthUnsafe?.fourth)
        assert(expected = 4, actual = safeSixth.value.fourth)
        assert(expected = 4, actual = safeSixthUnsafe?.fourth)
        assert(expected = 5, actual = safeSixth.value.fifth)
        assert(expected = 5, actual = safeSixthUnsafe?.fifth)
        assert(expected = 7, actual = safeSixth.value.sixth)
        assert(expected = 7, actual = safeSixthUnsafe?.sixth)
        assert(expected = null, actual = safeSixthSafe)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 7, actual = safeSixthSafe?.sixth)
    }

    @Test
    fun safeTrailingTupleHasChangeValueFromKeepsUnsafePropagation() {
        val parent = CSModel()
        val item1 = property(1)
        val item2 = property(2)
        val item3 = property(3).safe(parent)
        val item4 = property(4).safe(parent)
        val item5 = property(5)
        val item6 = property(6).safe(parent)
        val safeSecond = (item1 to item3)
            .safeStateDelegate(parent, unsafeFromValues = { first, second -> first + second })
        val safeThird = (item1 to item2 to item3)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third ->
                first + second + third
            })
        val safeFourth = (item1 to item2 to item5 to item4)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth ->
                first + second + third + fourth
            })
        val safeSixth = (item1 to item2 to item5 to item3 to item4 to item6)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth, fifth, sixth ->
                first + second + third + fourth + fifth + sixth
            })
        var safeSecondUnsafe: Int? = null
        var safeThirdUnsafe: Int? = null
        var safeFourthUnsafe: Int? = null
        var safeSixthUnsafe: Int? = null
        safeSecond.onUnsafeChange { safeSecondUnsafe = it }
        safeThird.onUnsafeChange { safeThirdUnsafe = it }
        safeFourth.onUnsafeChange { safeFourthUnsafe = it }
        safeSixth.onUnsafeChange { safeSixthUnsafe = it }

        Thread { item3 assign 4; item4 assign 5; item6 assign 7 }.apply {
            start()
            join()
        }
        assert(expected = 5, actual = safeSecond.value)
        assert(expected = 5, actual = safeSecondUnsafe)
        assert(expected = 7, actual = safeThird.value)
        assert(expected = 7, actual = safeThirdUnsafe)
        assert(expected = 13, actual = safeFourth.value)
        assert(expected = 13, actual = safeFourthUnsafe)
        assert(expected = 24, actual = safeSixth.value)
        assert(expected = 24, actual = safeSixthUnsafe)
        runUiThreadTasksIncludingDelayedTasks()
        assert(expected = 24, actual = safeSixth.value)
        assert(expected = 24, actual = safeSixthUnsafe)
    }

    @Test
    fun safeStateDelegateSupportsTrailingSafeMatrix() {
        val parent = CSModel()
        val item1 = property(1)
        val item2 = property(2)
        val item3 = property(3)
        val item4 = property(4)
        val safe1 = property(5).safe(parent)
        val safe2 = property(6).safe(parent)
        val safe3 = property(7).safe(parent)

        val with2Safe2 = (safe1 to safe2)
            .safeStateDelegate(parent, unsafeFromValues = { first, second -> first + second })
        val with3Safe2 = (item1 to safe1 to safe2)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third ->
                first + second + third
            })
        val with4Safe2 = (item1 to item2 to safe1 to safe2)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth ->
                first + second + third + fourth
            })
        val with5Safe2 = (item1 to item2 to item3 to safe1 to safe2)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth, fifth ->
                first + second + third + fourth + fifth
            })
        val with6Safe2 = (item1 to item2 to item3 to item4 to safe1 to safe2)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth, fifth, sixth ->
                first + second + third + fourth + fifth + sixth
            })

        val with3Safe3 = (safe1 to safe2 to safe3)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third ->
                first + second + third
            })
        val with4Safe3 = (item1 to safe1 to safe2 to safe3)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth ->
                first + second + third + fourth
            })
        val with5Safe3 = (item1 to item2 to safe1 to safe2 to safe3)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth, fifth ->
                first + second + third + fourth + fifth
            })
        val with6Safe3 = (item1 to item2 to item3 to safe1 to safe2 to safe3)
            .safeStateDelegate(parent, unsafeFromValues = { first, second, third, fourth, fifth, sixth ->
                first + second + third + fourth + fifth + sixth
            })

        val identity2Safe2 = (safe1 to safe2).safeStateDelegate(parent)
        val identity3Safe2 = (item1 to safe1 to safe2).safeStateDelegate(parent)
        val identity4Safe2 = (item1 to item2 to safe1 to safe2).safeStateDelegate(parent)
        val identity5Safe2 = (item1 to item2 to item3 to safe1 to safe2).safeStateDelegate(parent)
        val identity6Safe2 = (item1 to item2 to item3 to item4 to safe1 to safe2)
            .safeStateDelegate(parent)
        val identity3Safe3 = (safe1 to safe2 to safe3).safeStateDelegate(parent)
        val identity4Safe3 = (item1 to safe1 to safe2 to safe3).safeStateDelegate(parent)
        val identity5Safe3 = (item1 to item2 to safe1 to safe2 to safe3).safeStateDelegate(parent)
        val identity6Safe3 = (item1 to item2 to item3 to safe1 to safe2 to safe3)
            .safeStateDelegate(parent)

        assert(expected = 11, actual = with2Safe2.value)
        assert(expected = 12, actual = with3Safe2.value)
        assert(expected = 14, actual = with4Safe2.value)
        assert(expected = 17, actual = with5Safe2.value)
        assert(expected = 21, actual = with6Safe2.value)
        assert(expected = 18, actual = with3Safe3.value)
        assert(expected = 19, actual = with4Safe3.value)
        assert(expected = 21, actual = with5Safe3.value)
        assert(expected = 24, actual = with6Safe3.value)
        assert(expected = 11, actual = identity2Safe2.value.first + identity2Safe2.value.second)
        assert(expected = 12, actual = identity3Safe2.value.toList().sum())
        assert(expected = 14, actual = identity4Safe2.value.first + identity4Safe2.value.second +
                identity4Safe2.value.third + identity4Safe2.value.fourth)
        assert(expected = 17, actual = identity5Safe2.value.first + identity5Safe2.value.second +
                identity5Safe2.value.third + identity5Safe2.value.fourth + identity5Safe2.value.fifth)
        assert(expected = 21, actual = identity6Safe2.value.first + identity6Safe2.value.second +
                identity6Safe2.value.third + identity6Safe2.value.fourth + identity6Safe2.value.fifth +
                identity6Safe2.value.sixth)
        assert(expected = 18, actual = identity3Safe3.value.toList().sum())
        assert(expected = 19, actual = identity4Safe3.value.first + identity4Safe3.value.second +
                identity4Safe3.value.third + identity4Safe3.value.fourth)
        assert(expected = 21, actual = identity5Safe3.value.first + identity5Safe3.value.second +
                identity5Safe3.value.third + identity5Safe3.value.fourth + identity5Safe3.value.fifth)
        assert(expected = 24, actual = identity6Safe3.value.first + identity6Safe3.value.second +
                identity6Safe3.value.third + identity6Safe3.value.fourth + identity6Safe3.value.fifth +
                identity6Safe3.value.sixth)
    }

    private class MutatesOnRegisterValue<T>(
        initialValue: T,
        private val valueAfterRegister: T,
    ) : CSHasChangeValue<T> {
        override var value: T = initialValue

        override fun onChange(function: (T) -> Unit): CSRegistration {
            value = valueAfterRegister
            return CSRegistration.Empty
        }
    }

    private class ManualSafeValue<T>(
        initialValue: T,
    ) : CSSafeHasChangeValue<T> {
        @Volatile
        override var value: T = initialValue
        private val changeListeners = CopyOnWriteArrayList<(T) -> Unit>()
        private val unsafeChangeListeners = CopyOnWriteArrayList<(T) -> Unit>()

        override fun onChange(function: (T) -> Unit): CSRegistration =
            register(changeListeners, function)

        override fun onUnsafeChange(function: (T) -> Unit): CSRegistration =
            register(unsafeChangeListeners, function)

        fun unsafeValue(newValue: T) {
            value = newValue
            unsafeChangeListeners.forEach { it(newValue) }
        }

        fun changeValue(newValue: T) {
            value = newValue
            changeListeners.forEach { it(newValue) }
        }

        private fun register(
            listeners: CopyOnWriteArrayList<(T) -> Unit>,
            function: (T) -> Unit,
        ): CSRegistration {
            listeners.add(function)
            return CSRegistration(isActive = true, onCancel = { listeners.remove(function) })
        }
    }
}
