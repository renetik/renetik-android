package renetik.android.core.java.io

import renetik.android.core.android.os.isThreadMain
import renetik.android.core.java.util.format
import renetik.android.core.java.util.now
import renetik.android.core.logging.CSLog.logDebug
import renetik.android.core.logging.CSLog.logError
import renetik.android.core.kotlin.logWarn
import java.io.File
import java.io.File.createTempFile
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.io.path.exists

fun File.items(): List<File> = listFiles()?.toList() ?: emptyList()
fun File.files(): List<File> = listFiles(File::isFile)?.toList() ?: emptyList()
fun File.dirs(): List<File> = listFiles(File::isDirectory)?.toList() ?: emptyList()

inline fun File.forEachFile(action: (File) -> Unit) {
    listFiles(File::isFile)?.forEach(action)
}

inline fun File.forEachDir(action: (File) -> Unit) {
    listFiles(File::isDirectory)?.forEach(action)
}

fun File.recreateFile() = apply {
    delete()
    createFileAndDirs()
}

fun File.createFileAndDirs() = apply {
    parentFile?.mkdirs()
    createNewFile()
}

fun File.createFileDirs() = apply {
    parentFile?.mkdirs()
}

fun File.write(text: String) = apply {
    createFileAndDirs()
    writeText(text)
}

fun File.writeAtomic(text: String) {
    createFileAndDirs()
    val tmp = File(parentFile, "$name.tmp")
    tmp.bufferedWriter().use { writer ->
        writer.write(text)
        writer.flush()
    }
    if (!tmp.renameTo(this)) {
        tmp.delete()
        logError("Could not rename ${tmp.path} to ${this.path}")
    }
}

fun File.recreateDirs() = apply {
    deleteRecursively()
    mkdirs()
}

fun File.createDirs() = apply {
    mkdirs()
}

fun File.createDatedFile(extension: String): File {
    mkdirs()
    return createTempFile(now.format("yyyy-MM-dd_HH-mm-ss"), ".$extension", this)
}

fun File.readString(): String? {
    if (isThreadMain)
        logDebug("load called on UI thread")
    return if (exists()) readText() else null
}

val File.isDirEmpty get() = list()?.isEmpty() ?: !exists()

fun File.moveTo(file: File, overwrite: Boolean = true) {
    copyTo(file, overwrite)
    delete()
}

val File.itemCount: Int get() = list()?.size ?: 0

fun File.with(name: String? = null, extension: String? = null): File {
    return File(parentFile, "${name ?: nameWithoutExtension}.${extension ?: this.extension}")
}

val File.isJson: Boolean get() = extension == "json"

fun File.fileList(depth: Int): List<File> {
    val files = mutableListOf<File>()
    fun recurse(currentDirectory: File, currentDepth: Int) {
        if (currentDepth > depth || !currentDirectory.isDirectory) return
        currentDirectory.listFiles()?.forEach { file ->
            if (file.isFile) files.add(file)
            else if (file.isDirectory) recurse(file, currentDepth + 1)
        }
    }
    recurse(this, 0)
    return files
}

fun File.deleteAll() = apply {
    if (exists()) if (isDirectory) deleteRecursively() else delete()
}

fun File.atomicMove(output: File): Boolean {
    val path = toPath()
    if (!path.exists()) return false
    runCatching {
        try {
            Files.move(path, output.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            return true
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(path, output.toPath(), REPLACE_EXISTING)
            return true
        }
    }.logWarn()
    return false
}

fun File.copy(output: File): Boolean {
    val path = toPath()
    if (!path.exists()) return false
    runCatching {
        Files.copy(path, output.toPath(), REPLACE_EXISTING)
        return true
    }.logWarn()
    return false
}

fun File.safeDelete() {
    runCatching { deleteRecursively() }
}
