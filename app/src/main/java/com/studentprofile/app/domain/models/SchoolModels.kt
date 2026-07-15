package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class School(
    val subdomain: String,
    @SerialName("school_name") val name: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("primary_color") val primaryColor: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)
