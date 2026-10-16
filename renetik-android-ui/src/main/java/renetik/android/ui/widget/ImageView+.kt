package renetik.android.ui.widget

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import renetik.android.event.change.CSHasChangeValue
import renetik.android.event.change.action
import renetik.android.event.registration.CSRegistration
import renetik.android.ui.graphics.CSImaging
import java.io.File

fun <T : ImageView> T.image(@DrawableRes resourceId: Int?): T = apply {
    CSImaging.instance.load(this, resourceId)
}

fun <T : ImageView> T.drawable(drawable: Drawable?): T = apply {
    CSImaging.instance.load(this, drawable)
}

fun <T : ImageView> T.image(file: File) = apply {
    CSImaging.instance.load(this, file)
}

fun <T : ImageView> T.image(bitmap: Bitmap?) = apply {
    CSImaging.instance.load(this, bitmap)
}

fun <T : ImageView> T.image(uri: Uri?) = apply {
    CSImaging.instance.load(this, uri)
}

@JvmName("imagePropertyResource")
fun <T> ImageView.image(property: CSHasChangeValue<T>, resource: (T) -> Int?)
        : CSRegistration = property.action { image(resource(property.value)) }

@JvmName("imagePropertyBitmap")
fun <T> ImageView.image(property: CSHasChangeValue<T>, bitmap: (T) -> Bitmap?)
        : CSRegistration = property.action { image(bitmap(property.value)) }

@JvmName("imagePropertyFile")
fun <T> ImageView.image(property: CSHasChangeValue<T>, file: (T) -> File)
        : CSRegistration = property.action { image(file(property.value)) }

fun <T : ImageView> T.iconTint(@ColorInt color: Int?) = apply {
    imageTintList = color?.let { ColorStateList.valueOf(it) }
}