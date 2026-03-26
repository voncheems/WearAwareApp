package com.example.wearawarer

import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val user: UserData)

data class UserData(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String
)

data class ForgotPasswordRequest(val email: String, val reason: String?)

data class ForgotPasswordResponse(val success: Boolean)

data class NotificationAlert(
    val id: Int,
    val detection_id: Int,
    val is_read: Boolean,
    val created_at: String,
    val result: String?,
    val missing_ppe: List<String>?,
    val photo_url: String?,
    val station: String?,
    val location: String?,
    val worker_name: String?,
    val worker_employee_id: String?
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

data class Worker(
    val id: Int,
    val employee_id: String,
    val full_name: String,
    val position: String?,
    val contact_number: String?,
    val status: String,
    val created_at: String,
    val device_id: Int?,
    val station_label: String?
)

data class Station(
    val id: Int,
    val label: String,
    val location: String?
)

data class AssignWorkerRequest(val station_id: Int)

data class AssignWorkerResponse(
    val success: Boolean,
    val worker: Worker
)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ForgotPasswordResponse>

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

    @GET("api/inspector/workers")
    suspend fun getMyWorkers(
        @Header("Authorization") token: String
    ): Response<List<Worker>>

    @GET("api/inspector/workers/unassigned")
    suspend fun getUnassignedWorkers(
        @Header("Authorization") token: String
    ): Response<List<Worker>>

    @GET("api/inspector/stations")
    suspend fun getStations(
        @Header("Authorization") token: String
    ): Response<List<Station>>

    @PATCH("api/inspector/workers/{id}/assign")
    suspend fun assignWorker(
        @Header("Authorization") token: String,
        @Path("id") workerId: Int,
        @Body body: AssignWorkerRequest
    ): Response<AssignWorkerResponse>
}