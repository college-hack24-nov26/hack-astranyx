package com.sktech.wastetrack.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.sktech.wastetrack.domain.agent.AgentLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private val TAG = "SpeechManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private var pendingSpeech: PendingSpeechItem? = null

    private data class PendingSpeechItem(
        val text: String,
        val language: AgentLanguage,
        val utteranceId: String,
        val onDone: () -> Unit
    )

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingId = MutableStateFlow<String?>(null)
    val currentlySpeakingId: StateFlow<String?> = _currentlySpeakingId.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech: ${e.message}", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            Log.d(TAG, "TextToSpeech successfully initialized")

            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _currentlySpeakingId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                    Log.w(TAG, "TTS utterance error on ID: $utteranceId")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _currentlySpeakingId.value = null
                    Log.w(TAG, "TTS utterance error code: $errorCode on ID: $utteranceId")
                }
            })


            pendingSpeech?.let { pending ->
                pendingSpeech = null
                speak(pending.text, pending.language, pending.utteranceId, pending.onDone)
            }
        } else {
            isTtsInitialized = false
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
        }
    }

    fun startListening(
        language: AgentLanguage,
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is not supported on this device.")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    _rmsLevel.value = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _rmsLevel.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _rmsLevel.value = 0f
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_NETWORK -> "Network connection issue"
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        else -> "Speech recognition error ($error)"
                    }
                    onError(msg)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _rmsLevel.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val primaryLocale = language.localeTag

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, primaryLocale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, primaryLocale)
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN", "hi-IN", "mr-IN"))
            putExtra("android.speech.extra.ENABLE_LANGUAGE_DETECTION", true)
            putExtra("android.speech.extra.LANGUAGE_SWITCH_ALLOWED", true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError(e.message ?: "Failed to start listening")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer: ${e.message}")
        }
        _isListening.value = false
        _rmsLevel.value = 0f
    }

    fun speak(
        text: String,
        language: AgentLanguage,
        utteranceId: String = "agent_speech_${System.currentTimeMillis()}",
        onDone: () -> Unit = {}
    ) {
        if (text.isBlank()) return

        if (!isTtsInitialized || textToSpeech == null) {
            Log.d(TAG, "TTS not ready yet. Queuing pending speech item.")
            pendingSpeech = PendingSpeechItem(text, language, utteranceId, onDone)
            if (textToSpeech == null) {
                initializeTts()
            }
            return
        }

        stopSpeaking()

        val cleanText = sanitizeTextForSpeech(text, language)
        if (cleanText.isBlank()) return

        val hasDevanagari = cleanText.any { it in '\u0900'..'\u097F' }


        val targetLocale: Locale = when {
            language == AgentLanguage.MARATHI -> {
                val mrLocale = Locale("mr", "IN")
                val avail = textToSpeech?.isLanguageAvailable(mrLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (avail >= TextToSpeech.LANG_AVAILABLE) {
                    mrLocale
                } else {

                    Log.d(TAG, "Marathi voice not available on device TTS engine. Using Hindi Devanagari voice.")
                    Locale("hi", "IN")
                }
            }
            language == AgentLanguage.HINDI || hasDevanagari -> {
                val hiLocale = Locale("hi", "IN")
                val avail = textToSpeech?.isLanguageAvailable(hiLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (avail >= TextToSpeech.LANG_AVAILABLE) {
                    hiLocale
                } else {
                    Locale("hi")
                }
            }
            else -> {
                Locale("en", "IN")
            }
        }

        try {
            var langResult = textToSpeech?.setLanguage(targetLocale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                if (hasDevanagari) {

                    textToSpeech?.setLanguage(Locale("hi", "IN"))
                } else {
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                }
            }

            textToSpeech?.setPitch(1.02f)
            textToSpeech?.setSpeechRate(0.96f)


            val sentences = cleanText.split(Regex("(?<=[.!?।])\\s+")).filter { it.isNotBlank() }

            if (sentences.isEmpty()) {
                textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } else {
                for (i in sentences.indices) {
                    val queueMode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    val chunkId = if (i == sentences.lastIndex) utteranceId else "${utteranceId}_part_$i"
                    textToSpeech?.speak(sentences[i], queueMode, null, chunkId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak error: ${e.message}", e)
            _isSpeaking.value = false
            _currentlySpeakingId.value = null
        }
    }


    private fun sanitizeTextForSpeech(rawText: String, language: AgentLanguage): String {
        var text = rawText


        text = text.replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")


        val currencyWord = when (language) {
            AgentLanguage.HINDI, AgentLanguage.MARATHI -> "रुपये"
            AgentLanguage.ENGLISH -> "Rupees"
        }
        text = text.replace(Regex("₹\\s*([0-9,]+(\\.[0-9]+)?)"), "$1 $currencyWord")


        text = text.replace(Regex("[*#_`~>|\\[\\]]"), " ")


        text = text.replace(Regex("^[\\s]*[•\\-*][\\s]+", RegexOption.MULTILINE), ", ")


        text = text.replace(Regex("[⚠️✓✔🌿🌱🌾💡🚚📦🔍📈📉⭐✨🎯🚜🚨📊💰🛡️🌾❌⚙️]"), " ")


        text = text.replace("\n", ". ")
        text = text.replace(Regex("\\s+"), " ")
        text = text.replace(Regex("\\.{2,}"), ".")

        return text.trim()
    }

    fun stopSpeaking() {
        try {
            if (textToSpeech?.isSpeaking == true) {
                textToSpeech?.stop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS: ${e.message}")
        }
        _isSpeaking.value = false
        _currentlySpeakingId.value = null
    }

    fun destroy() {
        stopListening()
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsInitialized = false
    }
}
