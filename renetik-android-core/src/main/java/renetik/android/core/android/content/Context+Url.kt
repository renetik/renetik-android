package renetik.android.core.android.content

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import renetik.android.core.logging.CSLog.logWarn

fun Context.startActivityForUri(
    uri: Uri, onActivityNotFound: ((ActivityNotFoundException) -> Unit)? = null
) = startActivityForUriAndType(uri, null, onActivityNotFound)

fun Context.startActivityForUriAndType(
    uri: Uri, type: String? = null,
    onActivityNotFound: ((ActivityNotFoundException) -> Unit)? = null
) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, type)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    intent.grantUriPermissions(uri)
    startActivityIfAvailable(intent) { exception ->
        logWarn(exception)
        onActivityNotFound?.invoke(exception)
    }
}

fun Context.openUrl(url: String, errorMessage: String? = null) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (!startActivityIfAvailable(intent)) {
        errorMessage?.also(::toast)
        logWarn { "No application can open url: $url" }
    }
}
