package com.studentprofile.app.data.remote

import com.studentprofile.app.domain.models.School
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface SchoolApi {
    @GET("school/{subdomain}")
    suspend fun getSchoolDetails(@Path("subdomain") subdomain: String): School

    @GET
    suspend fun downloadLogo(@Url url: String): ResponseBody
}
