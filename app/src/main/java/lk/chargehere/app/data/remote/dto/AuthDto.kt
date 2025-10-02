package lk.chargehere.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Auth DTOs - Updated for email/password authentication
data class RegisterRequest(
    val nic: String?,
    val email: String?,
    val password: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)

data class LoginRequest(
    val email: String?,
    val password: String?
)

// Google Sign In DTOs
data class GoogleSignInRequest(
    val idToken: String?
)

data class GoogleSignInResponse(
    val accessToken: String,
    val expiresInSeconds: Int,
    val tokenType: String
)

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("token")
    val legacyToken: String? = null // Some endpoints return "token" instead of "accessToken"
) {
    fun getToken(): String = accessToken ?: legacyToken ?: ""
}

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

// Response DTOs for specific endpoints
data class RegisterResponse(
    val nic: String
)

data class LoginResponse(
    val accessToken: String,
    val nic: String
)

data class UserDto(
    val nic: String,
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("isActive")
    val isActive: Boolean = true
)

// Profile DTOs
data class UpdateProfileRequest(
    val nic: String,
    val email: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)

data class MessageResponse(
    val message: String
)
