package com.studentprofile.app.presentation.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import com.studentprofile.app.domain.models.ChildSummary
import com.studentprofile.app.domain.models.MpinRequest
import com.studentprofile.app.domain.models.ParentAccount
import com.studentprofile.app.domain.models.StudentDetails
import com.studentprofile.app.domain.models.StudentProfile
import com.studentprofile.app.data.local.TenantProvider
import androidx.core.content.edit
import com.studentprofile.app.data.remote.AcademicYearApi
import com.studentprofile.app.data.remote.AuthApi
import com.studentprofile.app.data.remote.SchoolApi
import com.studentprofile.app.data.remote.StudentAuthApi
import com.studentprofile.app.data.repository.ParentRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: SharedPreferences,
    private val parentRepo: ParentRepository,
    private val tenantProvider: TenantProvider,
    private val authApi: AuthApi,
    private val schoolApi: SchoolApi,
    private val studentAuthApi: StudentAuthApi,
    private val academicYearApi: AcademicYearApi,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private val errorBodyJson = Json { ignoreUnknownKeys = true }

        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_LOGGED_IN_STUDENT_ID = "loggedInStudentId"
        private const val KEY_LOGGED_IN_PARENT_ID = "loggedInParentId"
        private const val KEY_STUDENT_DISPLAY_NAME = "studentDisplayName"
        private const val KEY_STUDENT_CLASS_INFO = "studentClassInfo"
        private const val KEY_STUDENT_SECTION = "studentSection"
        private const val KEY_STUDENT_SESSION = "studentSession"
        private const val KEY_STUDENT_ADMISSION_ID = "studentAdmissionId"
        private const val KEY_STUDENT_FATHER_NAME = "studentFatherName"
        private const val KEY_STUDENT_MOTHER_NAME = "studentMotherName"
        private const val KEY_STUDENT_PHOTO_URL = "studentPhotoUrl"
    }

    private var lastFetchedChildren: List<StudentProfile> = emptyList()

    private val _authState = MutableStateFlow<AuthState>(AuthState.SubdomainRequired)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _selectedStudentDetails = MutableStateFlow<StudentDetails?>(null)
    val selectedStudentDetails: StateFlow<StudentDetails?> = _selectedStudentDetails.asStateFlow()

    private val _currentAcademicSession = MutableStateFlow<String?>(null)
    val currentAcademicSession: StateFlow<String?> = _currentAcademicSession.asStateFlow()

    private val _subdomain = MutableStateFlow<String?>(null)
    val subdomain: StateFlow<String?> = _subdomain.asStateFlow()

    private val _apiUrl = MutableStateFlow<String?>(null)
    val apiUrl: StateFlow<String?> = _apiUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _subdomainError = MutableStateFlow<String?>(null)
    val subdomainError: StateFlow<String?> = _subdomainError.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _schoolLogoUrl = MutableStateFlow(cachedLogoFileUri() ?: resolveUrl(tenantProvider.getSchoolLogoUrl()))
    val schoolLogoUrl: StateFlow<String?> = _schoolLogoUrl.asStateFlow()

    private fun cachedLogoFileUri(): String? {
        val f = File(appContext.cacheDir, "school_logo.jpg")
        return if (f.exists() && f.length() > 0) "file://${f.absolutePath}" else null
    }

    // AuthInterceptor rewrites the host/port of every outgoing request (API or storage) to
    // whichever host actually serves it, so an already-absolute URL doesn't need rewriting
    // here - only backend-relative paths need the base URL prefixed.
    private fun resolveUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return when {
            raw.startsWith("http://") || raw.startsWith("https://") || raw.startsWith("file://") -> raw
            else -> "${tenantProvider.getBaseUrl().trimEnd('/')}$raw"
        }
    }

    private suspend fun downloadAndCacheLogo(logoUrl: String): String? {
        return try {
            val body = schoolApi.downloadLogo(logoUrl)
            val file = File(appContext.cacheDir, "school_logo.jpg")
            file.outputStream().use { body.byteStream().copyTo(it) }
            "file://${file.absolutePath}"
        } catch (_: Exception) { null }
    }

    init {
        restoreSession()
    }

    private fun restoreSession() {
        tenantProvider.clearAll()
        prefs.edit(commit = true) { clear() }
        _authState.value = AuthState.SubdomainRequired
    }

    fun verifySubdomain(domain: String) {
        val formattedDomain = domain.trim().lowercase()
        viewModelScope.launch {
            _isLoading.value = true
            _subdomainError.value = null
            try {
                val school = schoolApi.getSchoolDetails(formattedDomain)
                tenantProvider.setSubdomain(formattedDomain)
                tenantProvider.setSchoolName(school.name)
                tenantProvider.setSchoolLogoUrl(school.logoUrl)

                val resolvedLogo = resolveUrl(school.logoUrl)
                if (resolvedLogo != null) {
                    _schoolLogoUrl.value = resolvedLogo
                    val localUri = downloadAndCacheLogo(resolvedLogo)
                    if (localUri != null) {
                        _schoolLogoUrl.value = localUri
                    }
                }

                _subdomain.value = formattedDomain
                _apiUrl.value = "https://$formattedDomain.localtest.me:8002/api/v1"
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                e.printStackTrace()
                _subdomainError.value = "Connection failed: ${extractErrorMessage(e)}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginError.value = "Please fill in all fields."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            try {
                val response = authApi.login(username = email, password = password)
                tenantProvider.setAccessToken(response.accessToken)

                val children = studentAuthApi.getMyChildren().map { it.toStudentProfile() }
                if (children.isEmpty()) {
                    _loginError.value = "No students linked to this account."
                } else {
                    lastFetchedChildren = children
                    parentRepo.addOrUpdateParent(ParentAccount(parentId = email, password = "", children = children))
                    prefs.edit().putString(KEY_LOGGED_IN_PARENT_ID, email).apply()
                    _authState.value = AuthState.StudentSelectionRequired(email, children)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loginError.value = "Login failed: ${extractErrorMessage(e)}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectStudent(studentId: String) {
        val isMpinRegistered = lastFetchedChildren.firstOrNull { it.studentId == studentId }?.mpinRegistered ?: false
        if (isMpinRegistered) {
            _authState.value = AuthState.MPINLoginRequired(studentId)
        } else {
            _authState.value = AuthState.MPINRegistrationRequired(studentId)
        }
    }

    fun registerMPIN(studentId: String, mpin: String) {
        if (mpin.length < 4) {
            _authState.value = AuthState.Error("MPIN must be 4 digits.")
            return
        }
        viewModelScope.launch {
            try {
                val response = studentAuthApi.setupMpin(MpinRequest(studentId = studentId.toInt(), mpin = mpin))
                tenantProvider.setAccessToken(response.accessToken)
                completeLogin(studentId)
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error(extractErrorMessage(e))
            }
        }
    }

    fun verifyMPIN(studentId: String, mpin: String) {
        viewModelScope.launch {
            try {
                val response = studentAuthApi.verifyMpin(MpinRequest(studentId = studentId.toInt(), mpin = mpin))
                tenantProvider.setAccessToken(response.accessToken)
                completeLogin(studentId)
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = AuthState.Error(extractErrorMessage(e))
            }
        }
    }

    private fun completeLogin(studentId: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_LOGGED_IN_STUDENT_ID, studentId)
            .apply()

        val student = lastFetchedChildren.firstOrNull { it.studentId == studentId }
        if (student != null) {
            persistSelectedStudent(student)
            _selectedStudentDetails.value = buildStudentDetails(student)
        }

        loadCurrentAcademicSession()
        _authState.value = AuthState.Authenticated(studentId)
    }

    private fun loadCurrentAcademicSession() {
        viewModelScope.launch {
            try {
                val years = academicYearApi.listAcademicYears().items
                val current = years.firstOrNull { it.isCurrent } ?: years.firstOrNull { it.isActive }
                _currentAcademicSession.value = current?.name
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun ChildSummary.toStudentProfile(): StudentProfile = StudentProfile(
        studentId = id.toString(),
        displayName = fullName,
        classInfo = gradeLevel,
        section = section,
        session = session,
        admissionId = this.studentId,
        avatarResId = null,
        mpinRegistered = mpinRegistered,
        fatherName = fatherName,
        motherName = motherName,
        photoUrl = resolveUrl(studentPhotoUrl)
    )

    private fun persistSelectedStudent(student: StudentProfile) {
        prefs.edit()
            .putString(KEY_STUDENT_DISPLAY_NAME, student.displayName)
            .putString(KEY_STUDENT_CLASS_INFO, student.classInfo)
            .putString(KEY_STUDENT_SECTION, student.section)
            .putString(KEY_STUDENT_SESSION, student.session)
            .putString(KEY_STUDENT_ADMISSION_ID, student.admissionId)
            .putString(KEY_STUDENT_FATHER_NAME, student.fatherName)
            .putString(KEY_STUDENT_MOTHER_NAME, student.motherName)
            .putString(KEY_STUDENT_PHOTO_URL, student.photoUrl)
            .apply()
    }

    private fun restoreSelectedStudentDetails() {
        val studentId = prefs.getString(KEY_LOGGED_IN_STUDENT_ID, null) ?: return
        val displayName = prefs.getString(KEY_STUDENT_DISPLAY_NAME, null) ?: return
        val student = StudentProfile(
            studentId = studentId,
            displayName = displayName,
            classInfo = prefs.getString(KEY_STUDENT_CLASS_INFO, null) ?: "",
            section = prefs.getString(KEY_STUDENT_SECTION, null),
            session = prefs.getString(KEY_STUDENT_SESSION, null),
            admissionId = prefs.getString(KEY_STUDENT_ADMISSION_ID, null),
            fatherName = prefs.getString(KEY_STUDENT_FATHER_NAME, null),
            motherName = prefs.getString(KEY_STUDENT_MOTHER_NAME, null),
            photoUrl = prefs.getString(KEY_STUDENT_PHOTO_URL, null)
        )
        _selectedStudentDetails.value = buildStudentDetails(student)
        loadCurrentAcademicSession()
    }

    @Serializable
    private data class FastApiErrorBody(val detail: String? = null)

    private fun extractErrorMessage(e: Exception): String {
        if (e is HttpException) {
            val detail = e.response()?.errorBody()?.string()?.let { body ->
                try {
                    errorBodyJson.decodeFromString<FastApiErrorBody>(body).detail
                } catch (parseError: Exception) {
                    null
                }
            }
            if (!detail.isNullOrBlank()) return detail
        }
        return e.localizedMessage ?: "Unknown error"
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_LOGGED_IN_STUDENT_ID)
            .remove(KEY_LOGGED_IN_PARENT_ID)
            .apply()
        _authState.value = AuthState.Unauthenticated
    }

    fun getCurrentParentId(): String? {
        return prefs.getString(KEY_LOGGED_IN_PARENT_ID, null)
    }

    fun getLinkedStudentsForCurrentSession(): List<StudentProfile> {
        val parentId = getCurrentParentId()
        if (!parentId.isNullOrBlank()) {
            return parentRepo.getChildrenForParent(parentId)
        }

        val currentStudentId = prefs.getString(KEY_LOGGED_IN_STUDENT_ID, null)
        if (!currentStudentId.isNullOrBlank()) {
            return parentRepo.findParentByStudentId(currentStudentId)?.children ?: emptyList()
        }

        return emptyList()
    }

    fun changeSubdomain() {
        tenantProvider.clearAll()
        _subdomain.value = null
        _apiUrl.value = null
        _subdomainError.value = null
        _schoolLogoUrl.value = null
        logout()
        _authState.value = AuthState.SubdomainRequired
    }

    fun refreshSelectedStudentFromSession() {
        restoreSelectedStudentDetails()
    }

    fun getSelectedStudentDetails(): StudentDetails? = _selectedStudentDetails.value

    private fun buildStudentDetails(student: StudentProfile): StudentDetails = StudentDetails(
        student = student,
        fatherName = student.fatherName,
        motherName = student.motherName
    )

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
