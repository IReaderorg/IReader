package ir.kazemcodes.infinity.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.domain.repository.Repository

private const val DOWNLOADER_ID: String = "69"

@HiltWorker
class DownloadService @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
) : CoroutineWorker(context, params) {

    private val notificationId = 69420

    override suspend fun doWork(): Result {
        val bookName = ""
        val book = repository.localBookRepository.getBookByName(bookName)
        val chapters = repository.localChapterRepository.getChapter(bookName)

        createChannel(
            applicationContext, Channel(
                name = applicationContext.getString(R.string.downloader_name),
                id = DOWNLOADER_ID
            )
        )


        return Result.success()
    }

}
