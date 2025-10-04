package lk.chargehere.app.utils

import org.junit.Test
import org.junit.Assert.*

class ErrorParserTest {

    @Test
    fun `test parseAndFormatError with valid API error response`() {
        val errorJson = """{"title":"ChargingStation.SlotNotAvailable","status":409,"detail":"The requested physical slot is not available during the specified time"}"""
        
        val result = ErrorParser.parseAndFormatError(errorJson)
        
        assertEquals("Slot Not Available", result.title)
        assertEquals("The requested physical slot is not available during the specified time", result.description)
    }

    @Test
    fun `test parseAndFormatError with empty title`() {
        val errorJson = """{"title":"","status":400,"detail":"Some error occurred"}"""
        
        val result = ErrorParser.parseAndFormatError(errorJson)
        
        assertEquals("Unknown Error", result.title)
        assertEquals("Some error occurred", result.description)
    }

    @Test
    fun `test parseAndFormatError with null title`() {
        val errorJson = """{"status":400,"detail":"Some error occurred"}"""
        
        val result = ErrorParser.parseAndFormatError(errorJson)
        
        assertEquals("Unknown Error", result.title)
        assertEquals("Some error occurred", result.description)
    }

    @Test
    fun `test parseAndFormatError with empty detail`() {
        val errorJson = """{"title":"Error.Type","status":400,"detail":""}"""
        
        val result = ErrorParser.parseAndFormatError(errorJson)
        
        assertEquals("Type", result.title)
        assertEquals("Unknown Reason", result.description)
    }

    @Test
    fun `test parseAndFormatError with null detail`() {
        val errorJson = """{"title":"Error.Type","status":400}"""
        
        val result = ErrorParser.parseAndFormatError(errorJson)
        
        assertEquals("Type", result.title)
        assertEquals("Unknown Reason", result.description)
    }

    @Test
    fun `test parseAndFormatError with null or empty input`() {
        val resultNull = ErrorParser.parseAndFormatError(null)
        assertEquals("Unknown Error", resultNull.title)
        assertEquals("Unknown Reason", resultNull.description)

        val resultEmpty = ErrorParser.parseAndFormatError("")
        assertEquals("Unknown Error", resultEmpty.title)
        assertEquals("Unknown Reason", resultEmpty.description)
    }

    @Test
    fun `test parseAndFormatError with title without dot`() {
        val errorJson = """{"title":"InvalidRequest","status":400,"detail":"Bad request"}"""
        
        val result = ErrorParser.parseAndFormatError(errorJson)
        
        assertEquals("Invalid Request", result.title)
        assertEquals("Bad request", result.description)
    }

    @Test
    fun `test parseAndFormatError with complex camelCase title`() {
        val errorJson = """{"title":"User.AccountNotFound","status":404,"detail":"User account does not exist"}"""
        
        val result = ErrorParser.parseAndFormatError(errorJson)
        
        assertEquals("Account Not Found", result.title)
        assertEquals("User account does not exist", result.description)
    }

    @Test
    fun `test parseError helper returns formatted string`() {
        val errorJson = """{"title":"ChargingStation.SlotNotAvailable","status":409,"detail":"The requested physical slot is not available during the specified time"}"""
        
        val result = ErrorParser.parseError(errorJson)
        
        assertEquals("Slot Not Available: The requested physical slot is not available during the specified time", result)
    }

    @Test
    fun `test parseErrorTitle helper`() {
        val errorJson = """{"title":"ChargingStation.SlotNotAvailable","status":409,"detail":"Some detail"}"""
        
        val result = ErrorParser.parseErrorTitle(errorJson)
        
        assertEquals("Slot Not Available", result)
    }

    @Test
    fun `test parseErrorDescription helper`() {
        val errorJson = """{"title":"ChargingStation.SlotNotAvailable","status":409,"detail":"The requested physical slot is not available during the specified time"}"""
        
        val result = ErrorParser.parseErrorDescription(errorJson)
        
        assertEquals("The requested physical slot is not available during the specified time", result)
    }

    @Test
    fun `test addSpacesBeforeCapitals with various inputs`() {
        // This is indirectly tested through formatTitle
        val testCases = mapOf(
            """{"title":"Test.OneTwo","status":400,"detail":"Test"}""" to "One Two",
            """{"title":"Test.ABC","status":400,"detail":"Test"}""" to "ABC",
            """{"title":"Test.SingleWord","status":400,"detail":"Test"}""" to "Single Word",
            """{"title":"Test.HTTPError","status":400,"detail":"Test"}""" to "HTTPError"
        )

        testCases.forEach { (json, expectedTitle) ->
            val result = ErrorParser.parseAndFormatError(json)
            assertEquals(expectedTitle, result.title)
        }
    }

    @Test
    fun `test parseAndFormatError with invalid JSON returns error as description`() {
        val invalidJson = "This is not valid JSON"
        
        val result = ErrorParser.parseAndFormatError(invalidJson)
        
        assertEquals("Error", result.title)
        assertEquals(invalidJson, result.description)
    }
}
