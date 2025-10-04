package lk.chargehere.app.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Data class representing API error response
 * Example: {"title":"ChargingStation.SlotNotAvailable","status":409,"detail":"The requested physical slot is not available during the specified time"}
 */
data class ApiErrorResponse(
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null
)

/**
 * Parsed and formatted error for display
 */
data class FormattedError(
    val title: String,
    val description: String
)

object ErrorParser {
    private val gson = Gson()

    /**
     * Parses error body JSON and formats it according to requirements:
     * - Title: Extract from title key, split by '.', take second part, add spaces before capital letters
     * - Description: Extract from detail key
     * - Fallbacks: "Unknown Error" for title, "Unknown Reason" for description
     */
    fun parseAndFormatError(errorBodyJson: String?): FormattedError {
        if (errorBodyJson.isNullOrBlank()) {
            return FormattedError(
                title = "Unknown Error",
                description = "Unknown Reason"
            )
        }

        return try {
            val errorResponse = gson.fromJson(errorBodyJson, ApiErrorResponse::class.java)
            
            val formattedTitle = formatTitle(errorResponse.title)
            val formattedDescription = formatDescription(errorResponse.detail)
            
            FormattedError(
                title = formattedTitle,
                description = formattedDescription
            )
        } catch (e: JsonSyntaxException) {
            // If JSON parsing fails, return the raw error as description
            FormattedError(
                title = "Error",
                description = errorBodyJson
            )
        } catch (e: Exception) {
            FormattedError(
                title = "Unknown Error",
                description = errorBodyJson
            )
        }
    }

    /**
     * Formats the title by:
     * 1. Splitting by '.' and taking the second part (or the whole string if no '.')
     * 2. Adding spaces before capital letters
     * 3. Returns "Unknown Error" if title is null or empty
     */
    private fun formatTitle(title: String?): String {
        if (title.isNullOrBlank()) {
            return "Unknown Error"
        }

        // Split by '.' and take the second part if available
        val parts = title.split(".")
        val relevantPart = if (parts.size > 1) parts[1] else parts[0]

        // Add spaces before capital letters
        return addSpacesBeforeCapitals(relevantPart)
    }

    /**
     * Adds spaces before capital letters in a string
     * Example: "SlotNotAvailable" -> "Slot Not Available"
     */
    private fun addSpacesBeforeCapitals(text: String): String {
        if (text.isEmpty()) return text

        val result = StringBuilder()
        text.forEachIndexed { index, char ->
            if (index > 0 && char.isUpperCase() && text[index - 1].isLowerCase()) {
                result.append(" ")
            }
            result.append(char)
        }
        return result.toString()
    }

    /**
     * Formats the description
     * Returns "Unknown Reason" if detail is null or empty
     */
    private fun formatDescription(detail: String?): String {
        return if (detail.isNullOrBlank()) {
            "Unknown Reason"
        } else {
            detail
        }
    }

    /**
     * Helper function to parse error from response error body
     * Can be used directly in repositories
     */
    fun parseError(errorBodyJson: String?): String {
        val formatted = parseAndFormatError(errorBodyJson)
        return "${formatted.title}: ${formatted.description}"
    }

    /**
     * Helper to get just the title
     */
    fun parseErrorTitle(errorBodyJson: String?): String {
        return parseAndFormatError(errorBodyJson).title
    }

    /**
     * Helper to get just the description
     */
    fun parseErrorDescription(errorBodyJson: String?): String {
        return parseAndFormatError(errorBodyJson).description
    }
}
