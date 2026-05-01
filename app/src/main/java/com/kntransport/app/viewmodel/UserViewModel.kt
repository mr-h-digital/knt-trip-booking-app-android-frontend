package com.kntransport.app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.UserDto
import com.kntransport.app.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class UserViewModel : ViewModel() {

    private val repo by lazy { UserRepository() }

    private val _profile = MutableStateFlow<ApiResult<UserDto>?>(null)
    val profile: StateFlow<ApiResult<UserDto>?> = _profile

    private val _updateState = MutableStateFlow<ApiResult<UserDto>?>(null)
    val updateState: StateFlow<ApiResult<UserDto>?> = _updateState

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = ApiResult.Loading
            _profile.value = repo.getProfile()
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val result = repo.updateProfile(name, email, phone)
            _updateState.value = result
            // Keep _profile in sync so the avatar/name update is visible immediately on back-navigation
            if (result is ApiResult.Success) _profile.value = result
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val file = uriToFile(context, uri) ?: run {
                _updateState.value = ApiResult.Error("Could not read image file")
                return@launch
            }
            val result = repo.uploadAvatar(file)
            _updateState.value = result
            if (result is ApiResult.Success) _profile.value = result
        }
    }

    fun uploadAvatarWithProfile(context: Context, uri: Uri, name: String, email: String, phone: String) {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val file = uriToFile(context, uri) ?: run {
                _updateState.value = ApiResult.Error("Could not read image file")
                return@launch
            }
            // Upload avatar first, then apply any text field changes
            val avatarResult = repo.uploadAvatar(file)
            if (avatarResult is ApiResult.Error) {
                _updateState.value = avatarResult
                return@launch
            }
            val savedUser = (avatarResult as? ApiResult.Success)?.data
            val textDirty = savedUser != null &&
                (savedUser.name != name || savedUser.email != email || savedUser.phone != phone)
            val finalResult = if (textDirty) repo.updateProfile(name, email, phone) else avatarResult
            _updateState.value = finalResult
            // Always push the latest user back into _profile so all screens update immediately
            if (finalResult is ApiResult.Success) _profile.value = finalResult
            else if (avatarResult is ApiResult.Success) _profile.value = avatarResult
        }
    }

    fun acceptTerms() {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val result = repo.acceptTerms()
            _updateState.value = result
            if (result is ApiResult.Success) _profile.value = result
        }
    }

    fun resetUpdateState() { _updateState.value = null }

    private suspend fun uriToFile(context: Context, uri: Uri): File? =
        withContext(Dispatchers.IO) {
            try {
                val input = context.contentResolver.openInputStream(uri) ?: return@withContext null

                // Decode → scale down to max 1024px on longest side → compress to JPEG 85%
                val original = BitmapFactory.decodeStream(input)
                input.close()

                val maxDim   = 1024
                val scaled   = if (original.width > maxDim || original.height > maxDim) {
                    val ratio  = maxDim.toFloat() / maxOf(original.width, original.height)
                    Bitmap.createScaledBitmap(
                        original,
                        (original.width  * ratio).toInt(),
                        (original.height * ratio).toInt(),
                        true,
                    ).also { if (it !== original) original.recycle() }
                } else original

                val out  = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                scaled.recycle()

                val file = File(context.cacheDir, "avatar_upload.jpg")
                FileOutputStream(file).use { it.write(out.toByteArray()) }
                file
            } catch (_: Exception) { null }
        }
}
