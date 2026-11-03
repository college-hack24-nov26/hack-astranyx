package com.sktech.wastetrack.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sktech.wastetrack.domain.model.User
import com.sktech.wastetrack.domain.model.UserRole
import com.sktech.wastetrack.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SignUpState(
    val name: String = "",
    val role: UserRole = UserRole.SUPERVISOR,
    val organizationName: String = "",
    val factoryId: String = "",
    val industrialArea: String = "",
    val city: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val registrationNumber: String = "",
    val vehicleNumber: String = "",
    val vehicleType: String = com.sktech.wastetrack.domain.model.VehicleType.BIG_TRUCK.displayName,
    val isFetchingLocation: Boolean = false,
    val locationMessage: String? = null,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val selectedRole = authRepository.getSelectedRole()
            val user = authRepository.getCurrentUser()
            if (user != null && user.isProfileComplete) {
                _state.update {
                    it.copy(
                        name = user.name,
                        role = user.role,
                        organizationName = user.organizationName,
                        factoryId = user.factoryId.ifBlank {
                            when (user.role) {
                                UserRole.RECYCLER, UserRole.BUYER -> "REC-${UUID.randomUUID().toString().take(6).uppercase()}"
                                UserRole.DRIVER -> "LOG-${UUID.randomUUID().toString().take(6).uppercase()}"
                                else -> "FAC-${UUID.randomUUID().toString().take(6).uppercase()}"
                            }
                        },
                        industrialArea = user.industrialArea,
                        city = user.city,
                        latitude = user.latitude,
                        longitude = user.longitude,
                        registrationNumber = user.registrationNumber,
                        vehicleNumber = user.vehicleNumber.ifBlank { if (user.role == UserRole.DRIVER) user.registrationNumber else "" },
                        vehicleType = user.vehicleType.ifBlank { com.sktech.wastetrack.domain.model.VehicleType.BIG_TRUCK.displayName }
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        role = selectedRole,
                        factoryId = when (selectedRole) {
                            UserRole.RECYCLER, UserRole.BUYER -> "REC-${UUID.randomUUID().toString().take(6).uppercase()}"
                            UserRole.DRIVER -> "LOG-${UUID.randomUUID().toString().take(6).uppercase()}"
                            else -> "FAC-${UUID.randomUUID().toString().take(6).uppercase()}"
                        }
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, error = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _state.update {
            it.copy(
                role = role,
                factoryId = when (role) {
                    UserRole.RECYCLER, UserRole.BUYER -> "REC-${UUID.randomUUID().toString().take(6).uppercase()}"
                    UserRole.DRIVER -> "LOG-${UUID.randomUUID().toString().take(6).uppercase()}"
                    else -> "FAC-${UUID.randomUUID().toString().take(6).uppercase()}"
                }
            )
        }
    }

    fun onOrganizationNameChanged(orgName: String) {
        _state.update { it.copy(organizationName = orgName, error = null) }
    }

    fun onFactoryIdChanged(factoryId: String) {
        _state.update { it.copy(factoryId = factoryId, error = null) }
    }

    fun onIndustrialAreaChanged(area: String) {
        _state.update { it.copy(industrialArea = area, error = null) }
    }

    fun onCityChanged(city: String) {
        _state.update { it.copy(city = city, error = null) }
    }

    fun onLocationDetected(detectedCity: String, detectedLocality: String?, lat: Double, lon: Double) {
        val area = if (!detectedLocality.isNullOrBlank()) "$detectedLocality, $detectedCity" else detectedCity
        _state.update {
            it.copy(
                city = detectedCity,
                industrialArea = if (it.industrialArea.isBlank()) area else it.industrialArea,
                latitude = lat,
                longitude = lon,
                isFetchingLocation = false,
                locationMessage = "📍 $detectedCity (${String.format("%.4f", lat)}, ${String.format("%.4f", lon)})",
                error = null
            )
        }
    }

    fun setFetchingLocation(fetching: Boolean) {
        _state.update { it.copy(isFetchingLocation = fetching, error = null) }
    }

    fun setLocationError(errorMsg: String) {
        _state.update { it.copy(isFetchingLocation = false, error = errorMsg) }
    }

    fun onRegistrationNumberChanged(regNo: String) {
        _state.update { it.copy(registrationNumber = regNo, error = null) }
    }

    fun onVehicleNumberChanged(vehNo: String) {
        _state.update { it.copy(vehicleNumber = vehNo.uppercase(), error = null) }
    }

    fun onVehicleTypeChanged(type: String) {
        _state.update { it.copy(vehicleType = type, error = null) }
    }

    fun completeProfile() {
        val s = _state.value
        if (s.name.trim().isBlank()) {
            _state.update { it.copy(error = "Please enter your full name") }
            return
        }
        if (s.organizationName.trim().isBlank()) {
            _state.update {
                it.copy(
                    error = when (s.role) {
                        UserRole.DRIVER -> "Please enter your transport agency or carrier name"
                        UserRole.RECYCLER, UserRole.BUYER -> "Please enter your business or company name"
                        else -> "Please enter your farm or holding name"
                    }
                )
            }
            return
        }
        if (s.role == UserRole.DRIVER && s.vehicleNumber.trim().isBlank()) {
            _state.update { it.copy(error = "Please enter your vehicle registration number (e.g. MH-15-EG-8291)") }
            return
        }
        val finalCity = s.city.trim().ifBlank { s.industrialArea.trim() }
        if (finalCity.isBlank()) {
            _state.update { it.copy(error = "Please enter or select your City") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            var lat = s.latitude
            var lon = s.longitude

            if (lat == 0.0 || lon == 0.0) {
                val fullAddress = "${s.industrialArea.trim()}, $finalCity, Maharashtra"
                val geocoded = com.sktech.wastetrack.util.LocationHelper.geocodeAddress(context, fullAddress)
                    ?: com.sktech.wastetrack.util.LocationHelper.geocodeAddress(context, "$finalCity, Maharashtra")
                if (geocoded != null) {
                    lat = geocoded.first
                    lon = geocoded.second
                }
            }

            val phone = authRepository.getLastEnteredPhone()
            val uid = com.sktech.wastetrack.data.repository.AuthRepositoryImpl.generateStableUserId(phone)

            val effectiveVehicleNo = if (s.role == UserRole.DRIVER) s.vehicleNumber.trim().uppercase() else ""
            val effectiveVehicleType = if (s.role == UserRole.DRIVER) s.vehicleType.trim().ifBlank { com.sktech.wastetrack.domain.model.VehicleType.BIG_TRUCK.displayName } else ""
            val effectiveRegNo = if (s.role == UserRole.DRIVER && s.registrationNumber.isBlank()) effectiveVehicleNo else s.registrationNumber.trim()

            val user = User(
                id = uid,
                name = s.name.trim(),
                phone = phone,
                role = s.role,
                organizationName = s.organizationName.trim(),
                factoryId = s.factoryId.trim().ifBlank {
                    when (s.role) {
                        UserRole.RECYCLER, UserRole.BUYER -> "REC-${UUID.randomUUID().toString().take(6).uppercase()}"
                        UserRole.DRIVER -> "LOG-${UUID.randomUUID().toString().take(6).uppercase()}"
                        else -> "FAC-${UUID.randomUUID().toString().take(6).uppercase()}"
                    }
                },
                industrialArea = s.industrialArea.trim().ifBlank { finalCity },
                city = finalCity,
                latitude = lat,
                longitude = lon,
                registrationNumber = effectiveRegNo,
                vehicleNumber = effectiveVehicleNo,
                vehicleType = effectiveVehicleType,
                isProfileComplete = true,
                languagePreference = "EN",
                createdAt = System.currentTimeMillis()
            )

            val existing = authRepository.getUserByPhone(phone)
            if (existing != null && existing.isProfileComplete && existing.role != s.role) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        error = "This mobile number is already registered as a ${existing.role.name}. Each mobile number is restricted to a single stakeholder account."
                    )
                }
                return@launch
            }

            val result = authRepository.saveUserProfile(user)
            if (result.isSuccess) {
                _state.update { it.copy(isSaving = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isSaving = false, error = result.exceptionOrNull()?.message ?: "Failed to save profile") }
            }
        }
    }
}
