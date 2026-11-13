package renetik.android.core.android.content

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.ContextWrapper.ACTIVITY_SERVICE
import android.content.ContextWrapper.MIDI_SERVICE
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.media.AudioManager
import android.media.midi.MidiManager
import android.os.PowerManager
import android.view.inputmethod.InputMethodManager
import androidx.core.location.LocationManagerCompat
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.logging.CSLog.logError
import renetik.android.core.logging.CSLog.logInfo
import java.lang.Integer.MAX_VALUE
import kotlin.reflect.KClass

inline fun <reified T> Context.service(name: String) = getSystemService(name) as T
val Context.inputService get():InputMethodManager = service(INPUT_METHOD_SERVICE)
val Context.notifications get():NotificationManager = service(NOTIFICATION_SERVICE)
val Context.audioManager get():AudioManager = service(AUDIO_SERVICE)

val Context.locationManager get() = getSystemService(Context.LOCATION_SERVICE) as LocationManager
val Context.isLocationEnabled: Boolean
    get() = LocationManagerCompat.isLocationEnabled(locationManager)

val Context.midiManager: MidiManager? get() = getSystemService(MIDI_SERVICE) as? MidiManager
val Context.isMidiSupported: Boolean get() = midiManager != null
fun <T> Context.ifHasMidi(function: (MidiManager) -> T): T? = midiManager?.let { function(it) }

@SuppressLint("WakelockTimeout")
fun Context.wakeLock(levelAndFlags: Int): PowerManager.WakeLock {
    val lock = (getSystemService(POWER_SERVICE) as PowerManager)
        .newWakeLock(levelAndFlags, "wakeLock:${debugClassName}")
    lock.acquire()
    if (lock.isHeld) logInfo { "Wake Lock held: $this" }
    else logError { "Wake Lock not held: $this" }
    return lock
}

fun Context.startService(serviceClass: KClass<out Service>) =
    startService(Intent(this, serviceClass))

inline fun <reified T : Service> Context.startService() =
    startService(Intent(this, T::class.java))

inline fun <reified T : Service> Context.startForegroundService() =
    startForegroundService(Intent(this, T::class.java))

fun Context.stopService(serviceClass: KClass<out Service>) =
    stopService(Intent(this, serviceClass))

inline fun <reified T : Service> Context.stopService() =
    stopService(Intent(this, T::class.java))

fun Context.unbind(connection: ServiceConnection) {
    runCatching { unbindService(connection) }
}

@Suppress("DEPRECATION")
fun Context.isServiceRunning(serviceClass: Class<out Service>): Boolean =
    (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getRunningServices(MAX_VALUE)
        .any { serviceClass.name == it.service.className }
