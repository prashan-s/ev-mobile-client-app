package lk.chargehere.app.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import lk.chargehere.app.data.remote.dto.*

interface OperatorApiService {
    
    @POST("/api/v1/validateSession")
    suspend fun validateSession(@Body request: ValidateSessionRequest): Response<ValidateSessionResponse>
    
    @POST("/api/v1/closeSession")
    suspend fun closeSession(@Body request: CloseSessionRequest): Response<MessageResponse>
}