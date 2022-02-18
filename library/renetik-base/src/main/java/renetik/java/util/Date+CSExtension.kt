package renetik.java.util

import renetik.android.framework.common.catchError
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

val currentTime get() = Date().time
fun dateFromString(format: String, string: String) = catchError<ParseException> {
    SimpleDateFormat(format, Locale.US).parse("" + string)
}

fun Date.format(dateStyle: Int, timeStyle: Int): String =
    DateFormat.getDateTimeInstance(dateStyle, timeStyle).format(this)

fun Date.formatDate(style: Int): String = DateFormat.getDateInstance(style).format(this)
fun Date.formatTime(style: Int): String = DateFormat.getTimeInstance(style).format(this)
fun Date.format(format: String): String = SimpleDateFormat(format, Locale.US).format(this)
fun Date.addYears(value: Int): Date {
    val instance = Calendar.getInstance()
    instance.time = this
    instance.add(Calendar.YEAR, value)
    return instance.time
}
fun Date.addHours(value: Int): Date {
    val instance = Calendar.getInstance()
    instance.time = this
    instance.add(Calendar.HOUR, value)
    return instance.time
}


fun Date.createDatedDirName() = format("yyyy-MM-dd_HH-mm-ss")

fun Date.formatToISO8601(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("CET")
    return dateFormat.format(this)
}