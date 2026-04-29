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

    // ── List ──────────────────────────────────────────────────────────────────

    private val _clubs = MutableStateFlow<ApiResult<List<LiftClubDto>>>(ApiResult.Loading)
    val clubs: StateFlow<ApiResult<List<LiftClubDto>>> = _clubs

    fun loadLiftClubs() {
        viewModelScope.launch {
            _clubs.value = ApiResult.Loading
            val result = repo.getLiftClubs()
            _clubs.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    private val _selectedClub = MutableStateFlow<ApiResult<LiftClubDto>?>(null)
    val selectedClub: StateFlow<ApiResult<LiftClubDto>?> = _selectedClub

    fun loadLiftClub(id: String) {
        viewModelScope.launch {
            _selectedClub.value = ApiResult.Loading
            _selectedClub.value = repo.getLiftClub(id)
        }
    }

    // ── My Subscriptions ─────────────────────────────────────────────────────

    private val _mySubscriptions = MutableStateFlow<ApiResult<List<LiftClubDto>>>(ApiResult.Loading)
    val mySubscriptions: StateFlow<ApiResult<List<LiftClubDto>>> = _mySubscriptions

    fun loadMySubscriptions() {
        viewModelScope.launch {
            _mySubscriptions.value = ApiResult.Loading
            _mySubscriptions.value = repo.getMySubscriptions()
        }
    }

    // ── My Clubs (created by me) ──────────────────────────────────────────────

    private val _myClubs = MutableStateFlow<ApiResult<List<LiftClubDto>>>(ApiResult.Loading)
    val myClubs: StateFlow<ApiResult<List<LiftClubDto>>> = _myClubs

    fun loadMyClubs() {
        viewModelScope.launch {
            _myClubs.value = ApiResult.Loading
            _myClubs.value = repo.getMyClubs()
        }
    }

    // ── Subscribe ─────────────────────────────────────────────────────────────

    private val _subscribeState = MutableStateFlow<ApiResult<Unit>?>(null)
    val subscribeState: StateFlow<ApiResult<Unit>?> = _subscribeState

    fun subscribe(id: String) {
        viewModelScope.launch {
            _subscribeState.value = ApiResult.Loading
            _subscribeState.value = repo.subscribe(id)
        }
    }

    fun resetSubscribeState() { _subscribeState.value = null }

    // ── Create ────────────────────────────────────────────────────────────────

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
