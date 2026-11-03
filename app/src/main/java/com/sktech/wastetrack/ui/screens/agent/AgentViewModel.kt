package com.sktech.wastetrack.ui.screens.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    ASSISTANT
}

data class AgentUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = "Namaste! I am your Kisan AI Assistant & Agronomic Copilot. You can ask me about Mandi prices, crop diseases, MPCB stubble burning regulations, or biomass procurement rates."
        )
    ),
    val inputText: String = "",
    val isThinking: Boolean = false,
    val isListening: Boolean = false
)

@HiltViewModel
class AgentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun toggleVoiceListening() {
        val current = _uiState.value.isListening
        if (!current) {
            _uiState.update { it.copy(isListening = true) }
            viewModelScope.launch {
                delay(2200)
                val voiceQuery = "What is the current Mandi price for Cotton Stalk biomass?"
                _uiState.update { it.copy(isListening = false, inputText = voiceQuery) }
                sendMessage(voiceQuery)
            }
        } else {
            _uiState.update { it.copy(isListening = false) }
        }
    }

    fun sendMessage(query: String? = null) {
        val messageText = (query ?: _uiState.value.inputText).trim()
        if (messageText.isBlank()) return

        val userMessage = ChatMessage(sender = MessageSender.USER, text = messageText)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isThinking = true
            )
        }

        viewModelScope.launch {
            delay(1100)
            val replyText = generateAiResponse(messageText)
            val assistantMessage = ChatMessage(sender = MessageSender.ASSISTANT, text = replyText)
            _uiState.update {
                it.copy(
                    messages = it.messages + assistantMessage,
                    isThinking = false
                )
            }
        }
    }

    private fun generateAiResponse(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("mandi") || lower.contains("rate") || lower.contains("price") -> {
                "Today's APMC & Biomass Rates across Nashik district:\n• Baled Cotton Stalks: ₹2,800/MT (Sinnar Agro Hub)\n• Crushed Bagasse: ₹1,900/MT (Niphad Sugar Basin)\n• Wheat Straw Pellets: ₹4,500/MT (Igatpuri Bio-Energy)\n• Soybean Husks: ₹2,500/MT (Dindori Cluster)\nDemand is trending 8% higher due to co-firing mandates in thermal boilers."
            }
            lower.contains("burn") || lower.contains("stubble") || lower.contains("penalty") || lower.contains("mpcb") -> {
                "Under the Air (Prevention and Control of Pollution) Act & NGT guidelines:\n• Open-air burning of crop residue is strictly prohibited in Maharashtra.\n• Penalties: Up to ₹15,000 per acre and loss of PM-KISAN subsidy eligibility.\n• AgriStock Alternative: Sell your residue to nearby bio-coal plants and earn ₹12,000 - ₹25,000 per harvest + earn verifiable Carbon Offsets."
            }
            lower.contains("disease") || lower.contains("pest") || lower.contains("cotton") || lower.contains("bollworm") -> {
                "For Cotton crop residue management after harvest:\n• Ensure stalks are shredded within 15 days to break the Pink Bollworm (Pectinophora gossypiella) life cycle.\n• If baling stalks, maintain moisture under 12% to prevent fungal rot and self-combustion in storage yards."
            }
            lower.contains("drying") || lower.contains("moisture") -> {
                "Biomass Drying Advisory:\n• Ideal moisture for boiler feedstock: ≤ 10-12%.\n• In current Nashik sunny weather (31°C, 38% RH), 48 hours of field sun-drying reduces stalk moisture from 28% to 11%.\n• Weighbridges apply an excess water penalty for moisture above 12%."
            }
            lower.contains("escrow") || lower.contains("payment") -> {
                "AgriStock Smart Escrow Protection:\n• Buyers deposit 100% of consignment funds into the escrow vault before truck dispatch.\n• Payout is released to the farmer's bank account within 2 hours of dual weighbridge gross-tare verification at the factory gate."
            }
            else -> {
                "AgriStock AI Agronomist Analysis:\nYour query regarding \"$query\" has been processed. Based on current seasonal telemetry in Nashik, we recommend baling post-harvest residues rather than open tillage. Buyers in Sinnar and Igatpuri MIDC are currently accepting cotton and bagasse lots with automated pickup logistics."
            }
        }
    }
}

