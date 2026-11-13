package renetik.android.controller.view.grid.item

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import renetik.android.controller.base.CSView
import renetik.android.controller.view.grid.CSGridView
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.CSLayoutRes.Companion.layout
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.invoke
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.event.registration.plus
import renetik.android.ui.view.firstChild

//TODO: Requires item to be wrapped in empty frame layout for now... ?
// It was like that for some reselection .. Now it is differently done in other places..
open class CSGridItemView<RowType : Any>(
    parent: CSGridView<*, *>,
    group: ViewGroup,
    @LayoutRes layout: Int,
) : CSView<ViewGroup>(parent, group, layout.layout) {

    constructor(parent: CSGridView<*, *>, @LayoutRes layout: Int)
            : this(parent, parent.view, layout)

    lateinit var value: RowType
    var index = -1
    val isLoaded get() = index != -1
    var itemDisabled = false
    val loadRegistrations = CSRegistrationsMap(debugClassName).also { this + it }
    val eventLoad = event<RowType>()

    fun load(value: RowType, index: Int) {
        this.index = index
        this.value = value
        loadRegistrations.onLoad(value)
        eventLoad(value)
    }

    protected open fun CSHasRegistrations.onLoad(data: RowType) = Unit

    override val contentView get() = view.firstChild!!
}

fun <RowType : Any, Item : CSGridItemView<RowType>> Item.onLoad(
    function: (Item).(value: RowType) -> Unit) = apply {
    this.eventLoad.listen { function(it) }
}