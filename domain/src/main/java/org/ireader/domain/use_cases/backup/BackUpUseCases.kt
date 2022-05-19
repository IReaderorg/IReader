package org.ireader.domain.use_cases.backup

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import org.ireader.common_extensions.findComponentActivity
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class BackUpUseCases @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    fun makeBackup(
        context: Context,
        resultIntent: ActivityResult,
        text:String,
        onError:(Throwable) -> Unit
    ) {
        try {
            val contentResolver = context.findComponentActivity()!!.contentResolver
            val uri = resultIntent.data!!.data!!
            val pfd = contentResolver.openFileDescriptor(uri, "w")
            pfd?.use {
                FileOutputStream(pfd.fileDescriptor).use { outputStream ->
                    outputStream.write(text.toByteArray())
                }
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }

    fun restoreBackup(
        context:Context,
        resultIntent: Intent,
        onSuccess:(String) -> Unit,
        onError:(Throwable) -> Unit
    ) {
        try {
            val contentResolver = context.findComponentActivity()!!.contentResolver
            val uri = resultIntent.data!!
            contentResolver
                .takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            val pfd = contentResolver.openFileDescriptor(uri, "r")
            pfd?.use {
                FileInputStream(pfd.fileDescriptor).use { stream ->
                    val txt = stream.readBytes().decodeToString()
                    kotlin.runCatching {
                        onSuccess(txt)
                    }.getOrElse { e ->
                        onError(e)
                    }
                }
            }

        } catch (e: Throwable) {
            onError(e)
        }
    }

}
