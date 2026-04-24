package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.DriverEarningsDto
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DriverViewModel : ViewModel() {

    private val repo by lazy { TripRepository() }

    // ── Trips ─────────────────────────────────────────────────────────────────

    private val _trips = MutableStateFlow<ApiResult<List<TripBookingDto>>>(ApiResult.Loading)
    val trips: StateFlow<ApiResult<List<TripBookingDto>>> = _trips

    private val _selectedTrip = MutableStateFlow<ApiResult<TripBookingDto>?>(null)
    val selectedTrip: StateFlow<ApiResult<TripBookingDto>?> = _selectedTrip

    private val _tripActionState = MutableStateFlow<ApiResult<TripBookingDto>?>(null)
    val tripActionState: StateFlow<ApiResult<TripBookingDto>?> = _tripActionState

    fun loadTrips() {
        viewModelScope.launch {
            _trips.value = ApiResult.Loading
            val result = repo.getDriverTrips()
            _trips.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    fun loadTrip(id: String) {
        viewModelScope.launch {
            _selectedTrip.value = ApiResult.Loading
            _selectedTrip.value = repo.getDriverTrip(id)
        }
    }

    fun updateTripStatus(id: String, status: String) {
        viewModelScope.launch {
            _tripActionState.value = ApiResult.Loading
            _tripActionState.value = repo.updateTripStatus(id, status)
        }
    }

    fun cancelTrip(id: String, reason: String, note: String = "") {
        viewModelScope.launch {
            _tripActionState.value = ApiResult.Loading
            _tripActionState.value = repo.cancelDriverTrip(id, reason, note)
        }
    }

    fun resetTripActionState() { _tripActionState.value = null }

    // ── Earnings ──────────────────────────────────────────────────────────────

    private val _earnings = MutableStateFlow<ApiResult<DriverEarningsDto>?>(null)
    val earnings: StateFlow<ApiResult<DriverEarningsDto>?> = _earnings

    fun loadEarnings() {
        viewModelScope.launch {
            _earnings.value = ApiResult.Loading
            _earnings.value = repo.getDriverEarnings()
        }
    }
}
