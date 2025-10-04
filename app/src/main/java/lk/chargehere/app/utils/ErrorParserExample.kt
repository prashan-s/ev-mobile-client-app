package lk.chargehere.app.utils

/**
 * Example usage of ErrorParser for formatting API error responses
 */
object ErrorParserExample {

    /**
     * Example 1: Slot not available error (409 Conflict)
     */
    fun exampleSlotNotAvailable() {
        val apiErrorJson = """
        {
          "title": "ChargingStation.SlotNotAvailable",
          "status": 409,
          "detail": "The requested physical slot is not available during the specified time"
        }
        """.trimIndent()

        val formatted = ErrorParser.parseAndFormatError(apiErrorJson)
        
        // Output:
        // Title: "Slot Not Available"
        // Description: "The requested physical slot is not available during the specified time"
        
        println("Title: ${formatted.title}")
        println("Description: ${formatted.description}")
    }

    /**
     * Example 2: Invalid credentials error (401 Unauthorized)
     */
    fun exampleInvalidCredentials() {
        val apiErrorJson = """
        {
          "title": "Authentication.InvalidCredentials",
          "status": 401,
          "detail": "The email or password you entered is incorrect"
        }
        """.trimIndent()

        val formatted = ErrorParser.parseAndFormatError(apiErrorJson)
        
        // Output:
        // Title: "Invalid Credentials"
        // Description: "The email or password you entered is incorrect"
        
        println("Title: ${formatted.title}")
        println("Description: ${formatted.description}")
    }

    /**
     * Example 3: Empty title (should default to "Unknown Error")
     */
    fun exampleEmptyTitle() {
        val apiErrorJson = """
        {
          "title": "",
          "status": 500,
          "detail": "Internal server error occurred"
        }
        """.trimIndent()

        val formatted = ErrorParser.parseAndFormatError(apiErrorJson)
        
        // Output:
        // Title: "Unknown Error"
        // Description: "Internal server error occurred"
        
        println("Title: ${formatted.title}")
        println("Description: ${formatted.description}")
    }

    /**
     * Example 4: Missing detail (should default to "Unknown Reason")
     */
    fun exampleMissingDetail() {
        val apiErrorJson = """
        {
          "title": "Reservation.NotFound",
          "status": 404
        }
        """.trimIndent()

        val formatted = ErrorParser.parseAndFormatError(apiErrorJson)
        
        // Output:
        // Title: "Not Found"
        // Description: "Unknown Reason"
        
        println("Title: ${formatted.title}")
        println("Description: ${formatted.description}")
    }

    /**
     * Example 5: Title without dot separator
     */
    fun exampleSimpleTitle() {
        val apiErrorJson = """
        {
          "title": "BadRequest",
          "status": 400,
          "detail": "The request was malformed"
        }
        """.trimIndent()

        val formatted = ErrorParser.parseAndFormatError(apiErrorJson)
        
        // Output:
        // Title: "Bad Request"
        // Description: "The request was malformed"
        
        println("Title: ${formatted.title}")
        println("Description: ${formatted.description}")
    }

    /**
     * Example 6: Using the helper method for combined output
     */
    fun exampleHelperMethod() {
        val apiErrorJson = """
        {
          "title": "ChargingStation.SlotNotAvailable",
          "status": 409,
          "detail": "The requested physical slot is not available during the specified time"
        }
        """.trimIndent()

        val combinedError = ErrorParser.parseError(apiErrorJson)
        
        // Output:
        // "Slot Not Available: The requested physical slot is not available during the specified time"
        
        println(combinedError)
    }

    /**
     * Example 7: Null or empty input
     */
    fun exampleNullInput() {
        val formatted1 = ErrorParser.parseAndFormatError(null)
        val formatted2 = ErrorParser.parseAndFormatError("")
        
        // Both outputs:
        // Title: "Unknown Error"
        // Description: "Unknown Reason"
        
        println("Null input - Title: ${formatted1.title}, Description: ${formatted1.description}")
        println("Empty input - Title: ${formatted2.title}, Description: ${formatted2.description}")
    }

    /**
     * Example 8: Invalid JSON (malformed)
     */
    fun exampleInvalidJson() {
        val invalidJson = "This is not valid JSON"

        val formatted = ErrorParser.parseAndFormatError(invalidJson)
        
        // Output:
        // Title: "Error"
        // Description: "This is not valid JSON"
        
        println("Title: ${formatted.title}")
        println("Description: ${formatted.description}")
    }

    /**
     * How it's used in repositories:
     */
    fun repositoryExample() {
        // In ReservationRepository.kt:
        // 
        // } else {
        //     val errorBody = response.errorBody()?.string()
        //     val formattedError = ErrorParser.parseError(errorBody)
        //     Result.Error(formattedError)
        // }
        
        // The formatted error then flows through:
        // Repository → ViewModel → UI Component
        // 
        // And is displayed directly in UI:
        // Text(text = uiState.error, ...)
    }
}
