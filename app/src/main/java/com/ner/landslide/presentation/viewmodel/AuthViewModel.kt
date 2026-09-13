package com.ner.landslide.presentation.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ner.landslide.domain.model.User
import com.ner.landslide.domain.model.UserRole
import com.ner.landslide.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val selectedRole: UserRole = UserRole.CITIZEN,
    val registeredUser: User? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var isSubmitting = false

    fun onRoleSelected(role: UserRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun signInWithEmail(email: String, password: String) {
        if (isSubmitting) return

        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please provide both email and password.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address.")
            return
        }

        isSubmitting = true
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = userRepository.authenticateUser(cleanEmail, cleanPassword)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredUser = result.getOrNull(),
                        error = null
                    )
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Authentication failed."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = e.localizedMessage ?: "Unexpected authentication error."
                )
            } finally {
                isSubmitting = false
            }
        }
    }

    fun registerWithEmail(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: UserRole = _uiState.value.selectedRole
    ) {
        if (isSubmitting) return

        val cleanName = name.trim()
        val cleanEmail = email.trim()

        if (cleanName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Full Name is required.")
            return
        }
        if (cleanName.length < 2) {
            _uiState.value = _uiState.value.copy(error = "Full Name must be at least 2 characters.")
            return
        }
        if (cleanEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email address is required.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address format.")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters long.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Password and Confirm Password do not match.")
            return
        }

        isSubmitting = true
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = userRepository.registerUser(cleanName, cleanEmail, password, role)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredUser = result.getOrNull(),
                        error = null
                    )
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Registration failed."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = e.localizedMessage ?: "Unexpected registration error."
                )
            } finally {
                isSubmitting = false
            }
        }
    }

    fun setValidationError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = AuthUiState()
        isSubmitting = false
    }
}
