package lk.chargehere.app.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import lk.chargehere.app.data.remote.dto.*

interface AuthApiService {
    
    // EVOwner Registration - POST /api/v1/evowners/register
    @POST("/api/v1/evowners/register")
    suspend fun register(@Body request: RegisterRequest): Response<String>
    
    // EVOwner Login - POST /api/v1/evowners/login
    @POST("/api/v1/evowners/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    // Sign In with Google - POST /api/v1/auth/google
    @POST("/api/v1/auth/google")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): Response<GoogleSignInResponse>
    
    // Get EVOwner Profile by NIC - GET /api/v1/evowners/nic/{nic}
    @GET("/api/v1/evowners/nic/{nic}")
    suspend fun getProfileByNIC(@Path("nic") nic: String): Response<UserDto>
    
    // Get EVOwner Profile by ID - GET /api/v1/evowners/{id}
    @GET("/api/v1/evowners/{id}")
    suspend fun getProfileById(@Path("id") id: String): Response<UserDto>
    
    // Update EVOwner Profile - PUT /api/v1/evowners/{nic}
    @PUT("/api/v1/evowners/{nic}")
    suspend fun updateProfile(
        @Path("nic") nic: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>
    
    // Deactivate EVOwner - POST /api/v1/evowners/{nic}/deactivate
    @POST("/api/v1/evowners/{nic}/deactivate")
    suspend fun deactivate(@Path("nic") nic: String): Response<Unit>
    
    // Deactivate User (Admin) - PUT /api/v1/users/{id}/deactivate
    @PUT("/api/v1/users/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: String): Response<Boolean>
    
    // Reactivate EVOwner - POST /api/v1/evowners/{nic}/reactivate
    @POST("/api/v1/evowners/{nic}/reactivate")
    suspend fun reactivate(@Path("nic") nic: String): Response<Unit>
    
    // Delete EVOwner - DELETE /api/v1/evowners/{nic}
    @DELETE("/api/v1/evowners/{nic}")
    suspend fun deleteAccount(@Path("nic") nic: String): Response<Unit>
}