package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoPlayerView(
    videoUri: String,
    isPlaying: Boolean,
    zoomScale: Float,
    modifier: Modifier = Modifier,
    onPlaybackError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Create and remember MediaPlayer
    val mediaPlayer = remember {
        MediaPlayer().apply {
            isLooping = true
            setVolume(0f, 0f) // Fake cameras are usually silent, mute by default
        }
    }

    // Effect to handle video source change
    LaunchedEffect(videoUri) {
        try {
            isBuffering = true
            errorMessage = null
            mediaPlayer.reset()
            
            // Check if it is a local content URI or an online URL
            if (videoUri.startsWith("content://") || videoUri.startsWith("file://")) {
                mediaPlayer.setDataSource(context, Uri.parse(videoUri))
            } else {
                // Online stream
                mediaPlayer.setDataSource(videoUri)
            }
            
            mediaPlayer.setOnPreparedListener { mp ->
                isBuffering = false
                if (isPlaying) {
                    mp.start()
                }
            }
            
            mediaPlayer.setOnErrorListener { _, what, extra ->
                isBuffering = false
                val errorMsg = "Media error: $what, $extra"
                errorMessage = "Không thể phát video này. Hãy thử video khác."
                onPlaybackError(errorMsg)
                true
            }

            mediaPlayer.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    isBuffering = true
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    isBuffering = false
                }
                true
            }
            
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            isBuffering = false
            errorMessage = "Đường dẫn video không khả dụng."
            onPlaybackError(e.localizedMessage ?: "Unknown preparation error")
        }
    }

    // Effect to handle play/pause changes
    LaunchedEffect(isPlaying) {
        try {
            if (mediaPlayer.isPlaying != isPlaying) {
                if (isPlaying) {
                    mediaPlayer.start()
                } else {
                    mediaPlayer.pause()
                }
            }
        } catch (e: Exception) {
            // Safe catch
        }
    }

    // Dispose player on leave
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (e: Exception) {
                // Safe catch
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (errorMessage == null) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(
                                surfaceTexture: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                val surface = Surface(surfaceTexture)
                                try {
                                    mediaPlayer.setSurface(surface)
                                } catch (e: Exception) {
                                    // Safe catch
                                }
                            }

                            override fun onSurfaceTextureSizeChanged(
                                surface: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {}

                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                try {
                                    mediaPlayer.setSurface(null)
                                } catch (e: Exception) {
                                    // Safe
                                }
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                        }
                    }
                },
                update = { textureView ->
                    // Handle dynamic digital zoom inside the viewport using scaleX and scaleY!
                    textureView.scaleX = zoomScale
                    textureView.scaleY = zoomScale
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Show buffering indicator
        if (isBuffering && errorMessage == null) {
            CircularProgressIndicator(
                color = Color(0xFF39FF14), // Cyber Green indicator
                strokeWidth = 3.dp
            )
        }

        // Show error placeholder if loading fails
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE61A1A1A))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
