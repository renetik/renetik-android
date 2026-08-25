package renetik.android.ui.widget

import android.R.layout.simple_spinner_dropdown_item
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import renetik.android.core.kotlin.collections.contains
import renetik.android.core.kotlin.collections.index
import renetik.android.event.registration.CSRegistration

fun AutoCompleteTextView.reset() = apply { setAdapter(null); text(null) }

// simple_dropdown_item_1line
fun AutoCompleteTextView.setDropDown(
    stringArray: Int, selectedIndex: Int? = null,
    disableEdit: Boolean = true, isAutoClear: Boolean = true,
    onItemSelected: ((position: Int?) -> Unit)? = null
) = setDropDown(resources.getStringArray(stringArray).toList(),
    selectedIndex, disableEdit, isAutoClear, onItemSelected)

fun <T : AutoCompleteTextView> T.setDropDown(
    strings: List<String>, filter: ((List<String>, CharSequence?) -> List<String>)?,
    selectedIndex: Int? = null, isAutoClear: Boolean = true,
    onSelection: ((position: Int?) -> Unit)? = null
): CSRegistration {
    val adapter = object : ArrayAdapter<String>(
        // Filtering mutates the adapter's backing list se we need toMutableList
        context, simple_spinner_dropdown_item, strings.toMutableList()) {
        override fun getFilter(): Filter = filter?.let {
            StringArrayAdapterFilter(this, strings, it)
        } ?: super.getFilter()
    }
    selectedIndex?.let(strings::getOrNull)?.also { setText(it, false) }
    setAdapter(adapter)
    var selectedItem: String? = selectedIndex?.let { adapter.getItem(it) }
    val onFocus = if (isAutoClear) onFocusLost {
        if (selectedItem == null) clearText()
        else if (!strings.contains { it == text() }) {
            selectedItem = null
            clearText()
        }
    } else null
    val onTextChange = onTextChange {
        if (text.isBlank()) {
            selectedItem = null
            onSelection?.invoke(null)
        }
    }
    setOnItemClickListener { _, _, position, _ ->
        selectedItem = adapter.getItem(position)
        onSelection?.invoke(strings.index(selectedItem))
    }
    isFocusable = true
    isFocusableInTouchMode = true
    return CSRegistration(onFocus, onTextChange)
}

fun <T : AutoCompleteTextView> T.setDropDown(
    strings: List<String>, selectedIndex: Int? = null,
    disableEdit: Boolean = true, isAutoClear: Boolean = true,
    onSelection: ((position: Int?) -> Unit)? = null
): CSRegistration {
    if (disableEdit) keyListener = null //To disable user editing
    return setDropDown(strings,
        filter = if (disableEdit) { _, _ -> strings } else null,
        selectedIndex, isAutoClear, onSelection
    )
}

class StringArrayAdapterFilter(
    val adapter: ArrayAdapter<String>,
    val strings: List<String>,
    val performFilter: (List<String>, CharSequence?) -> List<String>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults =
        performFilter(strings, constraint).let {
            FilterResults().apply { values = it; count = it.size }
        }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        @Suppress("UNCHECKED_CAST")
        val list = results.values as? List<String> ?: return
        adapter.clear()
        adapter.addAll(list)
        adapter.notifyDataSetChanged()
    }
}
