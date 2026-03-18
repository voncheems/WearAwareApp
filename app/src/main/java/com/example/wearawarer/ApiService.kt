package com.example.wearawarer

import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String
)

data class NotificationAlert(
    val id: Int,
    val detection_id: Int,
    val is_read: Boolean,
    val created_at: String,
    val result: String?,
    val missing_ppe: List<String>?,
    val photo_url: String?,
    val station: String?,
    val location: String?
)

data class DetectionStats(
    val total: Int,
    val violations: Int,
    val compliant: Int,
    val compliance_rate: Int
)

data class InspectorProfile(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String,
    val created_at: String
)

data class UpdateProfileRequest(
    val full_name: String? = null,
    val current_password: String? = null,
    val new_password: String? = null
)

data class UpdateProfileResponse(
    val success: Boolean,
    val user: InspectorProfile
)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/inspector/notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<List<NotificationAlert>>

    @GET("api/inspector/detections/stats")
    suspend fun getStats(
        @Header("Authorization") token: String
    ): Response<DetectionStats>

    @PATCH("api/inspector/notifications/{id}/read")
    suspend fun markAsRead(
        @Header("Authorization") token: String,
        @Path("id") notificationId: Int
    ): Response<Map<String, Any>>

    @GET("api/inspector/detections")
    suspend fun getDetections(
        @Header("Authorization") token: String
    ): Response<List<Map<String, Any>>>

    @GET("api/inspector/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<InspectorProfile>

    @PATCH("api/inspector/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: UpdateProfileRequest
    ): Response<UpdateProfileResponse>
}