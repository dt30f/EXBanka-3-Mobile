package com.exbanka.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exbanka.mobile.data.repository.BankingRepository
import com.exbanka.mobile.model.AccountSummary
import com.exbanka.mobile.model.ActivityItem
import com.exbanka.mobile.model.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val accounts: List<AccountSummary> = emptyList(),
    val selectedAccountId: Long? = null,
    val accountActivity: List<ActivityItem> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(
    private val bankingRepository: BankingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun load(session: SessionUser) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val accounts = bankingRepository.loadAccounts(session.clientId)
                val selectedId = _uiState.value.selectedAccountId ?: accounts.firstOrNull()?.id
                val lookup = accounts.associateBy { it.id }
                val activity = selectedId?.let { bankingRepository.loadAccountActivity(it, lookup) }.orEmpty()

                HomeUiState(
                    isLoading = false,
                    accounts = accounts,
                    selectedAccountId = selectedId,
                    accountActivity = activity,
                )
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Neuspešno učitavanje početne stranice.",
                    )
                }
            }
        }
    }

    fun selectAccount(accountId: Long) {
        val accounts = _uiState.value.accounts
        if (accounts.isEmpty()) return

        viewModelScope.launch {
            val lookup = accounts.associateBy { it.id }
            _uiState.update { it.copy(isLoading = true, selectedAccountId = accountId, error = null) }
            runCatching {
                bankingRepository.loadAccountActivity(accountId, lookup)
            }.onSuccess { activity ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedAccountId = accountId,
                        accountActivity = activity,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Neuspešno učitavanje aktivnosti za račun.",
                    )
                }
            }
        }
    }

    fun reset() {
        _uiState.value = HomeUiState()
    }
}
