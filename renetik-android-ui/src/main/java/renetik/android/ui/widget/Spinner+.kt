package renetik.android.ui.widget

import android.R.layout.simple_spinner_dropdown_item
import android.R.layout.simple_spinner_item
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner

fun <Data> Spinner.data(context: Context, values: Collection<Data>, selected: Data? = null) =
    data(context, simple_spinner_item, simple_spinner_dropdown_item, values, selected)

fun <Data> Spinner.data(context: Context, itemLayout: Int, dropDownItemLayout: Int,
                        values: Collection<Data>, selected: Data? = null) {
    adapter = ArrayAdapter(context, itemLayout, values.toList()).apply {
        setDropDownViewResource(dropDownItemLayout)
    }
    selected?.let { setSelection(values.indexOf(it), false) }
}

fun <Data> Spinner.data(context: Context, values: Collection<Data>,
                        selected: Data? = null, title: (Data) -> String) =
    data(context, simple_spinner_item, simple_spinner_dropdown_item, values, selected, title)

fun <Data> Spinner.data(context: Context, itemLayout: Int, dropDownItemLayout: Int,
                        values: Collection<Data>, selected: Data? = null, title: (Data) -> String) {
    adapter = ArrayAdapter(context, itemLayout, values.map(title)).apply {
        setDropDownViewResource(dropDownItemLayout)
    }
    setSelection(values.indexOf(selected).coerceAtLeast(0), false)
}

fun <Data> Spinner.selected(values: List<Data>): Data? = values.getOrNull(selectedItemPosition)
