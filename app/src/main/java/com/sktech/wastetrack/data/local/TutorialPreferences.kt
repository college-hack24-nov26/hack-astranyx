package com.sktech.wastetrack.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorialPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isFarmerTutorialCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_FARMER_TUTORIAL_COMPLETED, false)
    )
    val isFarmerTutorialCompleted: StateFlow<Boolean> = _isFarmerTutorialCompleted.asStateFlow()

    fun isTutorialCompleted(userId: String = ""): Boolean {
        val key = if (userId.isNotBlank()) "${KEY_FARMER_TUTORIAL_COMPLETED}_$userId" else KEY_FARMER_TUTORIAL_COMPLETED
        return prefs.getBoolean(key, prefs.getBoolean(KEY_FARMER_TUTORIAL_COMPLETED, false))
    }

    fun setTutorialCompleted(completed: Boolean, userId: String = "") {
        val editor = prefs.edit()
        editor.putBoolean(KEY_FARMER_TUTORIAL_COMPLETED, completed)
        if (userId.isNotBlank()) {
            editor.putBoolean("${KEY_FARMER_TUTORIAL_COMPLETED}_$userId", completed)
        }
        editor.apply()
        _isFarmerTutorialCompleted.value = completed
    }

    fun resetTutorial(userId: String = "") {
        setTutorialCompleted(false, userId)
    }

    companion object {
        private const val PREFS_NAME = "agristock_tutorial_prefs"
        private const val KEY_FARMER_TUTORIAL_COMPLETED = "farmer_tutorial_completed"
    }
}
