package com.sktech.wastetrack.data.biometric

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sktech.wastetrack.domain.model.biometric.BiometricAuthResult
import com.sktech.wastetrack.domain.model.biometric.BiometricAvailability
import com.sktech.wastetrack.domain.model.biometric.BiometricPromptConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DEFAULT_KEY_ALIAS = "wastetrack_biometric_key"
        private const val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
    }

    private val biometricManager = BiometricManager.from(context)

    fun checkBiometricAvailability(allowDeviceCredential: Boolean = true): BiometricAvailability {
        val authenticators = if (allowDeviceCredential) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SecurityUpdateRequired
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.Unsupported
            else -> BiometricAvailability.Error(-1, "Biometric authentication is not available")
        }
    }

    fun isBiometricAvailable(allowDeviceCredential: Boolean = true): Boolean {
        return checkBiometricAvailability(allowDeviceCredential).isAvailable
    }

    fun authenticate(
        activity: FragmentActivity,
        config: BiometricPromptConfig,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        onResult: (BiometricAuthResult) -> Unit
    ): BiometricPrompt? {
        val availability = checkBiometricAvailability(config.allowDeviceCredential)
        if (!availability.isAvailable) {
            onResult(BiometricAuthResult.Unavailable(availability))
            return null
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(BiometricAuthResult.Success(result.cryptoObject))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onResult(BiometricAuthResult.Cancelled)
                } else {
                    onResult(BiometricAuthResult.Error(errorCode, errString.toString()))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(BiometricAuthResult.Failed)
            }
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setConfirmationRequired(config.requireConfirmation)

        config.subtitle?.let { promptInfoBuilder.setSubtitle(it) }
        config.description?.let { promptInfoBuilder.setDescription(it) }

        if (config.allowDeviceCredential) {
            promptInfoBuilder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            promptInfoBuilder.setNegativeButtonText(config.negativeButtonText)
        }

        val promptInfo = promptInfoBuilder.build()
        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        if (cryptoObject != null) {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
        return biometricPrompt
    }

    suspend fun authenticateSuspend(
        activity: FragmentActivity,
        config: BiometricPromptConfig,
        cryptoObject: BiometricPrompt.CryptoObject? = null
    ): BiometricAuthResult = suspendCancellableCoroutine { continuation ->
        val prompt = authenticate(activity, config, cryptoObject) { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        continuation.invokeOnCancellation {
            prompt?.cancelAuthentication()
        }
    }

    fun getOrCreateSecretKey(keyAlias: String = DEFAULT_KEY_ALIAS): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(keyAlias)) {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
            )
        }

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    fun createCryptoObject(
        mode: Int = Cipher.ENCRYPT_MODE,
        keyAlias: String = DEFAULT_KEY_ALIAS,
        iv: ByteArray? = null
    ): BiometricPrompt.CryptoObject? {
        return try {
            val key = getOrCreateSecretKey(keyAlias)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            if (mode == Cipher.DECRYPT_MODE && iv != null) {
                cipher.init(mode, key, javax.crypto.spec.IvParameterSpec(iv))
            } else {
                cipher.init(mode, key)
            }
            BiometricPrompt.CryptoObject(cipher)
        } catch (e: Exception) {
            null
        }
    }
}
