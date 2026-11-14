package renetik.android.core.android.content

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Spanned
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import renetik.android.core.android.text.parseFormatted
import renetik.android.core.lang.catchAllWarn
import renetik.android.core.lang.catchErrorReturnNull
import renetik.android.core.lang.tryAndFinally
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

fun Context.formatted(@StringRes resId: Int): Spanned = getString(resId).parseFormatted()

fun Context.resourceBytes(id: Int) = catchAllWarn {
    val stream = resources.openRawResource(id)
    val outputStream = ByteArrayOutputStream()
    val buffer = ByteArray(4 * 1024)
    tryAndFinally({
        var read: Int
        do {
            read = stream.read(buffer, 0, buffer.size)
            if (read == -1) break
            outputStream.write(buffer, 0, read)
        } while (true)
        outputStream.toByteArray()
    }) { stream.close() }
}

fun Context.openInputStream(uri: Uri): InputStream? =
    catchErrorReturnNull<FileNotFoundException, InputStream> {
        return contentResolver.openInputStream(uri)
    }

fun Context.string(@StringRes resId: Int): String = resources.getString(resId)
fun Context.strings(id: Int) = resources.getStringArray(id).toList()
fun Context.ints(id: Int): List<Int> = resources.getIntArray(id).toList()

fun Context.assetsReadText(path: String) =
    assets.open(path).bufferedReader().use { it.readText() }

fun Context.drawable(@DrawableRes resource: Int) = getDrawable(this, resource)!!.apply {
    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
}

fun Drawable.createClear(): ColorDrawable = bounds.createClearDrawable()

fun Rect.createClearDrawable(): ColorDrawable = ColorDrawable().also { it.bounds = this }
