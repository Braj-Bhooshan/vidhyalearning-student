package com.studentprofile.app.data.remote

import com.studentprofile.app.BuildConfig
import com.studentprofile.app.data.local.TenantProvider
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tenantProvider: TenantProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val subdomain = tenantProvider.getSubdomain()
        val token = tenantProvider.getAccessToken()

        val builder = request.newBuilder()
        val originalUrl = request.url
        val path = originalUrl.encodedPath

        // Storage requests (branding/media) go through the same API gateway on 8002
        // as everything else, so the device never needs to talk to a storage host directly.
        val isStorageRequest = originalUrl.host == "minio" ||
                path.contains("/media/") ||
                path.contains("/static/") ||
                path.contains("/uploads/") ||
                path.contains("/storage/")

        if (subdomain != null) {
            if (BuildConfig.DEBUG) {
                // Local backend has no real DNS for *.localtest.me, so every request is
                // sent to the tenant proxy on 127.0.0.1:8002 with a spoofed Host header
                // that the proxy uses to resolve the tenant.
                val newUrl = originalUrl.newBuilder()
                    .host("127.0.0.1")
                    .port(8002)
                    .build()
                builder.url(newUrl)
                if (!isStorageRequest) {
                    builder.header("X-Tenant-Subdomain", subdomain)
                        .header("Host", "$subdomain.localtest.me")
                }
            } else {
                // Live API: the subdomain has real DNS, so route straight to it.
                val newUrl = originalUrl.newBuilder()
                    .scheme("https")
                    .host("$subdomain.${TenantProvider.LIVE_API_DOMAIN}")
                    .build()
                builder.url(newUrl)
                if (!isStorageRequest) {
                    builder.header("X-Tenant-Subdomain", subdomain)
                }
            }
        }

        val isLoginEndpoint = path.contains("/login") || path.contains("/access-token")
        if (token != null && !isLoginEndpoint && !isStorageRequest) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
