package com.studentprofile.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class StudentApplication : Application(), SingletonImageLoader.Factory {

    // Same singleton OkHttpClient used for Retrofit calls. Its AuthInterceptor rewrites
    // any request host/port to the tenant proxy (127.0.0.1:8002 in debug, the tenant's own
    // subdomain in release) and attaches the tenant/auth headers - without it, image URLs
    // pointing at the backend's internal storage host aren't reachable from the device.
    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .build()
    }
}
