package renetik.android.controller.base

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import renetik.android.core.android.content.startActivityIfAvailable

fun Context.openUri(uri: String, appPackage: String? = null): Boolean =
    Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appPackage?.let(::setPackage)
    }.let(::startActivityIfAvailable)
