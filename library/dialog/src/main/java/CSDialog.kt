package renetik.android.dialog

import android.app.Activity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import renetik.android.base.CSContextController
import renetik.android.base.CSView
import renetik.android.extensions.applicationIcon
import renetik.android.extensions.applicationLogo
import renetik.android.extensions.colorFromAttribute
import renetik.android.extensions.inflate
import renetik.android.java.extensions.notNull
import renetik.android.java.extensions.string
import renetik.android.logging.CSLog.logWarn
import renetik.android.view.extensions.withClear

class CSDialog : CSContextController {

    constructor(activity: Activity) : super(activity)
    constructor(view: CSView<*>) : super(view)
    constructor(view: View) : super(view.context)

    private val builder = MaterialDialog.Builder(this);
    private var dialog: MaterialDialog? = null
    private var title: String? = null
    private var message: String? = null
    private var view: View? = null
    private var isShowAppIcon = true

    fun title(value: String) = apply { title = value }
    fun message(value: String) = apply { message = value }
    fun text(title: String, message: String) = title(title).message(message)
    fun show() = apply {
        title?.let { builder.title(it) }
        message?.let { builder.content(it) }
        styleDialogBuilder()
        dialog = builder.show()
    }

    private fun styleDialogBuilder() {
        builder.titleColorAttr(R.attr.colorOnSurface)
                .contentColorAttr(R.attr.colorPrimaryVariant)  //title,textfield color
                .linkColorAttr(R.attr.colorSecondaryVariant)  // notice attr is used instead of none or res for attribute resolving
                .dividerColorAttr(R.attr.colorSecondaryVariant)
                .backgroundColorAttr(R.attr.colorSurface)
                .positiveColor(colorFromAttribute(R.attr.colorPrimary))
                .neutralColorAttr(R.attr.colorPrimary)
                .negativeColorAttr(R.attr.colorOnSurface)
                .widgetColorAttr(R.attr.colorPrimaryVariant) //textField line
                .buttonRippleColorAttr(R.attr.colorSecondaryVariant)

        if (isShowAppIcon) applicationIcon()?.let { icon -> builder.icon(icon) }
                ?: let {
                    applicationLogo()?.let { logo -> builder.icon(logo) }
                            ?: logWarn("Not Icon nor Logo found for dialog")
                }
    }

    fun show(positiveText: String, onPositive: (CSDialog) -> Unit) = apply {
        builder.positiveText(positiveText).onPositive { _, _ -> onPositive(this) }
                .negativeText(R.string.cs_dialog_cancel)
    }.show()

    fun show(positiveText: String, onPositive: (CSDialog) -> Unit,
             onNegative: (CSDialog) -> Unit) = apply {
        builder.positiveText(positiveText).onPositive { _, _ -> onPositive(this) }
                .negativeText(R.string.cs_dialog_cancel).onNegative { _, _ -> onNegative(this) }
    }.show()

    fun show(onPositive: (CSDialog) -> Unit) = apply {
        builder.positiveText(R.string.cs_dialog_ok).onPositive { _, _ -> onPositive(this) }
                .negativeText(R.string.cs_dialog_cancel)
    }.show()

    fun showChoice(leftButton: String, leftButtonAction: (CSDialog) -> Unit,
                   rightButton: String, rightButtonAction: (CSDialog) -> Unit) = apply {
        builder.neutralText(leftButton).onNeutral { _, _ -> leftButtonAction(this) }
                .positiveText(rightButton).onPositive { _, _ -> rightButtonAction(this) }
    }.show()

    fun show(positiveText: String, positiveAction: (CSDialog) -> Unit
             , negativeText: String, negativeAction: (CSDialog) -> Unit) = apply {
        builder.positiveText(positiveText).onPositive { _, _ -> positiveAction(this) }
                .negativeText(negativeText).onNegative { _, _ -> negativeAction(this) }
    }.show()

    fun showIndeterminateProgress(onCancel: (CSDialog) -> Unit) = apply {
        builder.progress(true, 0).negativeText(R.string.cs_dialog_cancel).cancelable(false)
                .onNegative { _, _ -> onCancel(this) }
    }.show()

    fun showIndeterminateProgress(actionText: String, action: (CSDialog) -> Unit,
                                  cancelText: String, onCancel: (CSDialog) -> Unit) = apply {
        builder.positiveText(actionText).onPositive { _, _ -> action(this) }
                .negativeText(cancelText).onNegative { _, _ -> onCancel(this) }
                .progress(true, 0).cancelable(false)
    }.show()

    fun showInput(hint: String = "", value: String = "", positiveAction: (CSDialog) -> Unit) = apply {
        builder.positiveText(R.string.cs_dialog_ok)
                .input(hint, value, false) { _, _ -> positiveAction(this) }
                .negativeText(R.string.cs_dialog_cancel)
    }.show().apply { dialog?.inputEditText?.withClear() }

    fun inputValue(): String = string(dialog?.inputEditText?.text)

    fun cancelable(cancelable: Boolean) = apply { builder.cancelable(cancelable) }

    fun withIcon(showAppIcon: Boolean) = apply { isShowAppIcon = showAppIcon }

    fun onCancel(cancelAction: (CSDialog) -> Unit) =
            apply { builder.cancelListener { cancelAction(this) } }

    fun hide() = apply { dialog?.dismiss() }

    class CSOnDialogViewAction<ViewType : View>(val dialog: CSDialog, val view: ViewType)

    fun <ViewType : View> showView(view: ViewType,
                                   action: ((CSOnDialogViewAction<ViewType>) -> Boolean)? = null): View {
        if (notNull(title, message)) throw UnsupportedOperationException("No place for second text with custom view")
        styleDialogBuilder()
        this.view = view
        action?.let {
            builder.positiveText(R.string.cs_dialog_ok).onPositive { _, _ ->
                if (action(CSOnDialogViewAction(this, view))) dialog!!.dismiss()
            }
        }
        builder.title(title ?: message ?: "").autoDismiss(action == null).customView(view, true)
        dialog = builder.show()
        return view
    }
}

fun <ViewType : View> CSDialog.showViewOf(layoutId: Int, action: ((CSDialog.CSOnDialogViewAction<View>) -> Boolean)? = null) =
        showView(inflate<ViewType>(layoutId), action)

fun CSDialog.showView(layoutId: Int, action: ((CSDialog.CSOnDialogViewAction<View>) -> Boolean)? = null) =
        showViewOf<View>(layoutId, action)
