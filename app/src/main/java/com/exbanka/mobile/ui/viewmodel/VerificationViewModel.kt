package com.exbanka.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exbanka.mobile.data.repository.BankingRepository
import com.exbanka.mobile.model.ActivityItem
import com.exbanka.mobile.model.SessionUser
import com.exbanka.mobile.model.VerificationActionResult
import com.exbanka.mobile.model.VerificationFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VerificationUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val allItems: List<ActivityItem> = emptyList(),
    val filter: VerificationFilter = VerificationFilter.ALL,
    val selectedItem: ActivityItem? = null,
    val actionMessage: String? = null,
    val verificationCode: String? = null,
    val expiresAt: String? = null,
    val error: String? = null,
) {
    val filteredItems: List<ActivityItem>
        get() = when (filter) {
            VerificationFilter.ALL -> allItems
            VerificationFilter.PENDING -> allItems.filter { it.status == "u_obradi" }
            VerificationFilter.SUCCESSFUL -> allItems.filter { it.status == "uspesno" }
            VerificationFilter.FAILED -> allItems.filter { it.status == "stornirano" }
        }
}

class VerificationViewModel(
    private val bankingRepository: BankingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private var currentSession: SessionUser? = null

    fun load(session: SessionUser) {
        currentSession = session
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val accounts = bankingRepository.loadAccounts(session.clientId)
                val lookup = accounts.associateBy { it.id }
                bankingRepository.loadVerificationHistory(session.clientId, lookup)
            }.onSuccess { items ->
                val selected = _uiState.value.selectedItem
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allItems = items,
                        selectedItem = selected?.let { current ->
                            items.find { candidate ->
                                candidate.id == current.id && candidate.type == current.type
                            }
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Neuspešno učitavanje verifikacija.",
                    )
                }
            }
        }
    }

    fun updateFilter(filter: VerificationFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun openDetails(item: ActivityItem) {
        _uiState.update {
            it.copy(
                selectedItem = item,
                actionMessage = null,
                verificationCode = null,
                expiresAt = null,
                error = null,
            )
        }
    }

    fun closeDetails() {
        _uiState.update {
            it.copy(
                selectedItem = null,
                actionMessage = null,
                verificationCode = null,
                expiresAt = null,
                error = null,
            )
        }
    }

    fun showVerificationCode() {
        performAction(refreshAfter = false) { item -> bankingRepository.showVerificationCode(item) }
    }

    fun confirmSelected() {
        performAction(refreshAfter = true) { item -> bankingRepository.confirm(item) }
    }

    fun rejectSelected() {
        performAction(refreshAfter = true) { item -> bankingRepository.reject(item) }
    }

    private fun performAction(
        refreshAfter: Boolean,
        block: suspend (ActivityItem) -> VerificationActionResult,
    ) {
        val session = currentSession ?: return
        val item = _uiState.value.selectedItem ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, actionMessage = null, error = null) }
            runCatching {
                block(item)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        actionMessage = result.message,
                        verificationCode = result.verificationCode,
                        expiresAt = result.expiresAt,
                    )
                }
                if (refreshAfter) {
                    load(session)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        error = error.message ?: "Akcija nije uspela.",
                    )
                }
            }
        }
    }

    fun reset() {
        currentSession = null
        _uiState.value = VerificationUiState()
    }
}
