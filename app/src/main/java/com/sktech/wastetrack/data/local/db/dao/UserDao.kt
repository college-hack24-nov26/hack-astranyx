package com.sktech.wastetrack.data.local.db.dao

import androidx.room.*
import com.sktech.wastetrack.data.local.db.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone")
    suspend fun getByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone AND UPPER(role) = UPPER(:role) LIMIT 1")
    suspend fun getByPhoneAndRole(phone: String, role: String): UserEntity?

    @Query("SELECT * FROM users WHERE UPPER(role) = UPPER(:role) LIMIT 1")
    suspend fun getByRole(role: String): UserEntity?

    @Query("SELECT * FROM users WHERE UPPER(role) = UPPER(:role)")
    suspend fun getAllByRole(role: String): List<UserEntity>

    @Query("SELECT * FROM users WHERE UPPER(role) = UPPER(:role)")
    fun getAllByRoleFlow(role: String): kotlinx.coroutines.flow.Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE UPPER(role) IN ('DRIVER', 'TRANSPORTER') ORDER BY isAvailable DESC, name ASC")
    fun getAllDriversFlow(): kotlinx.coroutines.flow.Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE UPPER(role) IN ('DRIVER', 'TRANSPORTER') ORDER BY isAvailable DESC, name ASC")
    suspend fun getAllDrivers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getByIdFlow(id: String): kotlinx.coroutines.flow.Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): UserEntity?

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
