package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.AuthResponse
import com.kntransport.app.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo by lazy { AuthRepository() }

    private val _loginState  = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val loginState: StateFlow<ApiResult<AuthResponse>?> = _loginState

    private val _signUpState = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val signUpState: StateFlow<ApiResult<AuthResponse>?> = _signUpState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ApiResult.Loading
            _loginState.value = repo.login(email, password)
        }
    }

    fun register(name: String, email: String, phone: String, password: String) {
        viewModelScope.launch {
            _signUpState.value = ApiResult.Loading
            _signUpState.value = repo.register(name, email, phone, password)
        }
    }

    fun logout() = repo.logout()

    fun resetLoginState()  { _loginState.value  = null }
    fun resetSignUpState() { _signUpState.value = null }

    /** Route based on role after successful login. */
    fun resolveDestination(role: String) = when (role.uppercase()) {
        "DRIVER" -> "driver_home"
        "ADMIN"  -> "admin_home"
        else     -> "home"
    }
}
