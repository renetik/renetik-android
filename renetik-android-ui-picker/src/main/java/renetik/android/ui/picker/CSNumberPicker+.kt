package renetik.android.ui.picker

import android.widget.NumberPicker.FOCUS_BEFORE_DESCENDANTS
import android.widget.NumberPicker.FOCUS_BLOCK_DESCENDANTS
import com.shawnlin.numberpicker.NumberPicker
import renetik.android.core.kotlin.asStringArray
import renetik.android.core.kotlin.collections.hasItems
import renetik.android.core.kotlin.collections.index
import renetik.android.core.lang.variable.assign
import renetik.android.event.change.action
import renetik.android.event.property.CSProperty
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.paused
import renetik.android.ui.protocol.view
import renetik.android.ui.view.fadeIn
import renetik.android.ui.view.invisible
import renetik.android.ui.protocol.CSViewInterface

fun CSViewInterface.numberPicker(viewId: Int) = view(viewId) as CSNumberPicker

fun CSNumberPicker.index(property: CSProperty<Int>): CSRegistration {
    val propertyRegistration = property.action { index assign it }
    val indexRegistration = index.onChange {
        propertyRegistration.paused { property assign it }
    }
    return CSRegistration(propertyRegistration, indexRegistration)
}

fun <T> CSNumberPicker.load(
    property: CSProperty<T>, data: List<Pair<String, T>>
): CSRegistration {
    load(data.map { it.first }, data.indexOfFirst { it.second == property.value })
    return index.onChange { property.value = data[it].second }
}

fun <Row : Any> CSNumberPicker.load(data: List<Row>, selected: Row? = null) =
    load(data, selected = data.index(selected) ?: 0)

fun <Row : Any> CSNumberPicker.load(data: List<Row>, selected: Int = 0) = apply {
    if (data.hasItems) {
        fadeIn()
        minValue = 0
        val newMaxValue = data.size - 1
        if (newMaxValue > maxValue) {
            displayedValues = data.asStringArray
            maxValue = newMaxValue
        } else {
            maxValue = newMaxValue
            displayedValues = data.asStringArray
        }
        index.value = selected
    } else invisible()
}

fun <T : NumberPicker> T.circulate(circulate: Boolean) = apply {
    wrapSelectorWheel = circulate
}

fun <T : NumberPicker> T.min(value: Int) = apply { minValue = value }

fun <T : NumberPicker> T.max(value: Int) = apply { maxValue = value }

fun <T : NumberPicker> T.disableTextEditing(disable: Boolean) = apply {
    descendantFocusability = if (disable) FOCUS_BLOCK_DESCENDANTS
    else FOCUS_BEFORE_DESCENDANTS
}