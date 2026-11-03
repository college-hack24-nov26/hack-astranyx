package com.sktech.wastetrack.ui.biometric

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.sktech.wastetrack.data.biometric.BiometricAuthManager
import com.sktech.wastetrack.domain.model.biometric.BiometricAuthResult
import com.sktech.wastetrack.domain.model.biometric.BiometricPromptConfig

class BiometricPromptLauncher(
    private val activity: FragmentActivity?,
    private val biometricAuthManager: BiometricAuthManager
) {

    fun isAvailable(allowDeviceCredential: Boolean = true): Boolean {
        return biometricAuthManager.isBiometricAvailable(allowDeviceCredential)
    }

    fun authenticate(
        config: BiometricPromptConfig,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        onResult: (BiometricAuthResult) -> Unit
    ) {
        if (activity == null) {
            onResult(BiometricAuthResult.Error(-1, "Activity not available for BiometricPrompt"))
            return
        }
        biometricAuthManager.authenticate(activity, config, cryptoObject, onResult)
    }

    suspend fun authenticateSuspend(
        config: BiometricPromptConfig,
        cryptoObject: BiometricPrompt.CryptoObject? = null
    ): BiometricAuthResult {
        if (activity == null) {
            return BiometricAuthResult.Error(-1, "Activity not available for BiometricPrompt")
        }
        return biometricAuthManager.authenticateSuspend(activity, config, cryptoObject)
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? {
    return when (this) {
        is FragmentActivity -> this
        is ContextWrapper -> baseContext.findFragmentActivity()
        else -> null
    }
}

@Composable
fun rememberBiometricPromptLauncher(): BiometricPromptLauncher {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val biometricAuthManager = remember(context) { BiometricAuthManager(context.applicationContext) }

    return remember(activity, biometricAuthManager) {
        BiometricPromptLauncher(activity, biometricAuthManager)
    }
}
