package ireader.presentation.ui.video

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.navigator.currentOrThrow
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.StorageType
import com.anggrayudi.storage.file.getAbsolutePath
import ireader.core.source.model.MovieUrl
import ireader.core.source.model.Subtitle
import ireader.domain.utils.extensions.*
import ireader.i18n.UiText
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.video.bottomsheet.audioTracksComposable
import ireader.presentation.ui.video.bottomsheet.loadLocalFileComposable
import ireader.presentation.ui.video.bottomsheet.playBackSpeedComposable
import ireader.presentation.ui.video.bottomsheet.subtitleSelectorComposable
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.PlayerState
import ireader.presentation.ui.video.component.core.toSubtitleData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoPlayerBottomSheet(
    playerState: PlayerState,
    mediaState: MediaState,
    vm: VideoScreenViewModel,
    sheetState: ModalBottomSheetState
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        LazyColumn {
            item {
                PreferenceRow(title = "Open in external app", onClick = {

                    (vm.mediaState.currentLink ?: vm.mediaState.currentDownloadedFile)?.let {
                        val webIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        context.startActivity(webIntent)
                    }
                })
            }
            playBackSpeedComposable(playerState) {
                playerState.playbackSpeed = it
                vm.player?.value?.setPlaybackSpeed(it)
                scope.launch {
                   sheetState.hide()
                }
                vm.showSnackBar(UiText.DynamicString("Playback speed: $it."))
            }
            audioTracksComposable(playerState, mediaState) {
                mediaState?.setPreferredAudioTrack(it)
                scope.launch {
                    sheetState.hide()
                }
                vm.showSnackBar(UiText.DynamicString("Audio Tracks: $it."))
            }
            subtitleSelectorComposable(
                playerState,
                mediaState.activeSubtitles.value
            ) { subtitleData ->
                playerState.currentSubtitle = subtitleData

                mediaState.setPreferredSubtitles(subtitleData).let { result ->
                    if (result) {
                        mediaState.reloadPlayer()
                    }
                }
                scope.launch {
                    sheetState.hide()
                }
                vm.showSnackBar(UiText.DynamicString("Subtitle: ${subtitleData?.name}."))
            }
            loadLocalFileComposable("Load Video From Local Storage") {
                vm.simpleStorage.simpleStorageHelper.openFilePicker(300, false, FileFullPath(context, StorageType.EXTERNAL), filterMimeTypes = arrayOf("video/*"))
                vm.simpleStorage.simpleStorageHelper.onFileSelected = { requestCode, files ->
                    globalScope.launchIO {
                        val firstFile = files.first().getAbsolutePath(context)
                        vm.chapter =
                            vm.chapter?.copy(content = listOf(MovieUrl(firstFile.toString())))
                        vm.insertUseCases.insertChapter(vm.chapter)
                        vm.mediaState.subs = emptyList()
                        vm.mediaState.medias = emptyList()
                        vm.mediaState.medias = listOf(MovieUrl(firstFile))
                        withUIContext {
                            mediaState.currentLink = null
                            mediaState.currentDownloadedFile = firstFile
                            mediaState.saveData()
                            mediaState.reloadPlayer()
                        }
                        vm.showSnackBar(UiText.DynamicString("File Selected."))
                        scope.launch {
                           sheetState.hide()
                        }
                    }
                }
            }
            loadLocalFileComposable("Load Subtitle from Local Storage") {
                vm.simpleStorage.simpleStorageHelper.openFilePicker(300, false, filterMimeTypes = arrayOf("application/*")
                    )
                vm.simpleStorage.simpleStorageHelper.onFileSelected = { requestCode, files ->
                    globalScope.launchIO {
                        val file = files.first()
                        val path = file.getAbsolutePath(context)
                        val sub = Subtitle(path).toSubtitleData()
                        val subs = mediaState.activeSubtitles.value + listOf(sub)
                        withUIContext {
                            mediaState.saveData()
                            mediaState.setActiveSubtitles(subs)
                            mediaState.reloadPlayer()
                        }
                        vm.showSnackBar(UiText.DynamicString("Subtitle is Selected."))
                        scope.launch {
                            sheetState.hide()
                        }
                    }
                }
            }
        }
    }
}