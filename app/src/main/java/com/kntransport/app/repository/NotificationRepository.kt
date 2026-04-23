package com.kntransport.app.repository

import com.kntransport.app.network.*

class NotificationRepository {

    private val api by lazy { ApiClient.service }

    suspend fun getNotifications(page: Int = 0) =
        safeApiCall { api.getNotifications(page) }

    suspend fun markRead(id: String) =
        safeApiCall { api.markNotificationRead(id) }

    suspend fun markAllRead() =
        safeApiCall { api.markAllNotificationsRead() }
}
