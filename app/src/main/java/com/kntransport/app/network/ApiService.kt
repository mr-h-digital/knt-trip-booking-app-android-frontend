package com.kntransport.app.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // ── User ──────────────────────────────────────────────────────────────────
    @GET("api/users/me")
    suspend fun getProfile(): Response<UserDto>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserDto>

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): Response<UserDto>

    // ── Trips ─────────────────────────────────────────────────────────────────
    @GET("api/trips")
    suspend fun getMyTrips(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<PagedResponse<TripBookingDto>>

    @GET("api/trips/{id}")
    suspend fun getTrip(@Path("id") id: String): Response<TripBookingDto>

    @POST("api/trips")
    suspend fun createTrip(@Body request: CreateTripRequest): Response<TripBookingDto>

    // ── Lift Clubs ────────────────────────────────────────────────────────────
    @GET("api/lift-clubs")
    suspend fun getLiftClubs(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<PagedResponse<LiftClubDto>>

    @GET("api/lift-clubs/{id}")
    suspend fun getLiftClub(@Path("id") id: String): Response<LiftClubDto>

    @POST("api/lift-clubs/{id}/subscribe")
    suspend fun subscribeToLiftClub(@Path("id") id: String): Response<Unit>

    // ── Quotes ────────────────────────────────────────────────────────────────
    @GET("api/quotes/{id}")
    suspend fun getQuote(@Path("id") id: String): Response<QuoteDto>

    @POST("api/quotes/{id}/respond")
    suspend fun respondToQuote(
        @Path("id") id: String,
        @Body request: QuoteAcceptRequest,
    ): Response<QuoteDto>

    // ── Notifications ─────────────────────────────────────────────────────────
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30,
    ): Response<PagedResponse<NotificationDto>>

    @POST("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<Unit>

    @POST("api/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<Unit>
}
