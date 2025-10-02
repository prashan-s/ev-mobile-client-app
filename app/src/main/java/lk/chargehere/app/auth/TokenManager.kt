package lk.chargehere.app.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences: SharedPreferences = createEncryptedPreferences()

    /**
     * Creates EncryptedSharedPreferences with error handling.
     * If creation fails due to corrupted data (AEADBadTagException),
     * deletes the corrupted file and recreates it.
     */
    private fun createEncryptedPreferences(): SharedPreferences {
        val prefsFileName = "chargehere_secure_prefs"

        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                prefsFileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Log the error
            Log.e("TokenManager", "Failed to create EncryptedSharedPreferences: ${e.javaClass.simpleName}", e)

            // Check if it's a crypto error (AEADBadTagException or similar)
            val isCryptoError = isCryptoException(e)

            if (isCryptoError) {
                Log.w("TokenManager", "Detected crypto exception, attempting to recover by deleting corrupted files")
                try {
                    // Delete the corrupted preferences file
                    val prefsFile = File("${context.filesDir.parent}/shared_prefs/$prefsFileName.xml")
                    if (prefsFile.exists()) {
                        val deleted = prefsFile.delete()
                        Log.i("TokenManager", "Deleted corrupted preferences file: $deleted")
                    }

                    // Also try to delete the master key if it exists
                    val masterKeyFile = File("${context.filesDir.parent}/shared_prefs/_androidx_security_master_key_.xml")
                    if (masterKeyFile.exists()) {
                        val deleted = masterKeyFile.delete()
                        Log.i("TokenManager", "Deleted corrupted master key file: $deleted")
                    }

                    // Recreate the master key and preferences
                    val newMasterKey = MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()

                    EncryptedSharedPreferences.create(
                        context,
                        prefsFileName,
                        newMasterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                } catch (e2: Exception) {
                    Log.e("TokenManager", "Failed to recreate EncryptedSharedPreferences, falling back to regular SharedPreferences", e2)
                    // Fall back to regular SharedPreferences as last resort
                    context.getSharedPreferences("chargehere_prefs_fallback", Context.MODE_PRIVATE)
                }
            } else {
                // For non-crypto errors, fall back to regular SharedPreferences
                Log.w("TokenManager", "Using fallback SharedPreferences due to non-crypto error")
                context.getSharedPreferences("chargehere_prefs_fallback", Context.MODE_PRIVATE)
            }
        }
    }

    /**
     * Checks if an exception is related to cryptographic operations.
     * This includes AEADBadTagException and other crypto-related exceptions.
     */
    private fun isCryptoException(e: Exception): Boolean {
        // Check the exception itself
        if (e is java.security.GeneralSecurityException ||
            e is javax.crypto.AEADBadTagException) {
            return true
        }

        // Check the cause chain
        var cause = e.cause
        while (cause != null) {
            if (cause is java.security.GeneralSecurityException ||
                cause is javax.crypto.AEADBadTagException) {
                return true
            }
            cause = cause.cause
        }

        // Check exception message for crypto-related keywords
        val message = e.message?.lowercase() ?: ""
        return message.contains("aead") ||
               message.contains("decrypt") ||
               message.contains("crypto") ||
               message.contains("keystore")
    }
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_NIC = "user_nic"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        val expirationTime = System.currentTimeMillis() + (expiresIn * 1000)

        Log.d("TokenManager", "Saving tokens - accessToken preview: ${accessToken.take(50)}...")
        Log.d("TokenManager", "Token expiration time: $expirationTime")

        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_TOKEN_EXPIRES_AT, expirationTime)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }

        Log.d("TokenManager", "Tokens saved successfully")
    }

    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        Log.d("TokenManager", "Getting access token - exists: ${token != null}")
        if (token != null) {
            Log.d("TokenManager", "Token preview: ${token.take(50)}...")
        }
        return token
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun isTokenExpired(): Boolean {
        val expirationTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRES_AT, 0)
        return System.currentTimeMillis() >= expirationTime
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && 
               !getAccessToken().isNullOrEmpty() &&
               !isTokenExpired()
    }
    
    fun saveUserInfo(userId: String, email: String, role: String, nic: String? = null) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            if (nic != null) {
                putString(KEY_USER_NIC, nic)
            }
            apply()
        }
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }
    
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }
    
    fun getUserNic(): String? {
        val nic = sharedPreferences.getString(KEY_USER_NIC, null)

        // Validate that NIC is not a UUID (from old implementation)
        // Valid NIC should be alphanumeric without dashes
        if (nic != null && nic.contains("-")) {
            // This is likely a UUID, clear it
            sharedPreferences.edit().remove(KEY_USER_NIC).apply()
            return null
        }

        return nic
    }
    
    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRES_AT)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_NIC)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}