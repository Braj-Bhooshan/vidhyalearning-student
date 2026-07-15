package com.studentprofile.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentprofile.app.data.local.TenantProvider
import com.studentprofile.app.data.remote.VideoApi
import com.studentprofile.app.domain.models.WatermarkConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val videoApi: VideoApi,
    private val tenantProvider: TenantProvider
) : ViewModel() {

    private val _watermark = MutableStateFlow<WatermarkConfig?>(null)
    val watermark: StateFlow<WatermarkConfig?> = _watermark.asStateFlow()

    // API returns a list; prefer the default config, fall back to the first one
    fun loadWatermark(videoId: String) {
        viewModelScope.launch {
            _watermark.value = try {
                val list = videoApi.getVideoWatermark(videoId)
                list.firstOrNull { it.isDefault } ?: list.firstOrNull()
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Resolves server-relative paths (e.g. /uploads/...) to full HTTP URLs.
     * ExoPlayer's FileDataSource treats bare "/" paths as local filesystem paths,
     * causing ENOENT when the file only lives on the server.
     */
    fun resolveVideoUrl(url: String): String {
        if (url.startsWith("/")) return tenantProvider.getBaseUrl().trimEnd('/') + url
        if (!url.startsWith("http://") && !url.startsWith("https://")) return url

        // The backend can return a direct storage (MinIO) URL on its own loopback port,
        // e.g. http://localhost:9000/bucket/file.mp4 — reachable from the server but not
        // from the device. ExoPlayer bypasses AuthInterceptor, so redo the host/port
        // rewrite here for playback.
        val original = url.toHttpUrlOrNull() ?: return url
        if (original.host != "localhost" && original.host != "127.0.0.1") return url
        val gateway = tenantProvider.getBaseUrl().toHttpUrlOrNull() ?: return url

        return original.newBuilder()
            .scheme(gateway.scheme)
            .host(gateway.host)
            .port(gateway.port)
            .build()
            .toString()
    }
}
