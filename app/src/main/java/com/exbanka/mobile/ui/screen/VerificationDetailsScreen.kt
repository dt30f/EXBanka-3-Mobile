package com.exbanka.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exbanka.mobile.model.ActivityItem
import com.exbanka.mobile.model.ActivityType
import com.exbanka.mobile.ui.viewmodel.VerificationUiState
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VerificationDetailsScreen(
    modifier: Modifier = Modifier,
    item: ActivityItem,
    uiState: VerificationUiState,
    onShowCode: () -> Unit,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (item.type == ActivityType.PAYMENT) "Plaćanje" else "Transfer",
                        style = MaterialTheme.typography.titleMedium,
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

        uiState.verificationCode?.let { code ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Verifikacioni kod", style = MaterialTheme.typography.titleMedium)
                        Text(code, style = MaterialTheme.typography.headlineSmall)
                        uiState.expiresAt?.let { expiresAt ->
                            Text("Važi do: ${formatDate(expiresAt)}")
                        }
                        Text("Kod unesite na web aplikaciji.")
                    }
                }
            }
        }

        uiState.actionMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.primary) }
        }

        uiState.error?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }

        item {
            if (uiState.isActionLoading) {
                CircularProgressIndicator()
            } else if (item.status == "u_obradi") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onShowCode, modifier = Modifier.fillMaxWidth()) {
                        Text("Prikaži kod")
                    }
                    Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                        Text("Potvrdi odmah")
                    }
                    OutlinedButton(onClick = onIgnore, modifier = Modifier.fillMaxWidth()) {
                        Text("Ignoriši")
                    }
                }
            } else {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Nazad na listu")
                }
            }
        }
    }
}

private fun formatDate(value: String): String =
    runCatching {
        OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }.getOrDefault(value)
