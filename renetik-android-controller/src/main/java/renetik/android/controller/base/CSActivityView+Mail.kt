package renetik.android.controller.base

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_SEND_MULTIPLE
import android.content.Intent.EXTRA_BCC
import android.content.Intent.EXTRA_CC
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.createChooser
import android.net.Uri
import android.os.Environment.getExternalStorageDirectory
import androidx.core.net.toUri
import renetik.android.controller.base.CSActivityView
import renetik.android.core.kotlin.logError
import java.io.File

fun <T : CSActivityView<*>> T.sendMail(email: String, subject: String, text: String) {
    sendMail(listOf(email), subject, text, listOf())
}

fun <T : CSActivityView<*>> T.sendMail(
    emails: List<String>, subject: String, body: CharSequence, attachment: File) {
    sendMail(emails, subject, body, listOf(attachment))
}

fun <T : CSActivityView<*>> T.sendMail(
    emails: List<String>, subject: String, body: CharSequence,
    attachments: List<File>
) {
    Intent(if (attachments.isEmpty()) ACTION_SEND else ACTION_SEND_MULTIPLE).apply {
        putExtra(EXTRA_EMAIL, emails.toTypedArray()).putExtra(EXTRA_SUBJECT, subject)
        putExtra(EXTRA_TEXT, body).type = "text/plain"
        val attachmentUris = ArrayList<Uri>()
        attachments.forEach { file ->
            if (!file.startsWith(getExternalStorageDirectory()))
                throw Exception("Attachment not in ExternalStorageDirectory")
            else if (!(file.exists() && file.canRead())) throw Exception("Attachment can not be read")
            attachmentUris.add(Uri.fromFile(file))
        }
        if (attachments.isNotEmpty()) putParcelableArrayListExtra(EXTRA_STREAM, attachmentUris)
        runCatching {
            startActivity(createChooser(this, "Pick an Email provider"))
        }.logError()
    }
}

fun CSActivityView<*>.sendMail(
    picker: String, to: String, subject: String? = null, body: String? = null
) = sendMail(
    picker, arrayOf(to), subject = subject, body = body
)

fun CSActivityView<*>.sendMail(
    pickerTitle: String,
    to: Array<String> = emptyArray(), cc: Array<String> = emptyArray(),
    bcc: Array<String> = emptyArray(), subject: String? = null, body: String? = null
) {
    val toPart = if (to.isNotEmpty()) to.joinToString(",") else ""
    val uriBuilder = StringBuilder("mailto:$toPart")

    val query = mutableListOf<String>()
    if (cc.isNotEmpty()) query += "cc=${Uri.encode(cc.joinToString(","))}"
    if (bcc.isNotEmpty()) query += "bcc=${Uri.encode(bcc.joinToString(","))}"
    subject?.let { query += "subject=${Uri.encode(it)}" }
    body?.let { query += "body=${Uri.encode(it)}" }

    if (query.isNotEmpty()) uriBuilder.append("?").append(query.joinToString("&"))

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = uriBuilder.toString().toUri()
        subject?.let { putExtra(EXTRA_SUBJECT, it) }
        body?.let { putExtra(EXTRA_TEXT, it) }
        if (to.isNotEmpty()) putExtra(EXTRA_EMAIL, to)
        if (cc.isNotEmpty()) putExtra(EXTRA_CC, cc)
        if (bcc.isNotEmpty()) putExtra(EXTRA_BCC, bcc)
    }
    runCatching {
        startActivity(createChooser(intent, pickerTitle))
    }.logError()
}
