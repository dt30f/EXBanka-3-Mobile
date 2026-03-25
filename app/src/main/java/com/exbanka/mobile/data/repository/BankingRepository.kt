package com.exbanka.mobile.data.repository

import com.exbanka.mobile.data.network.AccountDto
import com.exbanka.mobile.data.network.BankingApiService
import com.exbanka.mobile.model.AccountSummary
import com.exbanka.mobile.model.ActivityItem
import com.exbanka.mobile.model.ActivityType
import com.exbanka.mobile.model.VerificationActionResult
import java.time.OffsetDateTime

class BankingRepository(
    private val api: BankingApiService,
) {
    suspend fun loadAccounts(clientId: Long): List<AccountSummary> =
        api.getAccountsByClient(clientId)
            .map(::toAccountSummary)
            .sortedBy { it.brojRacuna }

    suspend fun loadAccountActivity(accountId: Long, accountLookup: Map<Long, AccountSummary>): List<ActivityItem> {
        val payments = api.listPaymentsByAccount(accountId).payments.map { payment ->
            ActivityItem(
                id = payment.id,
                type = ActivityType.PAYMENT,
                amount = payment.iznos,
                date = payment.vremeTransakcije,
                status = payment.status,
                senderAccount = accountLookup[payment.racunPosiljaocaId.toLongOrNull()]?.brojRacuna
                    ?: "Račun ID ${payment.racunPosiljaocaId}",
                receiverAccount = payment.racunPrimaocaBroj,
                purpose = payment.svrha,
            )
        }

        val transfers = api.listTransfersByAccount(accountId).transfers.map { transfer ->
            ActivityItem(
                id = transfer.id,
                type = ActivityType.TRANSFER,
                amount = transfer.iznos,
                date = transfer.vremeTransakcije,
                status = transfer.status,
                senderAccount = accountLookup[transfer.racunPosiljaocaId.toLongOrNull()]?.brojRacuna
                    ?: "Račun ID ${transfer.racunPosiljaocaId}",
                receiverAccount = accountLookup[transfer.racunPrimaocaId.toLongOrNull()]?.brojRacuna
                    ?: "Račun ID ${transfer.racunPrimaocaId}",
                purpose = transfer.svrha,
            )
        }

        return (payments + transfers).sortedByDescending { parseDate(it.date) }
    }

    suspend fun loadVerificationHistory(
        clientId: Long,
        accountLookup: Map<Long, AccountSummary>,
    ): List<ActivityItem> {
        val payments = api.listPaymentsByClient(clientId).payments.map { payment ->
            ActivityItem(
                id = payment.id,
                type = ActivityType.PAYMENT,
                amount = payment.iznos,
                date = payment.vremeTransakcije,
                status = payment.status,
                senderAccount = accountLookup[payment.racunPosiljaocaId.toLongOrNull()]?.brojRacuna
                    ?: "Račun ID ${payment.racunPosiljaocaId}",
                receiverAccount = payment.racunPrimaocaBroj,
                purpose = payment.svrha,
            )
        }

        val transfers = api.listTransfersByClient(clientId).transfers.map { transfer ->
            ActivityItem(
                id = transfer.id,
                type = ActivityType.TRANSFER,
                amount = transfer.iznos,
                date = transfer.vremeTransakcije,
                status = transfer.status,
                senderAccount = accountLookup[transfer.racunPosiljaocaId.toLongOrNull()]?.brojRacuna
                    ?: "Račun ID ${transfer.racunPosiljaocaId}",
                receiverAccount = accountLookup[transfer.racunPrimaocaId.toLongOrNull()]?.brojRacuna
                    ?: "Račun ID ${transfer.racunPrimaocaId}",
                purpose = transfer.svrha,
            )
        }

        return (payments + transfers).sortedByDescending { parseDate(it.date) }
    }

    suspend fun showVerificationCode(item: ActivityItem): VerificationActionResult =
        when (item.type) {
            ActivityType.PAYMENT -> {
                val response = api.approvePayment(item.id, MODE_CODE)
                VerificationActionResult(
                    message = response.message ?: "Verifikacioni kod je spreman.",
                    verificationCode = response.verificationCode,
                    expiresAt = response.expiresAt,
                )
            }

            ActivityType.TRANSFER -> {
                val response = api.approveTransfer(item.id, MODE_CODE)
                VerificationActionResult(
                    message = response.message ?: "Verifikacioni kod je spreman.",
                    verificationCode = response.verificationCode,
                    expiresAt = response.expiresAt,
                )
            }
        }

    suspend fun confirm(item: ActivityItem): VerificationActionResult =
        when (item.type) {
            ActivityType.PAYMENT -> {
                val response = api.approvePayment(item.id, MODE_CONFIRM)
                VerificationActionResult(message = response.message ?: "Zahtev je potvrđen.")
            }

            ActivityType.TRANSFER -> {
                val response = api.approveTransfer(item.id, MODE_CONFIRM)
                VerificationActionResult(message = response.message ?: "Zahtev je potvrđen.")
            }
        }

    suspend fun reject(item: ActivityItem): VerificationActionResult =
        when (item.type) {
            ActivityType.PAYMENT -> {
                val response = api.rejectPayment(item.id)
                VerificationActionResult(message = response.message ?: "Zahtev je otkazan.")
            }

            ActivityType.TRANSFER -> {
                val response = api.rejectTransfer(item.id)
                VerificationActionResult(message = response.message ?: "Zahtev je otkazan.")
            }
        }

    private fun toAccountSummary(dto: AccountDto): AccountSummary =
        AccountSummary(
            id = dto.id,
            brojRacuna = dto.brojRacuna,
            currency = dto.currencyKod,
            name = dto.naziv.ifBlank { "${dto.tip} ${dto.vrsta}" },
            availableBalance = dto.raspolozivoStanje,
            bookedBalance = dto.stanje,
            status = dto.status,
        )

    private fun parseDate(value: String) = runCatching { OffsetDateTime.parse(value) }.getOrNull()

    private companion object {
        val MODE_CODE = com.exbanka.mobile.data.network.MobileActionRequest(mode = "code")
        val MODE_CONFIRM = com.exbanka.mobile.data.network.MobileActionRequest(mode = "confirm")
    }
}
