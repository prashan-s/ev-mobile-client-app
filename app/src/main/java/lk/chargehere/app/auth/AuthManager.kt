package lk.chargehere.app.auth

import lk.chargehere.app.data.repository.UserRepository
import lk.chargehere.app.domain.model.User
import lk.chargehere.app.utils.Result
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) {
    
    // Email/Password Registration for EVOwner
    suspend fun registerEVOwner(
        nic: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?
    ): Result<User> {
        return when (val result = userRepository.register(nic, email, password, firstName, lastName, phoneNumber)) {
            is Result.Success -> {
                val (user, accessToken) = result.data
                // Save tokens and user info including NIC
                tokenManager.saveTokens(accessToken, "", 3600) // No refresh token in current API
                tokenManager.saveUserInfo(user.id, user.email, user.role, nic)
                Result.Success(user)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }
    
    // Email/Password Login for EVOwner
    suspend fun loginEVOwner(email: String, password: String): Result<User> {
        android.util.Log.d("AuthManager", "loginEVOwner called for email: $email")
        return when (val result = userRepository.login(email, password)) {
            is Result.Success -> {
                val (user, accessToken) = result.data
                android.util.Log.d("AuthManager", "Login successful - accessToken: ${accessToken.take(50)}...")
                android.util.Log.d("AuthManager", "User NIC: ${user.nic}")

                // Save tokens and user info with actual access token and NIC
                tokenManager.saveTokens(accessToken, "", 3600)
                tokenManager.saveUserInfo(user.id, user.email, user.role, user.nic)

                android.util.Log.d("AuthManager", "Tokens and user info saved")
                Result.Success(user)
            }
            is Result.Error -> {
                android.util.Log.e("AuthManager", "Login failed: ${result.message}")
                result
            }
            is Result.Loading -> Result.Loading
        }
    }

    // Email/Password Login for Station Operator
    suspend fun loginStationOperator(email: String, password: String): Result<String?> {
        android.util.Log.d("AuthManager", "loginStationOperator called for email: $email")
        return when (val result = userRepository.loginStationOperator(email, password)) {
            is Result.Success -> {
                val (userId, accessToken, stationName) = result.data
                android.util.Log.d("AuthManager", "Station operator login successful - accessToken: ${accessToken.take(50)}...")
                android.util.Log.d("AuthManager", "Station name: $stationName")

                val resolvedStationName = if (stationName.isNullOrBlank()) {
                    android.util.Log.w("AuthManager", "Station name missing; defaulting to 'Not Assigned'")
                    "Not Assigned"
                } else {
                    stationName
                }

                // Save tokens and user info for station operator
                tokenManager.saveTokens(accessToken, "", 3600)
                tokenManager.saveUserInfo(userId, email, "OPERATOR", null)

                // Save station name to preferences for later use
                tokenManager.saveStationName(resolvedStationName)

                android.util.Log.d("AuthManager", "Station operator tokens and info saved")
                Result.Success(resolvedStationName)
            }
            is Result.Error -> {
                android.util.Log.e("AuthManager", "Station operator login failed: ${result.message}")
                result
            }
            is Result.Loading -> Result.Loading
        }
    }
    
    // Legacy Google Sign-In methods (kept for backward compatibility if needed)
    @Deprecated("Use email/password authentication instead")
    suspend fun signInWithGoogle(): Result<User> {
        return Result.Error("Google Sign-In is deprecated. Please use email/password authentication.")
    }
    
    @Deprecated("Use registerEVOwner instead")
    suspend fun registerUser(googleId: String, nic: String, name: String, email: String, phone: String? = null): Result<User> {
        return Result.Error("This registration method is deprecated. Please use registerEVOwner.")
    }
    
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    fun getCurrentUserId(): String? {
        return tokenManager.getUserId()
    }
    
    fun getCurrentUserEmail(): String? {
        return tokenManager.getUserEmail()
    }
    
    fun getCurrentUserRole(): String? {
        return tokenManager.getUserRole()
    }
    
    fun getCurrentUserNic(): String? {
        return tokenManager.getUserNic()
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            tokenManager.clearTokens()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Sign out failed: ${e.message}")
        }
    }
    
    suspend fun logout(): Result<Unit> = signOut()
    
    suspend fun updateProfile(
        nic: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        phoneNumber: String?
    ): Result<User> {
        return userRepository.updateProfile(nic, firstName, lastName, email, phoneNumber)
    }
    
    suspend fun deactivateAccount(nic: String): Result<Unit> {
        return when (val result = userRepository.deactivateAccount(nic)) {
            is Result.Success -> {
                signOut()
                result
            }
            else -> result
        }
    }
    
    private fun hashNIC(nic: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(nic.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}