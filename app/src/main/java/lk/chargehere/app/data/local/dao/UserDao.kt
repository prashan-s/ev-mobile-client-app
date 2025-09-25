package lk.chargehere.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import lk.chargehere.app.data.local.entities.UserEntity

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE user_id = :userId")
    fun observeUser(userId: String): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET is_active = 0, updated_at = :timestamp WHERE user_id = :userId")
    suspend fun deactivateUser(userId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteUser(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}