package renetik.android.core.android.content

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.appcompat.app.AppCompatActivity
import kotlin.reflect.KClass

inline fun <reified Activity : AppCompatActivity> Context.startNewActivity(
    extras: Map<String, String> = emptyMap(), options: Bundle? = null
) = startNewActivity(Activity::class, extras, options)

fun <Activity : AppCompatActivity> Context.startNewActivity(
    activityClass: KClass<out Activity>,
    extras: Map<String, String> = emptyMap(), options: Bundle? = null
) {
    val intent = Intent(this, activityClass)
    for ((key, value) in extras) intent.putExtra(key, value)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent, options)
}

inline fun <reified Activity : AppCompatActivity> Context.startActivity(
    extras: Map<String, String> = emptyMap(), options: Bundle? = null
) = startActivity(Activity::class, extras, options)

fun <Activity : AppCompatActivity> Context.startActivity(
    activityClass: KClass<out Activity>,
    extras: Map<String, String> = emptyMap(), options: Bundle? = null
) {
    val intent = Intent(this, activityClass)
    for ((key, value) in extras) intent.putExtra(key, value)
    startActivity(intent, options)
}

fun Context.openApplicationSettings() {
    val intent = android.content.Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    intent.data = Uri.fromParts("package", packageName, null)
    startActivity(intent)
}

/**
 * Starts [intent] without a PackageManager preflight because Android package visibility can hide
 * valid handlers from `resolveActivity()`. Returns false when the activity is missing or its
 * launch is denied.
 */
fun Context.startActivityIfAvailable(
    intent: Intent, onActivityNotFound: ((ActivityNotFoundException) -> Unit)? = null
): Boolean = try {
    startActivity(intent)
    true
} catch (exception: ActivityNotFoundException) {
    onActivityNotFound?.invoke(exception)
    false
} catch (_: SecurityException) {
    false
}
