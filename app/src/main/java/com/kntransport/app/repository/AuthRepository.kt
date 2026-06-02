package com.kntransport.app.repository

import com.kntransport.app.network.*

class AuthRepository {

    private val api by lazy { ApiClient.service }
    private val tm  by lazy { ApiClient.getTokenManager() }

    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        tm.clearAll()
        val result = safeApiCall { api.login(LoginRequest(email, password)) }
        if (result is ApiResult.Success) {
            tm.saveToken(result.data.token)
            tm.saveRole(result.data.role)
        }
        return result
    }

    suspend fun register(name: String, email: String, phone: String, password: String): ApiResult<AuthResponse> {
        val result = safeApiCall { api.register(RegisterRequest(name, email, phone, password)) }
        if (result is ApiResult.Success) {
            tm.saveToken(result.data.token)
            tm.saveRole(result.data.role)
        }
        return result
    }

    fun logout() = tm.clearAll()

    fun isLoggedIn() = tm.isLoggedIn()
    fun getUserRole() = tm.getRole()
}
