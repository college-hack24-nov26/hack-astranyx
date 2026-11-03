package com.sktech.wastetrack.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sktech.wastetrack.data.local.db.dao.UserDao
import com.sktech.wastetrack.data.local.db.entity.UserEntity
import com.sktech.wastetrack.domain.model.User
import com.sktech.wastetrack.domain.model.UserRole
import com.sktech.wastetrack.domain.repository.IAuthRepository
import com.sktech.wastetrack.util.Constants
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import com.sktech.wastetrack.data.local.db.WasteTrackDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val database: Lazy<WasteTrackDatabase>
) : IAuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences("wastetrack_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "AuthRepository"
        private const val KEY_SELECTED_ROLE = "selected_user_role"
        const val KEY_CACHED_USER_ID = "cached_user_id"
        private const val KEY_CACHED_USER_NAME = "cached_user_name"
        private const val KEY_CACHED_ORG_NAME = "cached_org_name"
        private const val KEY_CACHED_FACTORY_ID = "cached_factory_id"
        private const val KEY_CACHED_AREA = "cached_area"
        private const val KEY_CACHED_REG_NO = "cached_reg_no"
        private const val KEY_IS_PROFILE_COMPLETE = "is_profile_complete"
        private const val KEY_IS_LOGGED_IN = "is_logged_in_pref"
        private const val KEY_LAST_PHONE = "last_entered_phone"

        fun generateStableUserId(phone: String, role: UserRole? = null): String {
            val last10 = com.sktech.wastetrack.util.PhoneNormalizer.extractLast10(phone)
            return if (role != null) {
                val roleSuffix = when (role) {
                    UserRole.BUYER -> "buyer"
                    UserRole.FARMER -> "farmer"
                    UserRole.DRIVER -> "driver"
                    UserRole.ADMIN -> "admin"
                    else -> role.name.lowercase()
                }
                if (last10.isNotBlank()) "user_${last10}_$roleSuffix" else "user_${roleSuffix}_001"
            } else {
                if (last10.isNotBlank()) "user_$last10" else "user_default_001"
            }
        }
    }

    override fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    override suspend fun setLastEnteredPhone(phone: String) {
        val cleanPhone = phone.trim()
        if (cleanPhone.isNotBlank()) {
            prefs.edit().putString(KEY_LAST_PHONE, cleanPhone).apply()
        }
    }

    override fun getLastEnteredPhone(): String {
        return prefs.getString(KEY_LAST_PHONE, "") ?: ""
    }

    override suspend fun getUserByPhone(phone: String): User? {
        val last10 = com.sktech.wastetrack.util.PhoneNormalizer.extractLast10(phone)
        if (last10.isBlank()) return null
        val stableId = com.sktech.wastetrack.util.PhoneNormalizer.toUserId(phone)
        val standardPhone = com.sktech.wastetrack.util.PhoneNormalizer.toStandardFormat(phone)
        val phoneVariants = com.sktech.wastetrack.util.PhoneNormalizer.getPhoneVariants(phone)

        var roomEntity: UserEntity? = userDao.getById(stableId)
        if (roomEntity == null) {
            for (v in phoneVariants) {
                roomEntity = userDao.getByPhone(v)
                if (roomEntity != null) break
            }
        }

        if (roomEntity != null) {
            return roomEntity.toDomain()
        }

        try {
            var doc = withTimeoutOrNull(2500L) {
                firestore.collection("users").document(stableId).get().await()
            }

            if (doc == null || !doc.exists()) {
                for (v in phoneVariants) {
                    val querySnapshot = withTimeoutOrNull(2000L) {
                        firestore.collection("users")
                            .whereEqualTo("phone", v)
                            .limit(1)
                            .get().await()
                    }
                    if (querySnapshot != null && !querySnapshot.isEmpty) {
                        doc = querySnapshot.documents[0]
                        break
                    }
                }
            }

            if (doc != null && doc.exists()) {
                val rawRole = doc.getString("role") ?: "SUPERVISOR"
                val parsedRole = UserRole.fromString(rawRole)
                val user = parseUserFromDoc(doc, doc.id, standardPhone, parsedRole)
                userDao.insert(user.toEntity())
                return user
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error looking up user by phone: ${e.message}")
        }

        return null
    }

    override suspend fun isProfileComplete(): Boolean {
        val phone = getLastEnteredPhone()
        val user = getUserByPhone(phone)
        val isComplete = user != null && user.isProfileComplete && user.organizationName.isNotBlank() && user.factoryId.isNotBlank()

        if (user != null) {
            cacheUserLocally(user)
            setSelectedRole(user.role)
            if (isComplete) {
                initSyncOnce(user.id, user.factoryId, user.role)
            }
        }

        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, isComplete)
            .putBoolean(KEY_IS_PROFILE_COMPLETE, isComplete)
            .apply()
        return isComplete
    }

    override suspend fun getCurrentUser(): User? {
        if (!isLoggedIn()) {
            return null
        }

        val phone = getLastEnteredPhone()
        val user = getUserByPhone(phone)
        if (user != null && (user.organizationName.isNotBlank() || user.role == UserRole.DRIVER)) {
            cacheUserLocally(user)
            setSelectedRole(user.role)
            initSyncOnce(user.id, user.factoryId, user.role)
            return user
        }

        val cachedOrg = prefs.getString(KEY_CACHED_ORG_NAME, "") ?: ""
        val cachedName = prefs.getString(KEY_CACHED_USER_NAME, "") ?: ""
        val cachedFactory = prefs.getString(KEY_CACHED_FACTORY_ID, "") ?: ""
        val selectedRole = getSelectedRole()

        if (cachedOrg.isNotBlank()) {
            val fallbackUser = User(
                id = generateStableUserId(phone),
                name = cachedName.ifBlank { "User ${phone.takeLast(4)}" },
                phone = phone,
                role = selectedRole,
                organizationName = cachedOrg,
                factoryId = cachedFactory.ifBlank { if (selectedRole == UserRole.RECYCLER) "REC-UNIT-01" else "FAC-UNIT-01" },
                industrialArea = prefs.getString(KEY_CACHED_AREA, "Ambad MIDC Industrial Area, Nashik") ?: "Ambad MIDC Industrial Area, Nashik",
                registrationNumber = prefs.getString(KEY_CACHED_REG_NO, "27AAACB2233M1Z2") ?: "27AAACB2233M1Z2",
                isProfileComplete = true,
                languagePreference = "EN",
                city = prefs.getString("cached_city", "Nashik") ?: "Nashik",
                latitude = prefs.getFloat("cached_lat", 19.9975f).toDouble(),
                longitude = prefs.getFloat("cached_lon", 73.7898f).toDouble(),
                isAvailable = prefs.getBoolean("cached_is_available", true),
                createdAt = System.currentTimeMillis(),
                vehicleNumber = prefs.getString("cached_vehicle_no", "") ?: "",
                vehicleType = prefs.getString("cached_vehicle_type", "") ?: ""
            )
            userDao.insert(fallbackUser.toEntity())
            cacheUserLocally(fallbackUser)
            initSyncOnce(fallbackUser.id, fallbackUser.factoryId, fallbackUser.role)
            return fallbackUser
        }

        return null
    }

    override suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            val stableId = generateStableUserId(user.phone)
            val completeUser = user.copy(id = stableId, isProfileComplete = true)

            userDao.insert(completeUser.toEntity())
            cacheUserLocally(completeUser)
            setSelectedRole(completeUser.role)

            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putBoolean(KEY_IS_PROFILE_COMPLETE, true)
                .apply()

            scope.launch {
                try {
                    val userMap = hashMapOf(
                        "id" to completeUser.id,
                        "name" to completeUser.name,
                        "phone" to completeUser.phone,
                        "role" to completeUser.role.name,
                        "organizationName" to completeUser.organizationName,
                        "factoryId" to completeUser.factoryId,
                        "industrialArea" to completeUser.industrialArea,
                        "registrationNumber" to completeUser.registrationNumber,
                        "vehicleNumber" to completeUser.vehicleNumber,
                        "vehicleType" to completeUser.vehicleType,
                        "isProfileComplete" to true,
                        "languagePreference" to completeUser.languagePreference,
                        "city" to completeUser.city,
                        "latitude" to completeUser.latitude,
                        "longitude" to completeUser.longitude,
                        "isAvailable" to completeUser.isAvailable,
                        "createdAt" to completeUser.createdAt
                    )
                    firestore.collection("users").document(stableId).set(userMap, SetOptions.merge()).await()

                    auth.currentUser?.let { fbUser ->
                        try {
                            firestore.collection("users").document(fbUser.uid).set(userMap, SetOptions.merge()).await()
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed mirroring user to auth.uid: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Background Firestore sync failed (will retry online): ${e.message}")
                }
            }

            initSyncOnce(completeUser.id, completeUser.factoryId, completeUser.role)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "saveUserProfile failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {

        } catch (e: Exception) {
            Log.w(TAG, "Failed stopping realtime sync: ${e.message}")
        }
        try {
            database.get().clearUserScopedData()
        } catch (e: Exception) {
            Log.w(TAG, "Failed purging user scoped Room cache on logout: ${e.message}")
        }
        auth.signOut()
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_IS_PROFILE_COMPLETE, false)
            .putString(KEY_CACHED_USER_ID, "")
            .putString(KEY_LAST_PHONE, "")
            .putString(KEY_CACHED_USER_NAME, "")
            .putString(KEY_CACHED_ORG_NAME, "")
            .putString(KEY_CACHED_FACTORY_ID, "")
            .putString(KEY_CACHED_AREA, "")
            .putString(KEY_CACHED_REG_NO, "")
            .apply()
        isSyncInitialized.set(false)
    }

    override fun observeAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { _ ->
            trySend(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun setSelectedRole(role: UserRole) {
        prefs.edit().putString(KEY_SELECTED_ROLE, role.name).apply()
    }

    override fun getSelectedRole(): UserRole {
        val name = prefs.getString(KEY_SELECTED_ROLE, null) ?: return UserRole.SUPERVISOR
        return try {
            UserRole.valueOf(name)
        } catch (e: Exception) {
            UserRole.SUPERVISOR
        }
    }

    private val isSyncInitialized = AtomicBoolean(false)

    private fun initSyncOnce(userId: String, factoryId: String, role: UserRole) {
        if (isSyncInitialized.compareAndSet(false, true)) {

        }
    }

    private fun cacheUserLocally(user: User) {
        prefs.edit()
            .putString(KEY_CACHED_USER_ID, user.id)
            .putString(KEY_LAST_PHONE, user.phone)
            .putString(KEY_SELECTED_ROLE, user.role.name)
            .putString(KEY_CACHED_USER_NAME, user.name)
            .putString(KEY_CACHED_ORG_NAME, user.organizationName)
            .putString(KEY_CACHED_FACTORY_ID, user.factoryId)
            .putString(KEY_CACHED_AREA, user.industrialArea)
            .putString(KEY_CACHED_REG_NO, user.registrationNumber)
            .putString("cached_vehicle_no", user.vehicleNumber)
            .putString("cached_vehicle_type", user.vehicleType)
            .putBoolean(KEY_IS_PROFILE_COMPLETE, user.isProfileComplete)
            .putBoolean(KEY_IS_LOGGED_IN, user.isProfileComplete)
            .putString("cached_city", user.city)
            .putFloat("cached_lat", user.latitude.toFloat())
            .putFloat("cached_lon", user.longitude.toFloat())
            .putBoolean("cached_is_available", user.isAvailable)
            .apply()
    }

    private fun parseUserFromDoc(
        doc: DocumentSnapshot,
        stableId: String,
        phone: String,
        role: UserRole
    ): User {
        val roleFromDoc = doc.getString("role")
        val parsedRole = if (!roleFromDoc.isNullOrBlank()) {
            UserRole.fromString(roleFromDoc)
        } else {
            role
        }

        val rawRegNo = doc.getString("registrationNumber") ?: ""
        val rawVehNo = doc.getString("vehicleNumber") ?: ""
        val effectiveVehNo = rawVehNo.ifBlank { if (parsedRole == UserRole.DRIVER) rawRegNo else "" }

        return User(
            id = doc.getString("id") ?: stableId,
            name = doc.getString("name") ?: "User ${phone.takeLast(4)}",
            phone = doc.getString("phone") ?: phone,
            role = parsedRole,
            organizationName = doc.getString("organizationName") ?: "",
            factoryId = doc.getString("factoryId") ?: if (parsedRole == UserRole.RECYCLER) "REC-UNIT-01" else "FAC-UNIT-01",
            industrialArea = doc.getString("industrialArea") ?: "Industrial Area",
            registrationNumber = rawRegNo,
            isProfileComplete = doc.getBoolean("isProfileComplete") ?: true,
            languagePreference = doc.getString("languagePreference") ?: "EN",
            city = doc.getString("city") ?: "Nashik",
            latitude = doc.getDouble("latitude") ?: 0.0,
            longitude = doc.getDouble("longitude") ?: 0.0,
            isAvailable = doc.getBoolean("isAvailable") ?: true,
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            vehicleNumber = effectiveVehNo,
            vehicleType = doc.getString("vehicleType") ?: ""
        )
    }

    private fun UserEntity.toDomain(): User {
        val parsedRole = UserRole.fromString(role)
        return User(
            id = id,
            name = name,
            phone = phone,
            role = parsedRole,
            organizationName = organizationName,
            factoryId = factoryId,
            industrialArea = industrialArea,
            registrationNumber = registrationNumber,
            isProfileComplete = isProfileComplete,
            languagePreference = languagePreference,
            city = city,
            latitude = latitude,
            longitude = longitude,
            isAvailable = isAvailable,
            createdAt = createdAt,
            vehicleNumber = vehicleNumber.ifBlank { if (parsedRole == UserRole.DRIVER) registrationNumber else "" },
            vehicleType = vehicleType
        )
    }

    private fun User.toEntity(): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            phone = phone,
            role = role.name,
            organizationName = organizationName,
            factoryId = factoryId,
            industrialArea = industrialArea,
            registrationNumber = registrationNumber,
            isProfileComplete = isProfileComplete,
            languagePreference = languagePreference,
            city = city,
            latitude = latitude,
            longitude = longitude,
            isAvailable = isAvailable,
            createdAt = createdAt,
            vehicleNumber = vehicleNumber,
            vehicleType = vehicleType
        )
    }
}
