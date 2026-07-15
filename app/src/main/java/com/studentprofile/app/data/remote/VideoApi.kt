package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.ContentApiItem
import com.studentprofile.app.domain.models.WatermarkConfig
import retrofit2.http.GET
import retrofit2.http.Path

// Same routes the parent app uses for playback: fetch the content record linked to a
// taught class (content_id from the schedule), then the superadmin watermark config.
interface VideoApi {
    @GET("/api/v1/content/{contentId}")
    suspend fun getVideoContent(@Path("contentId") contentId: Int): ContentApiItem

    // Returns a list of configs; pick the default or first one on the Android side
    @GET("/api/v1/videos/{video_id}/watermark")
    suspend fun getVideoWatermark(@Path("video_id") videoId: String): List<WatermarkConfig>
}
