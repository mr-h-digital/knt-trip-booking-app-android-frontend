package com.kntransport.app.network

import retrofit2.Response

/** Wraps every API call result — success, error, or network failure. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T)       : ApiResult<T>()
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>()
    data object Loading                      : ApiResult<Nothing>()
}

/** Execute a suspend API call and wrap in ApiResult. */
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) ApiResult.Success(body)
            else ApiResult.Error("Empty response body", response.code())
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
            ApiResult.Error(errorMsg, response.code())
        }
    } catch (e: java.net.UnknownHostException) {
        ApiResult.Error("No internet connection")
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error("Request timed out")
    } catch (e: Exception) {
        ApiResult.Error(e.localizedMessage ?: "Unexpected error")
    }
}
