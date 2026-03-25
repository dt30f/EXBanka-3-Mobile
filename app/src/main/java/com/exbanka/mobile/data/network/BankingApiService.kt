package com.exbanka.mobile.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BankingApiService {
    @POST("auth/client/login")
    suspend fun login(@Body request: ClientLoginRequest): ClientLoginResponse

    @GET("accounts/client/{clientId}")
    suspend fun getAccountsByClient(@Path("clientId") clientId: Long): List<AccountDto>

    @GET("payments/client/{clientId}")
    suspend fun listPaymentsByClient(@Path("clientId") clientId: Long): PaymentsResponse

    @GET("payments/account/{accountId}")
    suspend fun listPaymentsByAccount(@Path("accountId") accountId: Long): PaymentsResponse

    @GET("transfers/client/{clientId}")
    suspend fun listTransfersByClient(@Path("clientId") clientId: Long): TransfersResponse

    @GET("transfers/account/{accountId}")
    suspend fun listTransfersByAccount(@Path("accountId") accountId: Long): TransfersResponse

    @POST("payments/{id}/approve")
    suspend fun approvePayment(
        @Path("id") id: String,
        @Body request: MobileActionRequest,
    ): PaymentMobileActionResponse

    @POST("payments/{id}/reject")
    suspend fun rejectPayment(
        @Path("id") id: String,
        @Body request: Map<String, String> = emptyMap(),
    ): PaymentMobileActionResponse

    @POST("transfers/{id}/approve")
    suspend fun approveTransfer(
        @Path("id") id: String,
        @Body request: MobileActionRequest,
    ): TransferMobileActionResponse

    @POST("transfers/{id}/reject")
    suspend fun rejectTransfer(
        @Path("id") id: String,
        @Body request: Map<String, String> = emptyMap(),
    ): TransferMobileActionResponse
}
