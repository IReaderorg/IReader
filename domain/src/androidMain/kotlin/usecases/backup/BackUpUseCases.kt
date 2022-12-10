package ireader.domain.usecases.backup

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import ireader.domain.data.repository.DownloadRepository
import ireader.domain.utils.extensions.findComponentActivity
import org.koin.core.annotation.Factory
import java.io.FileInputStream
import java.io.FileOutputStream

@Factory
class BackUpUseCases(private val downloadRepository: DownloadRepository) {
    fun makeBackup(
        context: Context,
        resultIntent: ActivityResult,
        text: String,
        onError: (Throwable) -> Unit
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
        context: Context,
        resultIntent: Intent,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
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
