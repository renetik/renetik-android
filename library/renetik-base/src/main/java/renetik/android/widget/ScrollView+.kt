package renetik.android.widget

import android.view.View
import android.widget.ScrollView
import renetik.android.framework.event.CSViewInterface
import renetik.android.view.locationOnScreen

fun ScrollView.scrollToChild(child: View, centered: Boolean = true) {
    smoothScrollTo(0, child.y.toInt() -
            if (centered) ((height / 2) - (child.height / 2)) else 0)
}

fun ScrollView.scrollTo(view: CSViewInterface, centered: Boolean = true) {
    val viewY = view.view.locationOnScreen.y - locationOnScreen.y
    smoothScrollTo(0, viewY - if (centered) ((height / 2) - (view.view.height / 2)) else 0)
}