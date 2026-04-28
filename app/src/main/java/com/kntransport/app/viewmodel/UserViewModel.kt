package com.kntransport.app.viewmodel

import android.net.Uri
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.UserDto
import com.kntransport.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
            _updateState.value = repo.updateProfile(name, email, phone)
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val file = uriToFile(context, uri) ?: run {
                _updateState.value = ApiResult.Error("Could not read image file")
                return@launch
            }
            _updateState.value = repo.uploadAvatar(file)
        }
    }

    fun uploadAvatarWithProfile(context: Context, uri: Uri, name: String, email: String, phone: String) {
        viewModelScope.launch {
            _updateState.value = ApiResult.Loading
            val file = uriToFile(context, uri) ?: run {
                _updateState.value = ApiResult.Error("Could not read image file")
                return@launch
            }
            // Upload avatar first, then apply any text field changes.
            val avatarResult = repo.uploadAvatar(file)
            if (avatarResult is ApiResult.Error) {
                _updateState.value = avatarResult
                return@launch
            }
            val savedUser = (avatarResult as? ApiResult.Success)?.data
            val textDirty = savedUser != null &&
                (savedUser.name != name || savedUser.email != email || savedUser.phone != phone)
            _updateState.value = if (textDirty) {
                repo.updateProfile(name, email, phone)
            } else {
                avatarResult
            }
        }
    }

    fun resetUpdateState() { _updateState.value = null }

    private fun uriToFile(context: Context, uri: Uri): File? = try {
        val input  = context.contentResolver.openInputStream(uri) ?: return null
        val file   = File(context.cacheDir, "avatar_upload.jpg")
        FileOutputStream(file).use { out -> input.copyTo(out) }
        file
    } catch (_: Exception) { null }
}
