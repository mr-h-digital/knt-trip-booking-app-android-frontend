package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val repo by lazy { TripRepository() }

    private val _trips = MutableStateFlow<ApiResult<List<TripBookingDto>>>(ApiResult.Loading)
    val trips: StateFlow<ApiResult<List<TripBookingDto>>> = _trips

    private val _createState = MutableStateFlow<ApiResult<TripBookingDto>?>(null)
    val createState: StateFlow<ApiResult<TripBookingDto>?> = _createState

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

    fun resetCreateState() { _createState.value = null }
}
