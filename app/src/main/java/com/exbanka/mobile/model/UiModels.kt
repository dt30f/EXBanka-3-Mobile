package com.exbanka.mobile.model

data class SessionUser(
    val accessToken: String,
    val refreshToken: String,
    val clientId: Long,
    val email: String,
    val fullName: String,
)

data class AccountSummary(
    val id: Long,
    val brojRacuna: String,
    val currency: String,
    val name: String,
    val availableBalance: Double,
    val bookedBalance: Double,
    val status: String,
)

enum class ActivityType {
    PAYMENT,
    TRANSFER,
}

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val amount: Double,
    val date: String,
    val status: String,
    val senderAccount: String,
    val receiverAccount: String,
    val purpose: String,
)

enum class VerificationFilter(val label: String) {
    ALL("Sve"),
    PENDING("Na čekanju"),
    SUCCESSFUL("Uspešno"),
    FAILED("Neuspešno"),
}

data class VerificationActionResult(
    val message: String,
    val verificationCode: String? = null,
    val expiresAt: String? = null,
)
