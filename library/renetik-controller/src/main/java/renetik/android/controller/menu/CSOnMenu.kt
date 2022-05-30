package renetik.android.controller.menu

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import renetik.android.content.attributeColor
import renetik.android.framework.base.CSContext
import renetik.android.framework.event.property.CSEventPropertyFunctions.property
import renetik.android.primitives.isSet

const val GeneratedMenuItems = 7687678

class CSOnMenu(private val activity: AppCompatActivity, val menu: Menu) :
    CSContext(activity) {

    val showMenu = property(true)

    fun show(item: CSMenuItem) {
        if (item.isGenerated) createProgrammaticallyItem(item)
        else menu.findItem(item.id)?.initialize(item)
    }

    private fun MenuItem.initialize(item: CSMenuItem) {
        isVisible = true
        item.title?.let { title = it }
        if (isCheckable) item.isChecked?.let { isChecked = it }
            ?: let { item.isChecked = isChecked }
        actionView?.let { item.actionView }
    }

    private fun createProgrammaticallyItem(item: CSMenuItem) {
        menu.add(GeneratedMenuItems, item.id, Menu.NONE, item.title).apply {
            setShowAsAction(item.showAsAction)
            if (item.iconResource.isSet) setIcon(item.iconResource)
            item.isChecked?.let {
                isCheckable = true
                isChecked = it
            }
            item.actionView?.let { actionView = it }
            // Title color was not taken from theme
            title = SpannableString(title).apply {
                setSpan(
                    ForegroundColorSpan(
                        attributeColor(com.google.android.material.R.attr.colorOnSurface)
                    ), 0, length, 0
                )
            }
        }
    }

    fun inflate(menuId: Int) = activity.menuInflater.inflate(menuId, menu)

}