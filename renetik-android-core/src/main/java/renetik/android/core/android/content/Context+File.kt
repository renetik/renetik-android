package renetik.android.core.android.content

import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import java.io.File
import java.util.UUID

fun Context.temporaryFile(extension: String? = null): File =
    File.createTempFile("temp", extension?.let { ".$it" }, cacheDir)

fun Context.temporaryFolder(): File {
    val uniqueName = "temp_folder_${UUID.randomUUID()}"
    val tempFolder = File(cacheDir, uniqueName)
    tempFolder.deleteRecursively()
    tempFolder.mkdirs()
    return tempFolder
}

val Context.externalFilesDir: File
    get() = getExternalFilesDir(null) ?: getExternalStorageDirectory()
