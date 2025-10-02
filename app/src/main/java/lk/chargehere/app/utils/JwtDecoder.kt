package lk.chargehere.app.utils

import android.util.Base64
import org.json.JSONObject

/**
 * Simple JWT decoder to extract claims from JWT token
 * Does NOT verify signature - only for reading payload
 */
object JwtDecoder {

    /**
     * Decode JWT token and extract NIC from claims
     * @param token JWT token string
     * @return NIC if found, null otherwise
     */
    fun extractNicFromToken(token: String): String? {
        return try {
            val payload = getPayload(token)
            payload.optString("nic").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode JWT token and extract user ID from claims
     * @param token JWT token string
     * @return User ID if found, null otherwise
     */
    fun extractUserIdFromToken(token: String): String? {
        return try {
            val payload = getPayload(token)
            // Try different common claim names for user ID
            payload.optString("sub").takeIf { it.isNotBlank() }
                ?: payload.optString("userId").takeIf { it.isNotBlank() }
                ?: payload.optString("id").takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode JWT token and get full payload as JSONObject
     * @param token JWT token string
     * @return JSONObject containing all claims
     */
    private fun getPayload(token: String): JSONObject {
        // JWT format: header.payload.signature
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT token format")
        }

        // Decode the payload (second part)
        val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
        return JSONObject(payloadJson)
    }

    /**
     * Get all claims from JWT token as Map
     * @param token JWT token string
     * @return Map of claim name to value
     */
    fun getAllClaims(token: String): Map<String, Any?> {
        return try {
            val payload = getPayload(token)
            val claims = mutableMapOf<String, Any?>()
            payload.keys().forEach { key ->
                claims[key] = payload.get(key)
            }
            claims
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
