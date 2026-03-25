package com.exbanka.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exbanka.mobile.model.ActivityItem
import com.exbanka.mobile.model.SessionUser
import com.exbanka.mobile.ui.viewmodel.HomeUiState
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    session: SessionUser,
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onSelectAccount: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Klijent", style = MaterialTheme.typography.titleMedium)
                    Text(session.fullName.ifBlank { "Ime nije dostupno" })
                    Text(session.email, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onRefresh) {
                        Text("Osveži podatke")
                    }
                }
            }
        }

        item { Text("Računi", style = MaterialTheme.typography.titleMedium) }

        if (uiState.isLoading && uiState.accounts.isEmpty()) {
            item { CircularProgressIndicator() }
        }

        items(uiState.accounts, key = { it.id }) { account ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = uiState.selectedAccountId == account.id,
                        onClick = { onSelectAccount(account.id) },
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(account.name, style = MaterialTheme.typography.titleSmall)
                        Text(account.brojRacuna, style = MaterialTheme.typography.bodySmall)
                        Text("Raspoloživo: %.2f %s".format(account.availableBalance, account.currency))
                        Text("Status: ${account.status}")
                    }
                }
            }
        }

        item { Text("Osnovne transakcije", style = MaterialTheme.typography.titleMedium) }

        if (uiState.isLoading && uiState.accounts.isNotEmpty()) {
            item { CircularProgressIndicator() }
        }

        if (!uiState.isLoading && uiState.accountActivity.isEmpty()) {
            item { Text("Nema transakcija za izabrani račun.") }
        }

        items(uiState.accountActivity, key = { "${it.type}-${it.id}" }) { activity ->
            ActivityCard(activity)
        }

        uiState.error?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ActivityCard(item: ActivityItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (item.type.name == "PAYMENT") "Plaćanje" else "Transfer",
                style = MaterialTheme.typography.titleSmall,
            )
            Text("Iznos: %.2f".format(item.amount))
            Text("Status: ${item.status}")
            Text("Datum: ${formatDate(item.date)}")
            Text("Pošiljalac: ${item.senderAccount}")
            Text("Primalac: ${item.receiverAccount}")
            Text("Svrha: ${item.purpose.ifBlank { "-" }}")
        }
    }
}

private fun formatDate(value: String): String =
    runCatching {
        OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }.getOrDefault(value)
