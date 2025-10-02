package lk.chargehere.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lk.chargehere.app.data.local.dao.UserDao
import lk.chargehere.app.data.mapper.*
import lk.chargehere.app.data.remote.api.AuthApiService
import lk.chargehere.app.data.remote.dto.*
import lk.chargehere.app.domain.model.User
import lk.chargehere.app.utils.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userDao: UserDao,
    private val tokenManager: lk.chargehere.app.auth.TokenManager
) {

    // Register EVOwner with email/password
    suspend fun register(
        nic: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?
    ): Result<Pair<User, String>> {
        return try {
            val request = RegisterRequest(nic, email, password, firstName, lastName, phoneNumber)
            val response = authApiService.register(request)
            
            if (response.isSuccessful) {
                val registeredNic = response.body()
                if (registeredNic != null) {
                    // Fetch profile using GetEVOwnerByNIC
                    val profileResponse = authApiService.getProfileByNIC(nic)
                    if (profileResponse.isSuccessful) {
                        val userDto = profileResponse.body()
                        if (userDto != null) {
                            val userEntity = userDto.toEntity()
                            userDao.insertUser(userEntity)
                            // For registration, we need to login to get the token
                            val loginResponse = authApiService.login(LoginRequest(email, password))
                            val token = loginResponse.body()?.accessToken ?: ""
                            Result.Success(Pair(userEntity.toDomain(), token))
                        } else {
                            Result.Error("Registration failed: No user data received")
                        }
                    } else {
                        Result.Error("Registration failed: Could not fetch profile")
                    }
                } else {
                    Result.Error("Registration failed: No auth data received")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.Error("Registration failed: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Login EVOwner with email/password
    suspend fun login(email: String, password: String): Result<Pair<User, String>> {
        return try {
            android.util.Log.d("UserRepository", "Login attempt for email: $email")
            val request = LoginRequest(email, password)
            val response = authApiService.login(request)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                android.util.Log.d("UserRepository", "Login response received: ${loginResponse != null}")

                if (loginResponse != null) {
                    // Get NIC and access token directly from login response
                    val nic = loginResponse.nic
                    val token = loginResponse.accessToken

                    android.util.Log.d("UserRepository", "NIC from response: $nic")
                    android.util.Log.d("UserRepository", "Token from response: ${token.take(50)}...")

                    // IMPORTANT: Save token to SharedPreferences IMMEDIATELY so it's available for next API call
                    android.util.Log.d("UserRepository", "Saving token to SharedPreferences before profile fetch")
                    tokenManager.saveTokens(token, "", 3600)

                    // Fetch user profile from API using the NIC (Bearer token will now be included)
                    val profileResponse = authApiService.getProfileByNIC(nic)

                    if (profileResponse.isSuccessful) {
                        val userDto = profileResponse.body()
                        if (userDto != null) {
                            // Save user to local database
                            val userEntity = userDto.toEntity()
                            userDao.insertUser(userEntity)

                            android.util.Log.d("UserRepository", "Returning Success with token: ${token.take(50)}...")
                            // Return user and token
                            Result.Success(Pair(userEntity.toDomain(), token))
                        } else {
                            Result.Error("Failed to fetch user profile: No data received")
                        }
                    } else {
                        // If profile fetch fails, try to get from cache as fallback
                        val cachedUser = userDao.getUserByEmail(email)
                        if (cachedUser != null) {
                            android.util.Log.d("UserRepository", "Using cached user, returning token: ${token.take(50)}...")
                            Result.Success(Pair(cachedUser.toDomain(), token))
                        } else {
                            Result.Error("Failed to fetch user profile: ${profileResponse.code()}")
                        }
                    }
                } else {
                    Result.Error("Login failed: No auth data received")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.Error("Login failed: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Login error: ${e.message}", e)
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Sign in with Google OAuth
    suspend fun signInWithGoogle(idToken: String): Result<Pair<User, String>> {
        return try {
            val request = GoogleSignInRequest(idToken)
            val response = authApiService.signInWithGoogle(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    val accessToken = authResponse.accessToken
                    // Try to get user from cache or use a default user
                    // In production, the backend should return user info with the token
                    val cachedUser = userDao.getAllUsers().firstOrNull()
                    if (cachedUser != null) {
                        Result.Success(Pair(cachedUser.toDomain(), accessToken))
                    } else {
                        // Create a temporary user - in real scenario, backend provides this
                        Result.Error("Google sign-in successful but user profile not available. Please complete registration.")
                    }
                } else {
                    Result.Error("Google sign-in failed: No auth data received")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.Error("Google sign-in failed: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Update EVOwner profile
    suspend fun updateProfile(
        nic: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        phoneNumber: String?
    ): Result<User> {
        return try {
            val request = UpdateProfileRequest(nic, email, firstName, lastName, phoneNumber)
            val response = authApiService.updateProfile(nic, request)
            
            if (response.isSuccessful) {
                // Fetch updated profile using GetEVOwnerByNIC
                val profileResponse = authApiService.getProfileByNIC(nic)
                if (profileResponse.isSuccessful) {
                    val userDto = profileResponse.body()
                    if (userDto != null) {
                        val userEntity = userDto.toEntity()
                        userDao.updateUser(userEntity)
                        Result.Success(userEntity.toDomain())
                    } else {
                        Result.Error("Update failed: No user data received")
                    }
                } else {
                    Result.Error("Update failed: Could not fetch updated profile")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.Error("Update failed: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Get EVOwner profile by NIC
    suspend fun getProfile(nic: String): Result<User> {
        return try {
            val response = authApiService.getProfileByNIC(nic)
            
            if (response.isSuccessful) {
                val userDto = response.body()
                if (userDto != null) {
                    val userEntity = userDto.toEntity()
                    userDao.insertUser(userEntity)
                    Result.Success(userEntity.toDomain())
                } else {
                    Result.Error("Profile fetch failed: No user data received")
                }
            } else {
                Result.Error("Profile fetch failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Get current user's profile (requires NIC from caller)
    suspend fun getCurrentProfile(nic: String): Result<User> {
        return getProfile(nic)
    }
    
    // Get EVOwner profile by ID
    suspend fun getProfileById(id: String): Result<User> {
        return try {
            val response = authApiService.getProfileById(id)
            
            if (response.isSuccessful) {
                val userDto = response.body()
                if (userDto != null) {
                    val userEntity = userDto.toEntity()
                    userDao.insertUser(userEntity)
                    Result.Success(userEntity.toDomain())
                } else {
                    Result.Error("Profile fetch failed: No user data received")
                }
            } else {
                Result.Error("Profile fetch failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Deactivate EVOwner account
    suspend fun deactivateAccount(nic: String): Result<Unit> {
        return try {
            val response = authApiService.deactivate(nic)
            
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Deactivation failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Local database operations
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toDomain()
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomain()
    }
    
    fun observeUser(userId: String): Flow<User?> {
        return userDao.observeUser(userId).map { it?.toDomain() }
    }
    
    suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }
    
    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }
}