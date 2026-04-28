package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.DriverLocationDto
import com.kntransport.app.network.QuoteDto
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.repository.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val repo by lazy { TripRepository() }

    private val _trips = MutableStateFlow<ApiResult<List<TripBookingDto>>>(ApiResult.Loading)
    val trips: StateFlow<ApiResult<List<TripBookingDto>>> = _trips

    private val _selectedTrip = MutableStateFlow<ApiResult<TripBookingDto>?>(null)
    val selectedTrip: StateFlow<ApiResult<TripBookingDto>?> = _selectedTrip

    private val _createState = MutableStateFlow<ApiResult<TripBookingDto>?>(null)
    val createState: StateFlow<ApiResult<TripBookingDto>?> = _createState

    private val _cancelState = MutableStateFlow<ApiResult<TripBookingDto>?>(null)
    val cancelState: StateFlow<ApiResult<TripBookingDto>?> = _cancelState

    private val _rateState = MutableStateFlow<ApiResult<Unit>?>(null)
    val rateState: StateFlow<ApiResult<Unit>?> = _rateState

    private val _quoteState = MutableStateFlow<ApiResult<QuoteDto>?>(null)
    val quoteState: StateFlow<ApiResult<QuoteDto>?> = _quoteState

    private val _driverLocation = MutableStateFlow<DriverLocationDto?>(null)
    val driverLocation: StateFlow<DriverLocationDto?> = _driverLocation

    private var locationPollingJob: Job? = null

    fun loadTrips() {
        viewModelScope.launch {
            _trips.value = ApiResult.Loading
            val result = repo.getMyTrips()
            _trips.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    fun createTrip(
        pickup    : String,
        dropoff   : String,
        date      : String,
        time      : String,
        passengers: Int,
        notes     : String = "",
    ) {
        viewModelScope.launch {
            _createState.value = ApiResult.Loading
            _createState.value = repo.createTrip(pickup, dropoff, date, time, passengers, notes)
        }
    }

    fun loadTrip(id: String) {
        viewModelScope.launch {
            _selectedTrip.value = ApiResult.Loading
            _selectedTrip.value = repo.getTrip(id)
        }
    }

    fun cancelTrip(id: String, reason: String, note: String = "") {
        viewModelScope.launch {
            _cancelState.value = ApiResult.Loading
            _cancelState.value = repo.cancelTrip(id, reason, note)
        }
    }

    fun rateTrip(id: String, rating: Int, comment: String = "") {
        viewModelScope.launch {
            _rateState.value = ApiResult.Loading
            _rateState.value = repo.rateTrip(id, rating, comment)
        }
    }

    fun respondToQuote(id: String, accepted: Boolean, paymentCycle: String? = null) {
        viewModelScope.launch {
            _quoteState.value = ApiResult.Loading
            _quoteState.value = repo.respondToQuote(id, accepted, paymentCycle)
        }
    }

    fun startLocationPolling(tripId: String) {
        locationPollingJob?.cancel()
        locationPollingJob = viewModelScope.launch {
            while (true) {
                val result = repo.getTripLocation(tripId)
                if (result is ApiResult.Success) {
                    _driverLocation.value = result.data
                }
                delay(5_000L)
            }
        }
    }

    fun stopLocationPolling() {
        locationPollingJob?.cancel()
        locationPollingJob = null
        _driverLocation.value = null
    }

    fun resetCreateState() { _createState.value = null }
    fun resetCancelState() { _cancelState.value = null }
    fun resetRateState()   { _rateState.value   = null }
    fun resetQuoteState()  { _quoteState.value  = null }
}
