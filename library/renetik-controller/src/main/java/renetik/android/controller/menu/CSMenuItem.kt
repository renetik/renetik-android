package renetik.android.controller.menu

import android.view.MenuItem.*
import android.view.View
import renetik.android.controller.base.CSActivityView

/**
 * Created by Rene Dohan on 18/1/17.
 */
open class CSMenuItem {

    companion object {
        private var lastGeneratedMenuItemId = 10
    }

    val isNeverAsAction: Boolean get() = showAsAction == SHOW_AS_ACTION_NEVER
    val id: Int
    val isGenerated: Boolean
    private val controller: CSActivityView<*>
    var title: String? = null
    var isVisible = true
    var isChecked: Boolean? = null
    var iconResource = 0
    var showAsAction = SHOW_AS_ACTION_IF_ROOM
    private var runWith: ((CSMenuItem) -> Unit)? = null
    var actionView: View? = null

    constructor(controller: CSActivityView<*>, id: Int) {
        this.controller = controller
        this.id = id
        isGenerated = false
    }

    constructor(controller: CSActivityView<*>, title: String) {
        this.controller = controller
        this.id = lastGeneratedMenuItemId++
        isGenerated = true
        this.title = title
    }

    fun onClick(run: (CSMenuItem) -> Unit) = apply { runWith = run }
    fun run() = runWith?.invoke(this)
    fun hide() = visible(false)
    fun show() = visible(true)

    fun visible(visible: Boolean) = apply {
        if (isVisible == visible) return this
        isVisible = visible
        controller.activity().invalidateOptionsMenu()
    }

    fun onChecked(onItem: CSOnMenuItem) {
        isChecked = !isChecked!!
        onItem.isChecked = isChecked!!
        run()
    }

    fun setIconResourceId(iconResourceId: Int) = apply { this.iconResource = iconResourceId }
    fun neverAsAction() = apply { showAsAction = SHOW_AS_ACTION_NEVER }
    fun alwaysAsAction() = apply { showAsAction = SHOW_AS_ACTION_ALWAYS }
    fun withText() = apply { showAsAction = showAsAction or SHOW_AS_ACTION_WITH_TEXT }
//    fun remove() = controller.removeMenuItem(this)
}

