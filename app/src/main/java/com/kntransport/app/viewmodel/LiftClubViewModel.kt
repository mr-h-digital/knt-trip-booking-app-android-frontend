package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.LiftClubDto
import com.kntransport.app.repository.LiftClubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LiftClubViewModel : ViewModel() {

    private val repo by lazy { LiftClubRepository() }

    private val _createState = MutableStateFlow<ApiResult<LiftClubDto>?>(null)
    val createState: StateFlow<ApiResult<LiftClubDto>?> = _createState

    fun createLiftClub(
        title        : String,
        pickupArea   : String,
        dropArea     : String,
        departureTime: String,
        returnTime   : String?,
        daysOfWeek   : List<String>,
        maxPassengers: Int,
        description  : String,
    ) {
        viewModelScope.launch {
            _createState.value = ApiResult.Loading
            _createState.value = repo.createLiftClub(
                title, pickupArea, dropArea, departureTime,
                returnTime, daysOfWeek, maxPassengers, description,
            )
        }
    }

    fun resetCreateState() { _createState.value = null }
}
