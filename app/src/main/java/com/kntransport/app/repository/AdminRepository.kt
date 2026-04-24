package com.kntransport.app.repository

import com.kntransport.app.network.*

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

    suspend fun assignVehicle(driverId: String, vehicleId: String?) =
        safeApiCall { api.assignVehicle(driverId, AssignVehicleRequest(vehicleId)) }

    // ── Trips ─────────────────────────────────────────────────────────────────

    suspend fun listAllTrips() =
        safeApiCall { api.adminListTrips() }

    suspend fun assignDriver(tripId: String, driverId: String) =
        safeApiCall { api.adminAssignDriver(tripId, AssignDriverRequest(driverId)) }

    // ── Analytics & Financials ────────────────────────────────────────────────

    suspend fun getAnalytics() =
        safeApiCall { api.getAnalytics() }

    suspend fun getFinancialReport(from: String = "", to: String = "") =
        safeApiCall { api.getFinancialReport(from, to) }
}
