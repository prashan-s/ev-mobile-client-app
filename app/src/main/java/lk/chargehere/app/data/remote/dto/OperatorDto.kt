package lk.chargehere.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Operator DTOs - Updated to match OpenAPI spec
data class ValidateSessionRequest(
    @SerializedName("reservation_id")
    val reservationId: String,
    @SerializedName("user_id")
    val userId: String
)

data class ValidateSessionResponse(
    val valid: Boolean,
    val message: String,
    val reservation: ReservationDto?
)

data class CloseSessionRequest(
    @SerializedName("reservation_id")
    val reservationId: String
)