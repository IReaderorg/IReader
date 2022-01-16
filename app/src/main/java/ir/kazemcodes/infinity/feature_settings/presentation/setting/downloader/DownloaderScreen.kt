package ir.kazemcodes.infinity.feature_settings.presentation.setting.downloader

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.work.WorkManager
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.core.data.network.utils.toast
import ir.kazemcodes.infinity.core.presentation.reusable_composable.NotImplementedText
import ir.kazemcodes.infinity.feature_activity.domain.service.DownloadService

@Composable
fun DownloaderScreen() {
    val backStack = LocalBackstack.current
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "Downloads",
                    color = MaterialTheme.colors.onBackground,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
            },
            backgroundColor = MaterialTheme.colors.background,
            actions = {
                IconButton(
                    onClick = {
                        WorkManager.getInstance(context)
                            .cancelUniqueWork(DownloadService.DOWNLOAD_SERVICE_NAME)
                        context.toast("Downloads were Stopped Successfully")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownloadOff,
                        contentDescription = "Stop Download Icon",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { backStack.goBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "ArrowBack Icon",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }

            }
        )
    }) {
        NotImplementedText()

    }

}