package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.*
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val repo by lazy { AdminRepository() }

    // ── Users ─────────────────────────────────────────────────────────────────

    private val _users = MutableStateFlow<ApiResult<List<UserDto>>>(ApiResult.Loading)
    val users: StateFlow<ApiResult<List<UserDto>>> = _users

    private val _selectedUser = MutableStateFlow<UserDto?>(null)
    val selectedUser: StateFlow<UserDto?> = _selectedUser

    private val _userActionState = MutableStateFlow<ApiResult<UserDto>?>(null)
    val userActionState: StateFlow<ApiResult<UserDto>?> = _userActionState

    private val _deleteState = MutableStateFlow<ApiResult<Unit>?>(null)
    val deleteState: StateFlow<ApiResult<Unit>?> = _deleteState

    fun loadUsers(role: String? = null) {
        viewModelScope.launch {
            _users.value = ApiResult.Loading
            val result = repo.listUsers(role)
            _users.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    fun loadUser(id: String) {
        viewModelScope.launch {
            _selectedUser.value = null
            val result = repo.getUser(id)
            if (result is ApiResult.Success) _selectedUser.value = result.data
        }
    }

    fun createUser(request: AdminUserRequest) {
        viewModelScope.launch {
            _userActionState.value = ApiResult.Loading
            _userActionState.value = repo.createUser(request)
        }
    }

    fun updateUser(id: String, request: AdminUserRequest) {
        viewModelScope.launch {
            _userActionState.value = ApiResult.Loading
            _userActionState.value = repo.updateUser(id, request)
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            _deleteState.value = ApiResult.Loading
            _deleteState.value = repo.deleteUser(id)
        }
    }

    fun resetUserActionState() { _userActionState.value = null }
    fun resetDeleteState()     { _deleteState.value = null }

    // ── Fleet ─────────────────────────────────────────────────────────────────

    private val _vehicles = MutableStateFlow<ApiResult<List<VehicleDto>>>(ApiResult.Loading)
    val vehicles: StateFlow<ApiResult<List<VehicleDto>>> = _vehicles

    private val _selectedVehicle = MutableStateFlow<VehicleDto?>(null)
    val selectedVehicle: StateFlow<VehicleDto?> = _selectedVehicle

    private val _vehicleActionState = MutableStateFlow<ApiResult<VehicleDto>?>(null)
    val vehicleActionState: StateFlow<ApiResult<VehicleDto>?> = _vehicleActionState

    private val _deactivateState = MutableStateFlow<ApiResult<Unit>?>(null)
    val deactivateState: StateFlow<ApiResult<Unit>?> = _deactivateState

    fun loadVehicles(activeOnly: Boolean? = null) {
        viewModelScope.launch {
            _vehicles.value = ApiResult.Loading
            val result = repo.listVehicles(activeOnly)
            _vehicles.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    fun loadVehicle(id: String) {
        viewModelScope.launch {
            _selectedVehicle.value = null
            val result = repo.getVehicle(id)
            if (result is ApiResult.Success) _selectedVehicle.value = result.data
        }
    }

    fun createVehicle(
        make: String, model: String, colour: String, plate: String,
        year: Int, vehicleType: String, notes: String,
    ) {
        viewModelScope.launch {
            _vehicleActionState.value = ApiResult.Loading
            _vehicleActionState.value = repo.createVehicle(
                CreateVehicleRequest(make, model, colour, plate, year, vehicleType, null, notes)
            )
        }
    }

    fun updateVehicle(
        id: String,
        make: String, model: String, colour: String, plate: String,
        year: Int, vehicleType: String, notes: String,
    ) {
        viewModelScope.launch {
            _vehicleActionState.value = ApiResult.Loading
            _vehicleActionState.value = repo.updateVehicle(
                id, CreateVehicleRequest(make, model, colour, plate, year, vehicleType, null, notes)
            )
        }
    }

    fun deactivateVehicle(id: String) {
        viewModelScope.launch {
            _deactivateState.value = ApiResult.Loading
            _deactivateState.value = repo.deactivateVehicle(id)
        }
    }

    private val _reactivateState = MutableStateFlow<ApiResult<VehicleDto>?>(null)
    val reactivateState: StateFlow<ApiResult<VehicleDto>?> = _reactivateState

    fun reactivateVehicle(id: String) {
        viewModelScope.launch {
            _reactivateState.value = ApiResult.Loading
            _reactivateState.value = repo.reactivateVehicle(id)
        }
    }

    fun resetReactivateState() { _reactivateState.value = null }

    fun assignVehicle(driverId: String, vehicleId: String?) {
        viewModelScope.launch {
            repo.assignVehicle(driverId, vehicleId)
            loadUsers()
        }
    }

    fun uploadVehiclePhoto(id: String, file: java.io.File) {
        viewModelScope.launch {
            _vehicleActionState.value = ApiResult.Loading
            _vehicleActionState.value = repo.uploadVehiclePhoto(id, file)
        }
    }

    fun resetVehicleActionState() { _vehicleActionState.value = null }
    fun resetDeactivateState()    { _deactivateState.value = null }

    // ── Trips ─────────────────────────────────────────────────────────────────

    private val _allTrips = MutableStateFlow<ApiResult<List<TripBookingDto>>>(ApiResult.Loading)
    val allTrips: StateFlow<ApiResult<List<TripBookingDto>>> = _allTrips

    fun loadAllTrips() {
        viewModelScope.launch {
            _allTrips.value = ApiResult.Loading
            val result = repo.listAllTrips()
            _allTrips.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    private val _analytics = MutableStateFlow<ApiResult<AnalyticsDto>>(ApiResult.Loading)
    val analytics: StateFlow<ApiResult<AnalyticsDto>> = _analytics

    fun loadAnalytics() {
        viewModelScope.launch {
            _analytics.value = ApiResult.Loading
            _analytics.value = repo.getAnalytics()
        }
    }

    // ── Financial Report ──────────────────────────────────────────────────────

    private val _financialReport = MutableStateFlow<ApiResult<FinancialReportDto>>(ApiResult.Loading)
    val financialReport: StateFlow<ApiResult<FinancialReportDto>> = _financialReport

    fun loadFinancialReport(from: String = "", to: String = "") {
        viewModelScope.launch {
            _financialReport.value = ApiResult.Loading
            _financialReport.value = repo.getFinancialReport(from, to)
        }
    }
}
