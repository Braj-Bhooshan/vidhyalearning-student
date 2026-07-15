package com.studentprofile.app.presentation.screens.video

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.studentprofile.app.presentation.viewmodel.VideoPlayerViewModel

private const val WATERMARK_VIEW_TAG = "wm_overlay"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    videoUrl: String,
    videoTitle: String,
    onBack: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val activity = context as? Activity

    var isFullscreen by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Sync isFullscreen with device orientation changes
    LaunchedEffect(isLandscape) {
        isFullscreen = isLandscape
    }

    // Load watermark for this specific video
    LaunchedEffect(videoId) {
        viewModel.loadWatermark(videoId)
    }

    // FLAG_SECURE prevents screen recording and screenshots
    LaunchedEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            // Restore default orientation + system bars when leaving the screen
            val act = activity ?: return@onDispose
            val win = act.window ?: return@onDispose
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            WindowCompat.getInsetsController(win, view).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Fullscreen effect: hide/show system bars
    LaunchedEffect(isFullscreen) {
        val act = activity ?: return@LaunchedEffect
        val win = act.window ?: return@LaunchedEffect
        val ic = WindowCompat.getInsetsController(win, view)
        if (isFullscreen) {
            ic.hide(WindowInsetsCompat.Type.systemBars())
            ic.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            ic.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Handles both fullscreen button tap and BackHandler
    val applyFullscreen: (Boolean) -> Unit = applyFullscreen@{ entering ->
        isFullscreen = entering
        val act = activity ?: return@applyFullscreen
        if (entering) {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler(enabled = isFullscreen) { applyFullscreen(false) }

    val resolvedUrl = remember(videoUrl) { viewModel.resolveVideoUrl(videoUrl) }

    val exoPlayer = remember(resolvedUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(resolvedUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // Single Column keeps VideoPlayerView at the same composition slot whether portrait
    // or fullscreen — the AndroidView (PlayerView) is never recreated, so ExoPlayer's
    // internal fullscreen-button state is preserved and the toggle works correctly.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!isFullscreen) {
            TopAppBar(
                title = {
                    Text(
                        text = videoTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        exoPlayer.pause()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            VideoPlayerView(
                exoPlayer = exoPlayer,
                viewModel = viewModel,
                onFullscreenChange = applyFullscreen,
                modifier = if (isFullscreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .align(Alignment.Center)
                }
            )
        }
    }
}

/**
 * Wraps ExoPlayer's PlayerView and mounts the watermark as a ComposeView child inside
 * PlayerView's own view hierarchy so it renders above the SurfaceView surface.
 *
 * Composables placed as siblings of AndroidView in the outer Box are overdrawn by child
 * Views (Android draws parent canvas first, then child Views on top), so the watermark
 * must live inside PlayerView, not outside it.
 */
@Composable
private fun VideoPlayerView(
    exoPlayer: ExoPlayer,
    viewModel: VideoPlayerViewModel,
    onFullscreenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).also { playerView ->
                playerView.player = exoPlayer
                playerView.useController = true
                playerView.setShowNextButton(false)
                playerView.setShowPreviousButton(false)
                playerView.setShowShuffleButton(false)

                // Watermark as a child View inside PlayerView — renders above SurfaceView.
                // SurfaceView punches a hole behind the window; Views added as siblings
                // inside the same FrameLayout draw in the window surface above that hole.
                val wmView = ComposeView(ctx).apply {
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                    )
                    tag = WATERMARK_VIEW_TAG
                    isClickable = false
                    isFocusable = false
                    setContent {
                        val cfg by viewModel.watermark.collectAsState()
                        val wmText = cfg?.text
                        if (cfg != null && !wmText.isNullOrBlank()) {
                            WatermarkOverlay(
                                text = wmText,
                                opacity = cfg!!.opacity,
                                color = cfg!!.textColor,
                                fontSize = cfg!!.fontSize,
                                isMoving = !cfg!!.position.equals("static", ignoreCase = true),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                playerView.addView(
                    wmView,
                    android.widget.FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                )
            }
        },
        update = { playerView ->
            // Re-set listener on each recomposition so the closure never goes stale
            playerView.setFullscreenButtonClickListener { fs ->
                onFullscreenChange(fs)
            }
        },
        modifier = modifier
    )
}
