package com.exbanka.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import com.exbanka.mobile.model.SessionUser

class SessionStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(session: SessionUser) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putLong(KEY_CLIENT_ID, session.clientId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_FULL_NAME, session.fullName)
            .apply()
    }

    fun readSession(): SessionUser? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
        val clientId = prefs.getLong(KEY_CLIENT_ID, 0L)
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val fullName = prefs.getString(KEY_FULL_NAME, "") ?: ""
        if (clientId == 0L || email.isBlank()) {
            return null
        }

        return SessionUser(
            accessToken = token,
            refreshToken = refreshToken,
            clientId = clientId,
            email = email,
            fullName = fullName,
        )
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "exbanka_mobile_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_EMAIL = "email"
        const val KEY_FULL_NAME = "full_name"
    }
}
