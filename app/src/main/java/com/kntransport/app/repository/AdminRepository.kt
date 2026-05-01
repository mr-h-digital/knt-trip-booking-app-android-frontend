package com.kntransport.app.repository

import com.kntransport.app.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AdminRepository {

    private val api by lazy { ApiClient.service }

    // ── Users ─────────────────────────────────────────────────────────────────

    suspend fun listUsers(role: String? = null) =
        safeApiCall { api.adminListUsers(role = role) }

    suspend fun getUser(id: String) =
        safeApiCall { api.adminGetUser(id) }

    suspend fun createUser(request: AdminUserRequest) =
        safeApiCall { api.adminCreateUser(request) }

    suspend fun updateUser(id: String, request: AdminUserRequest) =
        safeApiCall { api.adminUpdateUser(id, request) }

    suspend fun deleteUser(id: String) =
        safeApiCall { api.adminDeleteUser(id) }

    // ── Fleet ─────────────────────────────────────────────────────────────────

    suspend fun listVehicles(activeOnly: Boolean? = null) =
        safeApiCall { api.getVehicles(active = activeOnly) }

    suspend fun getVehicle(id: String) =
        safeApiCall { api.getVehicle(id) }

    suspend fun createVehicle(request: CreateVehicleRequest) =
        safeApiCall { api.createVehicle(request) }

    suspend fun updateVehicle(id: String, request: CreateVehicleRequest) =
        safeApiCall { api.updateVehicle(id, request) }

    suspend fun deactivateVehicle(id: String) =
        safeApiCall { api.deactivateVehicle(id) }

    suspend fun reactivateVehicle(id: String) =
        safeApiCall { api.reactivateVehicle(id) }

    suspend fun uploadVehiclePhoto(id: String, file: File): ApiResult<VehicleDto> {
        val body = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("photo", file.name, body)
        return safeApiCall { api.uploadVehiclePhoto(id, part) }
    }

    suspend fun assignVehicle(driverId: String, vehicleId: String?) =
        safeApiCall { api.assignVehicle(driverId, AssignVehicleRequest(vehicleId)) }

    // ── Trips ─────────────────────────────────────────────────────────────────

    suspend fun listAllTrips() =
        safeApiCall { api.adminListTrips() }

    suspend fun getTrip(tripId: String) =
        safeApiCall { api.adminGetTrip(tripId) }

    suspend fun assignDriver(tripId: String, driverId: String) =
        safeApiCall { api.adminAssignDriver(tripId, AssignDriverRequest(driverId)) }

    suspend fun cancelTrip(tripId: String, reason: String, note: String = "") =
        safeApiCall { api.adminCancelTrip(tripId, CancelTripRequest(reason, note)) }

    suspend fun updateQuote(tripId: String, amount: Double) =
        safeApiCall { api.adminUpdateQuote(tripId, mapOf("amount" to amount)) }

    // ── Analytics & Financials ────────────────────────────────────────────────

    suspend fun getAnalytics() =
        safeApiCall { api.getAnalytics() }

    suspend fun getFinancialReport(from: String = "", to: String = "") =
        safeApiCall { api.getFinancialReport(from, to) }
}
