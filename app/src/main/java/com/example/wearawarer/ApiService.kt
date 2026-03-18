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

data class TokenRequest(val fcm_token: String)

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

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/users/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Body body: TokenRequest
    ): Response<Map<String, Any>>

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
}