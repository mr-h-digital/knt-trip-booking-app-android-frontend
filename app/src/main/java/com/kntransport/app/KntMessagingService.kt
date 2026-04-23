package com.kntransport.app

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * FCM push notification handler.
 * Uncomment FirebaseMessagingService base class and imports once
 * google-services.json is added and the FCM plugin is enabled.
 *
 * To enable:
 *  1. Add google-services.json to /app
 *  2. Uncomment plugin("com.google.gms.google-services") in build.gradle.kts
 *  3. Add implementation("com.google.firebase:firebase-messaging-ktx:24.1.1") to dependencies
 *  4. Uncomment the FirebaseMessagingService extension and @Override methods below
 *  5. Register this service in AndroidManifest.xml (see comment in manifest)
 */

// import com.google.firebase.messaging.FirebaseMessagingService
// import com.google.firebase.messaging.RemoteMessage

// class KntMessagingService : FirebaseMessagingService() {
//
//     override fun onNewToken(token: String) {
//         // TODO: Send token to Spring Boot backend: POST /api/devices/token
//     }
//
//     override fun onMessageReceived(message: RemoteMessage) {
//         val type    = message.data["type"] ?: "GENERAL"
//         val title   = message.notification?.title ?: message.data["title"] ?: "K&T Transport"
//         val body    = message.notification?.body  ?: message.data["body"]  ?: ""
//         val channel = when (type) {
//             "QUOTE_RECEIVED"   -> KntApplication.CHANNEL_QUOTES
//             "TRIP_CONFIRMED",
//             "TRIP_UPDATE"      -> KntApplication.CHANNEL_TRIPS
//             "LIFT_CLUB_UPDATE" -> KntApplication.CHANNEL_LIFT_CLUBS
//             else               -> KntApplication.CHANNEL_GENERAL
//         }
//         showNotification(title, body, channel)
//     }
//
//     private fun showNotification(title: String, body: String, channel: String) {
//         val intent = Intent(this, MainActivity::class.java).apply {
//             flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//         }
//         val pendingIntent = PendingIntent.getActivity(
//             this, 0, intent,
//             PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
//         )
//         val notification = NotificationCompat.Builder(this, channel)
//             .setSmallIcon(R.mipmap.ic_launcher)
//             .setContentTitle(title)
//             .setContentText(body)
//             .setAutoCancel(true)
//             .setContentIntent(pendingIntent)
//             .build()
//
//         val manager = getSystemService(NotificationManager::class.java)
//         manager.notify(System.currentTimeMillis().toInt(), notification)
//     }
// }
