package com.kntransport.app.repository

import com.kntransport.app.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepository {

    private val api by lazy { ApiClient.service }

    suspend fun getProfile() = safeApiCall { api.getProfile() }

    suspend fun updateProfile(name: String, email: String, phone: String) =
        safeApiCall { api.updateProfile(UpdateProfileRequest(name, email, phone)) }

    suspend fun acceptTerms() = safeApiCall { api.acceptTerms() }

    suspend fun uploadAvatar(file: File): ApiResult<UserDto> {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("avatar", "${file.nameWithoutExtension}.jpg", requestBody)
        return safeApiCall { api.uploadAvatar(part) }
    }
}
