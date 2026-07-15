package com.studentprofile.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors the backend's AcademicYear / AcademicYearListResponse, confirmed against the local
// backend's /openapi.json - see GET /api/v1/academic-years/.
@Serializable
data class AcademicYearDto(
    val id: Int = 0,
    val name: String = "",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_current") val isCurrent: Boolean = false
)

@Serializable
data class AcademicYearListResponse(
    val items: List<AcademicYearDto> = emptyList(),
    val total: Int = 0
)
