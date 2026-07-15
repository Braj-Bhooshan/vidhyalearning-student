package com.studentprofile.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.studentprofile.app.BuildConfig

class TenantProvider(context: Context) {

    private object Keys {
        const val TENANT_SUBDOMAIN = "current_tenant_subdomain"
        const val SCHOOL_NAME = "current_school_name"
        const val SCHOOL_LOGO_URL = "current_school_logo_url"
        const val ACCESS_TOKEN = "access_token"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("tenant_prefs", Context.MODE_PRIVATE)

    fun getSubdomain(): String? = prefs.getString(Keys.TENANT_SUBDOMAIN, null)

    fun setSubdomain(subdomain: String) {
        prefs.edit(commit = true) { putString(Keys.TENANT_SUBDOMAIN, subdomain) }
    }

    fun getSchoolName(): String? = prefs.getString(Keys.SCHOOL_NAME, null)

    fun setSchoolName(name: String) {
        prefs.edit(commit = true) { putString(Keys.SCHOOL_NAME, name) }
    }

    fun getSchoolLogoUrl(): String? = prefs.getString(Keys.SCHOOL_LOGO_URL, null)

    fun setSchoolLogoUrl(url: String?) {
        prefs.edit(commit = true) { putString(Keys.SCHOOL_LOGO_URL, url) }
    }

    fun getAccessToken(): String? = prefs.getString(Keys.ACCESS_TOKEN, null)

    fun setAccessToken(token: String) {
        prefs.edit(commit = true) { putString(Keys.ACCESS_TOKEN, token) }
    }

    fun getBaseUrl(): String = if (BuildConfig.DEBUG) "http://127.0.0.1:8002/" else "https://$LIVE_API_DOMAIN/"

    fun clearAuthData() {
        prefs.edit(commit = true) { remove(Keys.ACCESS_TOKEN) }
    }

    fun clearAll() {
        prefs.edit(commit = true) { clear() }
    }

    companion object {
        // Release builds talk to the tenant's own subdomain directly, e.g.
        // https://{subdomain}.vidyalearning.org — no local proxy involved.
        const val LIVE_API_DOMAIN = "vidyalearning.org"
    }
}
