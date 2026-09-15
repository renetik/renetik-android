package renetik.android.core.android.content

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import renetik.android.core.kotlin.logWarn

inline fun BroadcastReceiver(
    crossinline function: (context: Context, intent: Intent) -> Unit) =
    object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) =
            function(context, intent)
    }

inline fun Context.register(
    action: String, exported: Boolean = false, crossinline function: () -> Unit
): BroadcastReceiver = register(IntentFilter(action), exported) { _, _ -> function() }

fun Context.broadcastPendingIntent(actionId: String, flags: Int): PendingIntent =
    PendingIntent.getBroadcast(this, 0, Intent(actionId), flags)

inline fun Context.register(
    action: String, crossinline function: (Intent, BroadcastReceiver) -> Unit
): BroadcastReceiver = register(IntentFilter(action), function = function)

inline fun Context.register(
    intent: IntentFilter, exported: Boolean = false,
    crossinline function: (Intent, BroadcastReceiver) -> Unit
): BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = function(intent, this)
}.also { register(it, intent, exported) }

fun Context.register(intent: IntentFilter): Intent? = register(null, intent)

fun Context.register(
    receiver: BroadcastReceiver?, intent: IntentFilter,
    exported: Boolean = false): Intent? =
    ContextCompat.registerReceiver(this, receiver, intent,
        if (exported) RECEIVER_EXPORTED else RECEIVER_NOT_EXPORTED)

fun Context.unregister(receiver: BroadcastReceiver) {
    runCatching { unregisterReceiver(receiver) }.logWarn()
}
