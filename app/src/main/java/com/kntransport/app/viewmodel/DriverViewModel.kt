package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.DriverEarningsDto
import com.kntransport.app.network.QuoteDto
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.repository.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // ── Available trips (Option C) ────────────────────────────────────────────

    private val _availableTrips = MutableStateFlow<ApiResult<List<TripBookingDto>>>(ApiResult.Loading)
    val availableTrips: StateFlow<ApiResult<List<TripBookingDto>>> = _availableTrips

    fun loadAvailableTrips() {
        viewModelScope.launch {
            _availableTrips.value = ApiResult.Loading
            val result = repo.getAvailableTrips()
            _availableTrips.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    // ── Quote actions ─────────────────────────────────────────────────────────

    private val _quoteState = MutableStateFlow<ApiResult<QuoteDto>?>(null)
    val quoteState: StateFlow<ApiResult<QuoteDto>?> = _quoteState

    private val _cancelQuoteState = MutableStateFlow<ApiResult<Unit>?>(null)
    val cancelQuoteState: StateFlow<ApiResult<Unit>?> = _cancelQuoteState

    fun createQuote(tripId: String, amount: Double, note: String) {
        viewModelScope.launch {
            _quoteState.value = ApiResult.Loading
            _quoteState.value = repo.createDriverQuote(tripId, amount, note)
        }
    }

    fun editQuote(quoteId: String, amount: Double, note: String) {
        viewModelScope.launch {
            _quoteState.value = ApiResult.Loading
            _quoteState.value = repo.editDriverQuote(quoteId, amount, note)
        }
    }

    fun cancelQuote(quoteId: String) {
        viewModelScope.launch {
            _cancelQuoteState.value = ApiResult.Loading
            _cancelQuoteState.value = repo.cancelDriverQuote(quoteId)
        }
    }

    fun resetQuoteState() { _quoteState.value = null }
    fun resetCancelQuoteState() { _cancelQuoteState.value = null }

    // ── Live location sharing ─────────────────────────────────────────────────

    private val _isSharingLocation = MutableStateFlow(false)
    val isSharingLocation: StateFlow<Boolean> = _isSharingLocation

    private var locationBroadcastJob: Job? = null

    /** Called by the driver screen every time a new GPS fix arrives. */
    fun broadcastLocation(tripId: String, latitude: Double, longitude: Double) {
        if (!_isSharingLocation.value) return
        viewModelScope.launch {
            repo.updateDriverLocation(tripId, latitude, longitude)
        }
    }

    fun startSharingLocation() { _isSharingLocation.value = true }

    fun stopSharingLocation() {
        _isSharingLocation.value = false
        locationBroadcastJob?.cancel()
        locationBroadcastJob = null
    }

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
