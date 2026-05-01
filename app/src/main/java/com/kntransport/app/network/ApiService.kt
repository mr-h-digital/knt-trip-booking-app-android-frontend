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

    @PATCH("api/trips/{id}/cancel")
    suspend fun cancelTrip(@Path("id") id: String, @Body request: CancelTripRequest): Response<TripBookingDto>

    @POST("api/trips/{id}/rate")
    suspend fun rateTrip(@Path("id") id: String, @Body request: RateTripRequest): Response<Unit>

    // ── Lift Clubs ────────────────────────────────────────────────────────────
    @GET("api/lift-clubs")
    suspend fun getLiftClubs(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<PagedResponse<LiftClubDto>>

    @GET("api/lift-clubs/{id}")
    suspend fun getLiftClub(@Path("id") id: String): Response<LiftClubDto>

    @POST("api/lift-clubs")
    suspend fun createLiftClub(@Body request: CreateLiftClubRequest): Response<LiftClubDto>

    @GET("api/lift-clubs/my-subscriptions")
    suspend fun getMyLiftClubSubscriptions(): Response<List<LiftClubDto>>

    @GET("api/lift-clubs/my-clubs")
    suspend fun getMyLiftClubs(): Response<List<LiftClubDto>>

    @POST("api/lift-clubs/{id}/subscribe")
    suspend fun subscribeToLiftClub(@Path("id") id: String): Response<Unit>

    // ── Quotes ────────────────────────────────────────────────────────────────
    @GET("api/quotes/{id}")
    suspend fun getQuote(@Path("id") id: String): Response<QuoteDto>

    @GET("api/trips/{id}/quotes")
    suspend fun getTripQuotes(@Path("id") id: String): Response<List<QuoteDto>>

    @POST("api/quotes/{id}/respond")
    suspend fun respondToQuote(
        @Path("id") id: String,
        @Body request: QuoteAcceptRequest,
    ): Response<QuoteDto>

    // ── Admin — Users ─────────────────────────────────────────────────────────
    @GET("api/admin/users")
    suspend fun adminListUsers(
        @Query("role") role: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<PagedResponse<UserDto>>

    @GET("api/admin/users/{id}")
    suspend fun adminGetUser(@Path("id") id: String): Response<UserDto>

    @POST("api/admin/users")
    suspend fun adminCreateUser(@Body request: AdminUserRequest): Response<UserDto>

    @PUT("api/admin/users/{id}")
    suspend fun adminUpdateUser(@Path("id") id: String, @Body request: AdminUserRequest): Response<UserDto>

    @DELETE("api/admin/users/{id}")
    suspend fun adminDeleteUser(@Path("id") id: String): Response<Unit>

    // ── Admin — Fleet ─────────────────────────────────────────────────────────
    @GET("api/admin/vehicles")
    suspend fun getVehicles(
        @Query("active") active: Boolean? = null,
        @Query("page")   page: Int = 0,
        @Query("size")   size: Int = 50,
    ): Response<PagedResponse<VehicleDto>>

    @GET("api/admin/vehicles/{id}")
    suspend fun getVehicle(@Path("id") id: String): Response<VehicleDto>

    @POST("api/admin/vehicles")
    suspend fun createVehicle(@Body request: CreateVehicleRequest): Response<VehicleDto>

    @PUT("api/admin/vehicles/{id}")
    suspend fun updateVehicle(@Path("id") id: String, @Body request: CreateVehicleRequest): Response<VehicleDto>

    @DELETE("api/admin/vehicles/{id}")
    suspend fun deactivateVehicle(@Path("id") id: String): Response<Unit>

    @PATCH("api/admin/vehicles/{id}/reactivate")
    suspend fun reactivateVehicle(@Path("id") id: String): Response<VehicleDto>

    @Multipart
    @POST("api/admin/vehicles/{id}/photo")
    suspend fun uploadVehiclePhoto(
        @Path("id") id: String,
        @Part photo: MultipartBody.Part,
    ): Response<VehicleDto>

    @PATCH("api/admin/drivers/{driverId}/assign-vehicle")
    suspend fun assignVehicle(@Path("driverId") driverId: String, @Body request: AssignVehicleRequest): Response<UserDto>

    // ── Admin — Trips ─────────────────────────────────────────────────────────
    @GET("api/admin/trips")
    suspend fun adminListTrips(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<PagedResponse<TripBookingDto>>

    @GET("api/admin/trips/{tripId}")
    suspend fun adminGetTrip(@Path("tripId") tripId: String): Response<TripBookingDto>

    @PATCH("api/admin/trips/{tripId}/assign-driver")
    suspend fun adminAssignDriver(@Path("tripId") tripId: String, @Body request: AssignDriverRequest): Response<TripBookingDto>

    @PATCH("api/admin/trips/{tripId}/cancel")
    suspend fun adminCancelTrip(@Path("tripId") tripId: String, @Body request: CancelTripRequest): Response<TripBookingDto>

    @PATCH("api/admin/trips/{tripId}/quote")
    suspend fun adminUpdateQuote(@Path("tripId") tripId: String, @Body body: Map<String, Double>): Response<TripBookingDto>

    // ── Admin — Analytics & Financials ────────────────────────────────────────
    @GET("api/admin/analytics")
    suspend fun getAnalytics(): Response<AnalyticsDto>

    @GET("api/admin/financial-report")
    suspend fun getFinancialReport(
        @Query("from") from: String = "",
        @Query("to")   to: String = "",
    ): Response<FinancialReportDto>

    // ── Live tracking ─────────────────────────────────────────────────────────

    /** Commuter polls this to get the driver's latest position during a trip. */
    @GET("api/trips/{id}/location")
    suspend fun getTripLocation(@Path("id") id: String): Response<DriverLocationDto>

    /** Driver pushes their GPS position periodically while a trip is IN_PROGRESS. */
    @PUT("api/driver/trips/{id}/location")
    suspend fun updateDriverLocation(
        @Path("id") id: String,
        @Body request: UpdateDriverLocationRequest,
    ): Response<Unit>

    // ── Driver API ─────────────────────────────────────────────────────────────
    @GET("api/driver/trips")
    suspend fun getDriverTrips(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<PagedResponse<TripBookingDto>>

    @GET("api/driver/trips/{id}")
    suspend fun getDriverTrip(@Path("id") id: String): Response<TripBookingDto>

    @PATCH("api/driver/trips/{id}/status")
    suspend fun updateTripStatus(@Path("id") id: String, @Body request: UpdateTripStatusRequest): Response<TripBookingDto>

    @PATCH("api/driver/trips/{id}/cancel")
    suspend fun cancelDriverTrip(@Path("id") id: String, @Body request: CancelTripRequest): Response<TripBookingDto>

    @GET("api/driver/earnings")
    suspend fun getDriverEarnings(): Response<DriverEarningsDto>

    @GET("api/driver/available-trips")
    suspend fun getAvailableTrips(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<PagedResponse<TripBookingDto>>

    @GET("api/driver/available-trips/{id}")
    suspend fun getAvailableTripById(@Path("id") id: String): Response<TripBookingDto>

    @GET("api/driver/trips/{id}/my-quote")
    suspend fun getMyQuoteForTrip(@Path("id") id: String): Response<QuoteDto>

    @POST("api/driver/trips/{id}/quote")
    suspend fun createDriverQuote(
        @Path("id") id: String,
        @Body request: DriverQuoteRequest,
    ): Response<QuoteDto>

    @PUT("api/driver/quotes/{quoteId}")
    suspend fun editDriverQuote(
        @Path("quoteId") quoteId: String,
        @Body request: DriverQuoteRequest,
    ): Response<QuoteDto>

    @DELETE("api/driver/quotes/{quoteId}")
    suspend fun cancelDriverQuote(@Path("quoteId") quoteId: String): Response<Unit>

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
