package com.exbanka.mobile.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
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
import com.exbanka.mobile.model.SessionUser
import com.exbanka.mobile.model.VerificationFilter
import com.exbanka.mobile.ui.viewmodel.VerificationUiState
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VerificationListScreen(
    modifier: Modifier = Modifier,
    session: SessionUser,
    uiState: VerificationUiState,
    onRefresh: () -> Unit,
    onFilterChange: (VerificationFilter) -> Unit,
    onItemClick: (ActivityItem) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Verifikacija", style = MaterialTheme.typography.titleMedium)
                    Text("Klijent: ${session.email}")
                    OutlinedButton(onClick = onRefresh) {
                        Text("Osveži zahteve")
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VerificationFilter.entries.forEach { filter ->
                    AssistChip(
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }
        }

        if (uiState.isLoading && uiState.allItems.isEmpty()) {
            item { CircularProgressIndicator() }
        }

        if (!uiState.isLoading && uiState.filteredItems.isEmpty()) {
            item { Text("Nema verifikacionih zahteva za izabrani filter.") }
        }

        items(uiState.filteredItems, key = { "${it.type}-${it.id}" }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) },
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (item.type == ActivityType.PAYMENT) "Plaćanje" else "Transfer",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text("Iznos: %.2f".format(item.amount))
                    Text("Datum: ${formatDate(item.date)}")
                    Text("Status: ${item.status}")
                }
            }
        }

        uiState.error?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
    }
}

private fun formatDate(value: String): String =
    runCatching {
        OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }.getOrDefault(value)
