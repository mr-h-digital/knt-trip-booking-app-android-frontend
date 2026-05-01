package com.kntransport.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.android.libraries.places.api.Places
import com.kntransport.app.network.ApiClient

class KntApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
        if (!Places.isInitialized()) {
            val appInfo: ApplicationInfo = packageManager.getApplicationInfo(
                packageName, PackageManager.GET_META_DATA
            )
            val mapsKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
            Places.initializeWithNewPlacesApiEnabled(this, mapsKey)
        }
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_TRIPS,
                "Trip Updates",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Notifications for trip status changes and driver assignments" },

            NotificationChannel(
                CHANNEL_QUOTES,
                "Quotes",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Notifications when a quote is received" },

            NotificationChannel(
                CHANNEL_LIFT_CLUBS,
                "Lift Club Updates",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Lift club quota and subscription updates" },

            NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "General app announcements" },
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    companion object {
        const val CHANNEL_TRIPS      = "knt_trips"
        const val CHANNEL_QUOTES     = "knt_quotes"
        const val CHANNEL_LIFT_CLUBS = "knt_lift_clubs"
        const val CHANNEL_GENERAL    = "knt_general"
    }
}
