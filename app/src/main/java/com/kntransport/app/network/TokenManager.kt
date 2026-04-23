package com.kntransport.app.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the JWT token securely using EncryptedSharedPreferences.
 * Call from Application or provide via DI.
 */
class TokenManager(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "knt_secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?       = prefs.getString(KEY_TOKEN, null)
    fun clearToken()              = prefs.edit().remove(KEY_TOKEN).apply()

    fun saveRole(role: String)    = prefs.edit().putString(KEY_ROLE, role).apply()
    fun getRole(): String?        = prefs.getString(KEY_ROLE, null)
    fun clearRole()               = prefs.edit().remove(KEY_ROLE).apply()

    fun clearAll() { clearToken(); clearRole() }
    fun isLoggedIn() = getToken() != null

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_ROLE  = "user_role"
    }
}
