package lk.chargehere.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lk.chargehere.app.data.local.dao.OperatorSessionDao
import lk.chargehere.app.data.mapper.*
import lk.chargehere.app.data.remote.api.OperatorApiService
import lk.chargehere.app.data.remote.dto.*
import lk.chargehere.app.domain.model.*
import lk.chargehere.app.utils.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatorRepository @Inject constructor(
    private val operatorApiService: OperatorApiService,
    private val operatorSessionDao: OperatorSessionDao
) {
    
    suspend fun validateSession(reservationId: String, userId: String): Result<ValidateSessionResponse> {
        return try {
            val request = ValidateSessionRequest(reservationId, userId)
            val response = operatorApiService.validateSession(request)
            
            if (response.isSuccessful) {
                val validationResponse = response.body()
                if (validationResponse != null) {
                    // Save session to local database if valid
                    if (validationResponse.valid && validationResponse.reservation != null) {
                        val operatorSession = OperatorSession(
                            sessionId = "session_${reservationId}_${System.currentTimeMillis()}",
                            reservationId = reservationId,
                            operatorId = userId, // Current user acting as operator
                            status = SessionStatus.VALIDATED,
                            validationTimestamp = System.currentTimeMillis()
                        )
                        operatorSessionDao.insertSession(operatorSession.toEntity())
                    }
                    
                    Result.Success(validationResponse)
                } else {
                    Result.Error("Validation failed: No response data received")
                }
            } else {
                Result.Error("Session validation failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    suspend fun closeSession(reservationId: String): Result<Unit> {
        return try {
            val request = CloseSessionRequest(reservationId)
            val response = operatorApiService.closeSession(request)
            
            if (response.isSuccessful) {
                // Update local session
                operatorSessionDao.closeSessionByReservation(reservationId)
                Result.Success(Unit)
            } else {
                Result.Error("Close session failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    fun observeOperatorSessions(operatorId: String): Flow<List<OperatorSession>> {
        return operatorSessionDao.observeSessionsByOperator(operatorId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getSessionById(sessionId: String): OperatorSession? {
        return operatorSessionDao.getSessionById(sessionId)?.toDomain()
    }
    
    suspend fun getSessionByReservation(reservationId: String): OperatorSession? {
        return operatorSessionDao.getSessionByReservation(reservationId)?.toDomain()
    }
}