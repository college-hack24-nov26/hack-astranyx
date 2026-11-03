package com.sktech.wastetrack.ui.screens.analytics

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.analytics.FarmExpenseItem
import com.sktech.wastetrack.domain.model.analytics.FarmPnLLedger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

data class FarmProfitLossUiState(
    val ledger: FarmPnLLedger = FarmPnLLedger(),
    val showAddDialog: Boolean = false,
    val selectedCategory: String = "Fertilizer & Soil NPK",
    val availableCategories: List<String> = listOf(
        "Fertilizer & Soil NPK",
        "High-Yield Certified Seed",
        "Diesel Tractor Tillage",
        "Labour & Super Seeder",
        "Drip Irrigation Power",
        "Crop Protection & Bio-Pesticide"
    ),
    val descriptionInput: String = "",
    val amountInput: String = "",
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class FarmProfitLossViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FarmProfitLossUiState())
    val uiState: StateFlow<FarmProfitLossUiState> = _uiState.asStateFlow()

    fun openAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun closeAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                descriptionInput = "",
                amountInput = ""
            )
        }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun updateDescription(desc: String) {
        _uiState.update { it.copy(descriptionInput = desc) }
    }

    fun updateAmount(amt: String) {
        _uiState.update { it.copy(amountInput = amt) }
    }

    fun addExpense() {
        val amount = _uiState.value.amountInput.toDoubleOrNull() ?: 0.0
        val desc = _uiState.value.descriptionInput.trim()

        if (amount <= 0.0) {
            _uiState.update { it.copy(error = "Enter a valid expense amount") }
            return
        }
        if (desc.isBlank()) {
            _uiState.update { it.copy(error = "Enter an expense description") }
            return
        }

        val newItem = FarmExpenseItem(
            id = "exp_${UUID.randomUUID().toString().take(6)}",
            category = _uiState.value.selectedCategory,
            description = desc,
            amountInr = amount,
            dateText = "Today"
        )

        _uiState.update { current ->
            val updatedExpenses = listOf(newItem) + current.ledger.expenses
            current.copy(
                ledger = current.ledger.copy(expenses = updatedExpenses),
                showAddDialog = false,
                descriptionInput = "",
                amountInput = "",
                message = "Farm operational expense recorded"
            )
        }
    }

    fun deleteExpense(id: String) {
        _uiState.update { current ->
            val updatedExpenses = current.ledger.expenses.filterNot { it.id == id }
            current.copy(
                ledger = current.ledger.copy(expenses = updatedExpenses),
                message = "Expense removed from ledger"
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

