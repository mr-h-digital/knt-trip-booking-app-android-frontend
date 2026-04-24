package com.kntransport.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.NotificationDto
import com.kntransport.app.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val repo by lazy { NotificationRepository() }

    private val _notifications = MutableStateFlow<ApiResult<List<NotificationDto>>>(ApiResult.Loading)
    val notifications: StateFlow<ApiResult<List<NotificationDto>>> = _notifications

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = ApiResult.Loading
            val result = repo.getNotifications()
            _notifications.value = when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.content)
                is ApiResult.Error   -> result
                ApiResult.Loading    -> ApiResult.Loading
            }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            repo.markRead(id)
            updateLocal(id) { it.copy(read = true) }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repo.markAllRead()
            val current = (_notifications.value as? ApiResult.Success)?.data ?: return@launch
            _notifications.value = ApiResult.Success(current.map { it.copy(read = true) })
        }
    }

    fun dismissLocal(id: String) {
        val current = (_notifications.value as? ApiResult.Success)?.data ?: return
        _notifications.value = ApiResult.Success(current.filter { it.id != id })
    }

    private fun updateLocal(id: String, transform: (NotificationDto) -> NotificationDto) {
        val current = (_notifications.value as? ApiResult.Success)?.data ?: return
        _notifications.value = ApiResult.Success(current.map { if (it.id == id) transform(it) else it })
    }
}
