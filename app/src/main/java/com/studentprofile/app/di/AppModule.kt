package com.studentprofile.app.di

import android.content.Context
import android.content.SharedPreferences
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.studentprofile.app.data.local.AssignmentTopicPrefs
import com.studentprofile.app.data.local.TenantProvider
import com.studentprofile.app.data.remote.AcademicDashboardApi
import com.studentprofile.app.data.remote.AcademicYearApi
import com.studentprofile.app.data.remote.AssignmentApi
import com.studentprofile.app.data.remote.AttendanceApi
import com.studentprofile.app.data.remote.AuthApi
import com.studentprofile.app.data.remote.AuthInterceptor
import com.studentprofile.app.data.remote.ClassesApi
import com.studentprofile.app.data.remote.GradesApi
import com.studentprofile.app.data.remote.MessageApi
import com.studentprofile.app.data.remote.NotificationApi
import com.studentprofile.app.data.remote.PerformanceApi
import com.studentprofile.app.data.remote.ProfileApi
import com.studentprofile.app.data.remote.SchoolApi
import com.studentprofile.app.data.remote.StudentAuthApi
import com.studentprofile.app.data.remote.VideoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTenantProvider(@ApplicationContext context: Context): TenantProvider = TenantProvider(context)

    @Provides
    @Singleton
    fun provideAuthInterceptor(tenantProvider: TenantProvider): AuthInterceptor = AuthInterceptor(tenantProvider)

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, tenantProvider: TenantProvider): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = true
        }
        return Retrofit.Builder()
            .baseUrl(tenantProvider.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideSchoolApi(retrofit: Retrofit): SchoolApi = retrofit.create(SchoolApi::class.java)

    @Provides
    @Singleton
    fun provideStudentAuthApi(retrofit: Retrofit): StudentAuthApi = retrofit.create(StudentAuthApi::class.java)

    @Provides
    @Singleton
    fun provideMessageApi(retrofit: Retrofit): MessageApi = retrofit.create(MessageApi::class.java)

    @Provides
    @Singleton
    fun provideAssignmentApi(retrofit: Retrofit): AssignmentApi = retrofit.create(AssignmentApi::class.java)

    @Provides
    @Singleton
    fun provideClassesApi(retrofit: Retrofit): ClassesApi = retrofit.create(ClassesApi::class.java)

    @Provides
    @Singleton
    fun provideAttendanceApi(retrofit: Retrofit): AttendanceApi = retrofit.create(AttendanceApi::class.java)

    @Provides
    @Singleton
    fun provideAcademicYearApi(retrofit: Retrofit): AcademicYearApi = retrofit.create(AcademicYearApi::class.java)

    @Provides
    @Singleton
    fun provideGradesApi(retrofit: Retrofit): GradesApi = retrofit.create(GradesApi::class.java)

    @Provides
    @Singleton
    fun provideAcademicDashboardApi(retrofit: Retrofit): AcademicDashboardApi = retrofit.create(AcademicDashboardApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideVideoApi(retrofit: Retrofit): VideoApi = retrofit.create(VideoApi::class.java)

    @Provides
    @Singleton
    fun providePerformanceApi(retrofit: Retrofit): PerformanceApi = retrofit.create(PerformanceApi::class.java)

    @Provides
    @Singleton
    fun provideUserPrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideAssignmentTopicPrefs(@ApplicationContext context: Context): AssignmentTopicPrefs =
        AssignmentTopicPrefs(context)
}
