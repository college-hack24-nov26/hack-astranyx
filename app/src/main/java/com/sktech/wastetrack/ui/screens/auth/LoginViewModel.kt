package com.sktech.wastetrack.ui.screens.auth

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.sktech.wastetrack.R
import com.sktech.wastetrack.data.biometric.BiometricAuthManager
import com.sktech.wastetrack.data.biometric.BiometricPreferencesManager
import com.sktech.wastetrack.domain.model.User
import com.sktech.wastetrack.domain.model.UserRole
import com.sktech.wastetrack.domain.repository.IAuthRepository
import com.sktech.wastetrack.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class LoginState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val isSuccess: Boolean = false,
    val needsProfileSetup: Boolean = false,
    val selectedRole: UserRole = UserRole.FARMER,
    val detectedUser: User? = null,
    val isExistingUser: Boolean = false,
    val isRoleLocked: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isBiometricSupported: Boolean = false,
    val lastGeneratedOtp: String? = null,
    val error: UiText? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val biometricPrefs: BiometricPreferencesManager,
    private val biometricAuthManager: BiometricAuthManager,
    private val freeOtpGateway: com.sktech.wastetrack.data.remote.api.FreeOtpGateway
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(
        LoginState(
            isBiometricEnabled = biometricPrefs.getBiometricEnabled(),
            isBiometricSupported = biometricAuthManager.isBiometricAvailable()
        )
    )
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        val currentSelectedRole = authRepository.getSelectedRole()
        _state.update { it.copy(selectedRole = currentSelectedRole) }

        viewModelScope.launch {
            biometricPrefs.isBiometricEnabled.collect { isEnabled ->
                _state.update {
                    it.copy(
                        isBiometricEnabled = isEnabled,
                        isBiometricSupported = biometricAuthManager.isBiometricAvailable()
                    )
                }
            }
        }
    }

    fun resetState() {
        storedVerificationId = null
        resendToken = null
        _state.update {
            LoginState(
                phoneNumber = "",
                otpCode = "",
                isLoading = false,
                isOtpSent = false,
                isSuccess = false,
                needsProfileSetup = false,
                selectedRole = authRepository.getSelectedRole(),
                isBiometricEnabled = biometricPrefs.getBiometricEnabled(),
                isBiometricSupported = biometricAuthManager.isBiometricAvailable(),
                error = null
            )
        }
    }

    fun onChangeNumber() {
        storedVerificationId = null
        resendToken = null
        _state.update {
            it.copy(
                isOtpSent = false,
                otpCode = "",
                isLoading = false,
                error = null
            )
        }
    }

    fun onPhoneNumberChanged(number: String) {
        _state.update { it.copy(phoneNumber = number, error = null) }

        val digits = number.filter { it.isDigit() }
        if (digits.length >= 10) {
            viewModelScope.launch {
                val formatted = if (number.startsWith("+")) number else "+91$digits"
                val existingUser = authRepository.getUserByPhone(formatted)
                    ?: authRepository.getUserByPhone(digits)
                    ?: authRepository.getUserByPhone(digits.takeLast(10))

                if (existingUser != null && existingUser.isProfileComplete) {
                    authRepository.setSelectedRole(existingUser.role)
                    _state.update {
                        it.copy(
                            detectedUser = existingUser,
                            selectedRole = existingUser.role,
                            isExistingUser = true,
                            isRoleLocked = true,
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            detectedUser = null,
                            isExistingUser = false,
                            isRoleLocked = false
                        )
                    }
                }
            }
        } else {
            _state.update {
                it.copy(
                    detectedUser = null,
                    isExistingUser = false,
                    isRoleLocked = false
                )
            }
        }
    }

    fun onOtpCodeChanged(code: String) {
        _state.update { it.copy(otpCode = code, error = null) }
    }

    fun onRoleSelected(role: UserRole) {
        val s = _state.value
        if (s.isRoleLocked && s.detectedUser != null && s.detectedUser.role != role) {
            _state.update {
                it.copy(
                    error = UiText.StringResource(R.string.login_err_phone_locked_role, s.detectedUser.role.name)
                )
            }
            return
        }

        _state.update { it.copy(selectedRole = role, error = null) }
        viewModelScope.launch {
            authRepository.setSelectedRole(role)
        }
    }

    fun performBiometricLogin(role: UserRole = state.value.selectedRole) {
        _state.update { it.copy(isLoading = true, selectedRole = role, error = null) }
        val phone = state.value.phoneNumber.trim()
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (phone.isNotBlank()) {
                        authRepository.setLastEnteredPhone(phone)
                    }
                    val existing = authRepository.getUserByPhone(phone)
                    val effectiveRole = existing?.role ?: role
                    authRepository.setSelectedRole(effectiveRole)
                    val isComplete = authRepository.isProfileComplete()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            selectedRole = effectiveRole,
                            isSuccess = isComplete,
                            needsProfileSetup = !isComplete,
                            error = null
                        )
                    }
                }
            }
    }

    fun quickDemoLogin(role: UserRole) {
        _state.update { it.copy(isLoading = true, selectedRole = role, error = null) }
        val phone = state.value.phoneNumber.trim()
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (phone.isNotBlank()) {
                        authRepository.setLastEnteredPhone(phone)
                    }
                    val existing = authRepository.getUserByPhone(phone)
                    val effectiveRole = existing?.role ?: role
                    authRepository.setSelectedRole(effectiveRole)
                    val isComplete = authRepository.isProfileComplete()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            selectedRole = effectiveRole,
                            isSuccess = isComplete,
                            needsProfileSetup = !isComplete,
                            error = null
                        )
                    }
                }
            }
    }

    fun sendOtp(
        context: Context,
        mode: com.sktech.wastetrack.data.remote.api.FreeOtpGateway.DeliveryMode = com.sktech.wastetrack.data.remote.api.FreeOtpGateway.DeliveryMode.BANNER_ONLY
    ) {
        val number = state.value.phoneNumber.trim()
        val digitsOnly = number.filter { it.isDigit() }

        val formattedNumber = when {
            number.startsWith("+") -> number.replace(" ", "")
            digitsOnly.length == 10 -> "+91$digitsOnly"
            digitsOnly.length > 10 -> "+$digitsOnly"
            else -> {
                _state.update { it.copy(error = UiText.StringResource(R.string.login_err_invalid_phone)) }
                return
            }
        }

        val detected = state.value.detectedUser
        if (detected != null && detected.isProfileComplete && detected.role != state.value.selectedRole) {
            _state.update {
                it.copy(
                    error = UiText.StringResource(R.string.login_err_role_mismatch, detected.role.name, detected.role.name)
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = freeOtpGateway.sendOtp(formattedNumber, mode)

            if (result.isSuccess) {
                val otpCode = result.getOrNull()
                _state.update {
                    it.copy(
                        isLoading = false,
                        isOtpSent = true,
                        otpCode = "",
                        lastGeneratedOtp = otpCode,
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage?.let { msg -> UiText.DynamicString(msg) }
                            ?: UiText.StringResource(R.string.login_err_failed_send_otp)
                    )
                }
            }
        }
    }

    fun verifyOtp() {
        val code = state.value.otpCode.trim()
        val phone = state.value.phoneNumber.trim()

        if (code.length !in 4..6) {
            _state.update { it.copy(error = UiText.StringResource(R.string.login_err_enter_valid_otp)) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val isGatewayValid = freeOtpGateway.verifySmsOtp(phone, code)
            if (isGatewayValid) {
                if (auth.currentUser == null) {
                    try {
                        auth.signInAnonymously().await()
                    } catch (e: Exception) {
                    }
                }
                onSuccessfulVerification(phone)
                return@launch
            }

            val verificationId = storedVerificationId
            if (!verificationId.isNullOrBlank()) {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                signInWithCredential(credential)
                return@launch
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    error = UiText.StringResource(R.string.login_err_invalid_otp)
                )
            }
        }
    }

    private fun onSuccessfulVerification(phone: String) {
        viewModelScope.launch {
            if (phone.isNotBlank()) {
                authRepository.setLastEnteredPhone(phone)
            }
            val existing = authRepository.getUserByPhone(phone)
            val finalRole = existing?.role ?: state.value.selectedRole
            authRepository.setSelectedRole(finalRole)
            val isComplete = authRepository.isProfileComplete()
            _state.update {
                it.copy(
                    isLoading = false,
                    selectedRole = finalRole,
                    isSuccess = isComplete,
                    needsProfileSetup = !isComplete,
                    error = null
                )
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        val phone = state.value.phoneNumber.trim()
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccessfulVerification(phone)
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = task.exception?.localizedMessage?.let { msg -> UiText.DynamicString(msg) }
                                ?: UiText.StringResource(R.string.login_err_invalid_otp)
                        )
                    }
                }
            }
    }
}
