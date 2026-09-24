package renetik.android.core.android.content

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import renetik.android.core.android.content.CSToastTime.ShortTime
import renetik.android.core.android.os.CSHandler.mainHandler
import renetik.android.core.android.os.isThreadMain
import renetik.android.core.android.os.send
import renetik.android.core.lang.CSEnvironment.app

object CSToast {
    fun toast(text: String) = app.toast(text)

    fun toast(text: String, time: CSToastTime = ShortTime) =
        app.toast(text, time)
}

enum class CSToastTime(val value: Int) {
    ShortTime(LENGTH_SHORT), LongTime(LENGTH_LONG)
}

fun Context.toast(text: String) = toast(text, time = ShortTime)

fun Context.toast(text: Int, time: CSToastTime = ShortTime) =
    toast(text = getString(text), time)

fun Context.toast(text: String, time: CSToastTime = ShortTime) {
    fun toast() = Toast.makeText(this, text, time.value).show()
    if (isThreadMain) toast() else mainHandler.send(::toast)
}
