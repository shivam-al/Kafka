package com.kafka.user.home

import android.os.Build
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kafka.data.prefs.Theme
import com.sarahang.playback.ui.audio.AudioActionHost
import com.sarahang.playback.ui.audio.PlaybackHost
import com.sarahang.playback.ui.color.ColorExtractor
import com.sarahang.playback.ui.color.LocalColorExtractor
import kotlinx.coroutines.flow.collectLatest
import org.kafka.base.debug
import org.kafka.common.widgets.LocalSnackbarHostState
import org.kafka.navigation.NavigatorHost
import org.kafka.ui.components.snackbar.SnackbarMessagesHost
import tm.alashow.datmusic.ui.downloader.DownloaderHost
import ui.common.theme.theme.LocalTheme

@Composable
fun MainScreen(
    navController: NavHostController,
    bottomSheetNavigator: BottomSheetNavigator,
    colorExtractor: ColorExtractor,
    theme: Theme,
) {
    val mainViewModel = hiltViewModel<MainViewModel>()
    val context = LocalContext.current

    ForceUpdateDialog(
        show = mainViewModel.isUpdateRequired,
        update = { mainViewModel.updateApp(context) })

    LaunchedEffect(mainViewModel, navController) {
        navController.currentBackStackEntryFlow.collectLatest { entry ->
            mainViewModel.logScreenView(entry)
        }
    }

    RequestNotificationPermission()

    CompositionLocalProvider(LocalColorExtractor provides colorExtractor) {
        CompositionLocalProvider(LocalTheme provides theme) {
            CompositionHosts {
                ModalBottomSheetLayout(
                    bottomSheetNavigator = bottomSheetNavigator,
                    sheetShape = MaterialTheme.shapes.large.copy(
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp),
                    ),
                    sheetBackgroundColor = MaterialTheme.colorScheme.surface,
                    sheetContentColor = MaterialTheme.colorScheme.onSurface,
                    scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                ) {
                    Home(
                        navController = navController,
                        analytics = mainViewModel.analytics,
                        modifier = Modifier.semantics { testTagsAsResourceId = true },
                        playerTheme = mainViewModel.playerTheme,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompositionHosts(content: @Composable () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        NavigatorHost {
            DownloaderHost {
                PlaybackHost {
                    AudioActionHost {
                        SnackbarMessagesHost()
                        content()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RequestNotificationPermission() {
    debug { "RequestNotificationPermission" }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )

        LaunchedEffect(permissionState) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}
