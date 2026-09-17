@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.core.kotlin.primitives

import renetik.android.core.kotlin.text.StringBuilder
import renetik.android.core.kotlin.text.add
import renetik.android.core.kotlin.text.deleteLast
import renetik.android.core.kotlin.text.reload
import renetik.android.core.lang.CSStringConstants
import renetik.android.core.lang.catchWarnReturn
import renetik.android.core.kotlin.logWarn
import java.text.Normalizer
import java.util.Locale
import java.util.Random

inline val String.Companion.alphabet get() = CSStringConstants.alphabet
inline val String.Companion.NewLine get() = CSStringConstants.NewLine
inline val String.Companion.Comma get() = CSStringConstants.Comma
inline val String.Companion.Semicolon get() = CSStringConstants.Semicolon
inline val String.Companion.Empty get() = CSStringConstants.Empty
inline val String.Companion.Space get() = CSStringConstants.Space
inline val String.Companion.unsafeFileChars: String get() = CSStringConstants.UnsafeFileChars

fun String.prefix(prefix: String): String = prefix + this

fun String.plusIf(condition: Boolean, function: () -> String): String =
    if (condition) this + function() else this

inline fun String.Companion.formatted(
    format: String, vararg args: Any?
): String = String.format(Locale.getDefault(), format, *args)

fun String.Companion.random(length: Int): String {
    val random = Random()
    val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val text = CharArray(length)
    for (i in 0 until length)
        text[i] = letters[random.nextInt(letters.length)]
    return String(text)
}

fun String.Companion.contains(
    string1: String?, string2: String?, ignoreCase: Boolean = false
) =
    if (string1 == null || string2 == null) false
    else string1.contains(string2, ignoreCase)

fun String.Companion.range(start: Int, endInclusive: Int): List<String> =
    IntRange(start, endInclusive).map { "$it" }

inline val String?.isBlank get() = this?.isBlank() ?: true
inline val String?.isSet get() = !this.isBlank
inline val String?.setOrNull get() = if (isSet) this else null

inline fun String.ifSet(function: (String) -> Unit) = apply {
    if (this.isSet) function(this)
}

infix fun String.ends(suffix: String) = endsWith(suffix)


fun String.asLong() = runCatching<Long> { toLong() }.logWarn().getOrNull()
fun String.asFloat() = runCatching<Float> { toFloat() }.logWarn().getOrNull()
fun String.asDouble() = runCatching<Double> { toDouble() }.logWarn().getOrNull()
fun String.asInt() = runCatching<Int> { toInt() }.logWarn().getOrNull()
fun String.asDouble(default: Double) =
    catchWarnReturn<Double, NumberFormatException>(default) { toDouble() }

fun String.asLong(default: Long) =
    catchWarnReturn<Long, NumberFormatException>(default) { toLong() }

fun String.asFloat(default: Float) =
    catchWarnReturn<Float, NumberFormatException>(default) { toFloat() }

fun String.asInt(default: Int) =
    catchWarnReturn<Int, NumberFormatException>(default) { toInt() }

fun String.trimNewLines() = replace(String.NewLine, String.Empty)

fun String.remove(toRemove: String) = replace(toRemove, "")

fun String.replace(strings: Iterable<String>, replacement: String): String {
    val builder = StringBuilder(this)
    strings.forEach { builder.reload(builder.toString().replace(it, replacement)) }
    return builder.toString()
}

fun String.replace(strings: Array<String>, replacement: String) =
    replace(strings.asIterable(), replacement)

fun String.leaveEndOfLength(length: Int): String {
    if (this.length > length) {
        val toCut = this.length - length
        return substring(toCut)
    }
    return this
}

fun String.separateToString(vararg items: Any?): String {
    val text = StringBuilder()
    for (value in items) value?.let { text.add(it).add(this) }
    if (!text.isEmpty) text.deleteLast(this.length)
    return text.toString()
}

val String.lowerCased: String get() = lowercase()
val String.upperCased: String get() = uppercase()
val String.noAccents: String get() = removeAccents()
val String.noNewLines: String get() = replace("/n", " ")

private val nonSpacingCharactersRegex = "\\p{Mn}+".toRegex()
// some used this in remove accents code instead
// private val combiningdiacriticalMarksRegex = "\\p{Mn}+".toRegex()

fun String.removeAccents(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(nonSpacingCharactersRegex, "")
}

fun CharSequence.containsAll(words: List<String>, ignoreCase: Boolean = false): Boolean =
    words.all { contains(it, ignoreCase) }

fun CharSequence.containsAll(vararg words: String, ignoreCase: Boolean = false): Boolean =
    words.all { contains(it, ignoreCase) }

fun CharSequence.containsAny(words: List<String>, ignoreCase: Boolean = false): Boolean =
    words.any { contains(it, ignoreCase) }

fun CharSequence.containsAny(vararg words: String, ignoreCase: Boolean = false): Boolean =
    words.any { contains(it, ignoreCase) }

fun String.vertical(): String = fold("") { acc, char -> "$acc$char\n" }.dropLast(1)
fun String.noBreakSpace(): String = replace(' ', '\u00A0')

fun Pair<String?, String?>.joinToString(
    separator: CharSequence = ", ", prefix: CharSequence = "",
    postfix: CharSequence = "", limit: Int = -1,
    truncated: CharSequence = "...", transform: ((String?) -> CharSequence)? = null
) = toList().filterNotNull()
    .joinToString(separator, prefix, postfix, limit, truncated, transform)

fun String.splitInTwo(): Pair<String, String> {
    val mid = (this.length + 1) / 2
    return this.substring(0, mid) to this.substring(mid)
}

fun String.splitInTwo(separator: String): Pair<String, String?> {
    val index = indexOf(separator)
    return if (index == -1) this to null
    else substring(0, index) to substring(index + separator.length)
}

private val ILLEGAL_FILENAME_CHARS = Regex("[\\x00-\\x1F\\\\/:*?\"<>|]")
fun String.sanitizeForFile(default: String, max: Int = 200): String = this
    .replace(ILLEGAL_FILENAME_CHARS, " ").replace(Regex("\\s+"), " ").trim()
    .take(max).trimEnd { c -> c == '.' || c == ' ' }.ifBlank { default }

val String.titleCased: String
    get() = if (isEmpty()) this else split(" ")
        .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }

val String.sentenceCased: String
    get() = if (isEmpty()) this else lowerCased.replaceFirstChar(Char::uppercase)
