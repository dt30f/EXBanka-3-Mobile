package com.exbanka.mobile.data.repository

import com.exbanka.mobile.data.local.SessionStore
import com.exbanka.mobile.data.network.BankingApiService
import com.exbanka.mobile.data.network.ClientLoginRequest
import com.exbanka.mobile.model.SessionUser

class AuthRepository(
    private val api: BankingApiService,
    private val sessionStore: SessionStore,
) {
    fun currentSession(): SessionUser? = sessionStore.readSession()

    suspend fun login(email: String, password: String): SessionUser {
        val response = api.login(ClientLoginRequest(email = email.trim(), password = password))
        val fullName = listOfNotNull(response.client.ime, response.client.prezime)
            .joinToString(" ")
            .trim()

        return SessionUser(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            clientId = response.client.id.toLong(),
            email = response.client.email,
            fullName = fullName,
        ).also(sessionStore::saveSession)
    }

    fun logout() {
        sessionStore.clear()
    }
}
