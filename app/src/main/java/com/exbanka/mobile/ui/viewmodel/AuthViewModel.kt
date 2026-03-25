package com.exbanka.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exbanka.mobile.data.repository.AuthRepository
import com.exbanka.mobile.model.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isBootstrapping: Boolean = true,
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val session: SessionUser? = null,
    val error: String? = null,
)

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                isBootstrapping = false,
                session = authRepository.currentSession(),
            )
        }
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val snapshot = _uiState.value
        if (snapshot.email.isBlank() || snapshot.password.isBlank()) {
            _uiState.update { it.copy(error = "Email i lozinka su obavezni.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                authRepository.login(snapshot.email, snapshot.password)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        session = session,
                        error = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Prijava nije uspela.",
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update {
            it.copy(
                session = null,
                password = "",
                error = null,
            )
        }
    }
}
