package renetik.android.ui

import android.content.Context
import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import kotlinx.coroutines.suspendCancellableCoroutine
import renetik.android.ui.protocol.CSViewInterface
import kotlin.coroutines.resume

fun Context.colorStateList(resource: Int): ColorStateList =
    AppCompatResources.getColorStateList(this, resource)!!

val Context.inflaterContext
    get() = CSViewInterface.themeOverride?.let { ContextThemeWrapper(this, it) } ?: this

@Suppress("UNCHECKED_CAST")
fun <ViewType : View> Context.inflate(layoutId: Int, group: ViewGroup? = null): ViewType =
    LayoutInflater.from(inflaterContext).inflate(layoutId, group, false) as ViewType

@Suppress("UNCHECKED_CAST")
fun <ViewType : View> Context.inflate(
    layoutId: Int, group: ViewGroup? = null, onViewReady: (ViewType) -> Unit
) = AsyncLayoutInflater(inflaterContext)
    .inflate(layoutId, group) { view, _, _ -> onViewReady(view as ViewType) }

suspend fun <ViewType : View> Context.inflateAsync(
    layoutId: Int, group: ViewGroup? = null
): ViewType = suspendCancellableCoroutine { continuation ->
    inflate<ViewType>(layoutId, group, onViewReady = { view ->
        if (!continuation.isActive) return@inflate
        continuation.resume(view)
    })
}