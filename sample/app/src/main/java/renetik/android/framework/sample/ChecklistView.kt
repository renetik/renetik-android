package renetik.android.framework.sample

import android.graphics.Color
import android.view.Gravity.CENTER_VERTICAL
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import renetik.android.controller.base.push
import renetik.android.controller.navigation.CSNavigationItemView
import renetik.android.controller.navigation.fullScreen
import renetik.android.core.android.content.dpToPixel
import renetik.android.core.lang.tuples.to
import renetik.android.ui.view.findView
import renetik.android.ui.view.onClick
import renetik.android.ui.widget.HorizontalLayout
import renetik.android.ui.widget.VerticalLayout
import renetik.android.ui.widget.text
import renetik.android.ui.widget.textColorInt

class ChecklistView(parent: MainView) :
    CSNavigationItemView(parent, viewLayout = R.layout.sample_checklist) {

    val checks: List<ModuleCheck> by lazy { ModuleChecks(context).all }

    val allPassed: Boolean get() = checks.all { it.passed }

    override fun onViewReady() {
        super.onViewReady()
        applySystemBarInsets()
        val container: LinearLayout =
            layoutView.findView(R.id.sample_checklist_container)!!
        container.addView(TextView(context).text("Renetik Android 2.0")
            .apply { textSize = 22f })
        container.addView(TextView(context)
            .text("Module checklist — every row is a live exercise")
            .apply { textSize = 14f })
        checks.forEach { container.addView(row(it)) }
        container.addView(pushDemoButton())
    }

    private fun applySystemBarInsets() {
        val (left, top, right, bottom) = layoutView.paddingLeft to
                layoutView.paddingTop to layoutView.paddingRight to
                layoutView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(layoutView) { view, windowInsets ->
            val insets = windowInsets.getInsets(systemBars() or displayCutout())
            view.updatePadding(
                left + insets.left, top + insets.top,
                right + insets.right, bottom + insets.bottom,
            )
            windowInsets
        }
        ViewCompat.requestApplyInsets(layoutView)
    }

    private fun row(check: ModuleCheck): LinearLayout =
        VerticalLayout(context).apply {
            val padding = context.dpToPixel(8)
            setPadding(0, padding, 0, padding)
            addView(HorizontalLayout(context).apply {
                gravity = CENTER_VERTICAL
                addView(TextView(context).text(if (check.passed) "✓" else "✗")
                    .textColorInt(if (check.passed) GREEN else RED)
                    .apply { textSize = 18f; tag = statusTag(check) })
                addView(TextView(context).text("  ${check.module}")
                    .apply { textSize = 16f })
            })
            addView(TextView(context).text(check.details)
                .apply { textSize = 12f })
            check.demoView?.let { addView(it(context), demoParams()) }
        }

    private fun pushDemoButton() = Button(context)
        .text("Push detail view (controller navigation)")
        .onClick { DetailView(this).fullScreen().push() }

    private fun demoParams() = LayoutParams(MATCH_PARENT, WRAP_CONTENT)

    companion object {
        private val GREEN = Color.rgb(0, 140, 60)
        private val RED = Color.rgb(200, 30, 30)
        fun statusTag(check: ModuleCheck) = "status-${check.module}-${check.passed}"
    }
}
