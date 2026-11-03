package com.sktech.wastetrack.data.biometric

import android.content.Context
import android.content.SharedPreferences
import com.sktech.wastetrack.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val biometricAuthManager: BiometricAuthManager
) {
    companion object {
        private const val PREFS_NAME = Constants.PREFERENCES_NAME
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_BIOMETRIC_DISPATCH_REQUIRED = "biometric_dispatch_required"
        private const val KEY_BIOMETRIC_LOGIN_ENABLED = "biometric_login_enabled"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isBiometricDispatchRequired = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_DISPATCH_REQUIRED, false))
    val isBiometricDispatchRequired: StateFlow<Boolean> = _isBiometricDispatchRequired.asStateFlow()

    private val _isBiometricLoginEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_LOGIN_ENABLED, false))
    val isBiometricLoginEnabled: StateFlow<Boolean> = _isBiometricLoginEnabled.asStateFlow()

    fun isBiometricHardwareAvailable(): Boolean {
        return biometricAuthManager.isBiometricAvailable()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
        if (!enabled) {
            setBiometricDispatchRequired(false)
            setBiometricLoginEnabled(false)
        }
    }

    fun getBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricDispatchRequired(required: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_DISPATCH_REQUIRED, required).apply()
        _isBiometricDispatchRequired.value = required
    }

    fun getBiometricDispatchRequired(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_DISPATCH_REQUIRED, false)
    }

    fun setBiometricLoginEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOGIN_ENABLED, enabled).apply()
        _isBiometricLoginEnabled.value = enabled
    }

    fun getBiometricLoginEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_LOGIN_ENABLED, false)
    }
}
