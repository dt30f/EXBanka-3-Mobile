package com.exbanka.mobile.data.network

import com.google.gson.annotations.SerializedName

data class ClientLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class ClientLoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("client") val client: ClientDto,
)

data class ClientDto(
    @SerializedName("id") val id: String,
    @SerializedName("ime") val ime: String? = null,
    @SerializedName("prezime") val prezime: String? = null,
    @SerializedName("email") val email: String,
)

data class AccountDto(
    @SerializedName("id") val id: Long,
    @SerializedName("brojRacuna") val brojRacuna: String,
    @SerializedName("currencyKod") val currencyKod: String,
    @SerializedName("tip") val tip: String,
    @SerializedName("vrsta") val vrsta: String,
    @SerializedName("podvrsta") val podvrsta: String? = null,
    @SerializedName("stanje") val stanje: Double,
    @SerializedName("raspolozivoStanje") val raspolozivoStanje: Double,
    @SerializedName("naziv") val naziv: String,
    @SerializedName("status") val status: String,
)

data class PaymentsResponse(
    @SerializedName("payments") val payments: List<PaymentDto> = emptyList(),
)

data class PaymentDto(
    @SerializedName("id") val id: String,
    @SerializedName("racunPosiljaocaId") val racunPosiljaocaId: String,
    @SerializedName("racunPrimaocaBroj") val racunPrimaocaBroj: String,
    @SerializedName("iznos") val iznos: Double,
    @SerializedName("sifraPlacanja") val sifraPlacanja: String,
    @SerializedName("pozivNaBroj") val pozivNaBroj: String,
    @SerializedName("svrha") val svrha: String,
    @SerializedName("status") val status: String,
    @SerializedName("vremeTransakcije") val vremeTransakcije: String,
)

data class TransfersResponse(
    @SerializedName("transfers") val transfers: List<TransferDto> = emptyList(),
)

data class TransferDto(
    @SerializedName("id") val id: String,
    @SerializedName("racun_posiljaoca_id") val racunPosiljaocaId: String,
    @SerializedName("racun_primaoca_id") val racunPrimaocaId: String,
    @SerializedName("iznos") val iznos: Double,
    @SerializedName("valuta_iznosa") val valutaIznosa: String,
    @SerializedName("konvertovani_iznos") val konvertovaniIznos: Double,
    @SerializedName("kurs") val kurs: Double,
    @SerializedName("svrha") val svrha: String,
    @SerializedName("status") val status: String,
    @SerializedName("vreme_transakcije") val vremeTransakcije: String,
)

data class MobileActionRequest(
    @SerializedName("mode") val mode: String = "code",
)

data class PaymentMobileActionResponse(
    @SerializedName("payment") val payment: PaymentDto? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("verificationCode") val verificationCode: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("mode") val mode: String? = null,
)

data class TransferMobileActionResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("verificationCode") val verificationCode: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("mode") val mode: String? = null,
)
