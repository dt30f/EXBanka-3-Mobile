package com.exbanka.mobile

import android.content.Context
import com.exbanka.mobile.data.local.SessionStore
import com.exbanka.mobile.data.network.NetworkModule
import com.exbanka.mobile.data.repository.AuthRepository
import com.exbanka.mobile.data.repository.BankingRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val sessionStore = SessionStore(appContext)
    private val api = NetworkModule.createApi(sessionStore)

    val authRepository = AuthRepository(api, sessionStore)
    val bankingRepository = BankingRepository(api)
}
