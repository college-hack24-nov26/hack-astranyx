package com.sktech.wastetrack.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.data.biometric.BiometricPreferencesManager
import com.sktech.wastetrack.domain.model.User
import com.sktech.wastetrack.domain.model.UserRole
import com.sktech.wastetrack.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentUser: User? = null,
    val isBiometricEnabled: Boolean = false,
    val isBiometricSupported: Boolean = true,
    val selectedLanguage: String = "English",
    val isNotificationsEnabled: Boolean = true,
    val isOfflineModeEnabled: Boolean = false,
    val isLoggingOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val biometricPrefs: BiometricPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isBiometricEnabled = biometricPrefs.getBiometricEnabled(),
            isBiometricSupported = biometricPrefs.isBiometricHardwareAvailable()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: User(
                id = "usr_kisan_01",
                name = "Ramesh Vitthalrao Patil",
                phone = "+91 98220 14829",
                role = UserRole.FARMER,
                organizationName = "Sinnar Agro Farmer Producer Co.",
                city = "Nashik"
            )
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        biometricPrefs.setBiometricEnabled(enabled)
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun selectLanguage(lang: String) {
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
            onSuccess()
        }
    }
}
