package lk.chargehere.app.ui.screens.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.auth.AuthManager
import lk.chargehere.app.auth.TokenManager
import javax.inject.Inject

@HiltViewModel
class OperatorHomeViewModel @Inject constructor(
    val tokenManager: TokenManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _signOutState = MutableStateFlow<SignOutState>(SignOutState.Idle)
    val signOutState: StateFlow<SignOutState> = _signOutState

    fun signOut() {
        viewModelScope.launch {
            _signOutState.value = SignOutState.Loading
            try {
                authManager.signOut()
                _signOutState.value = SignOutState.Success
            } catch (e: Exception) {
                _signOutState.value = SignOutState.Error(e.message ?: "Sign out failed")
            }
        }
    }
}

sealed class SignOutState {
    data object Idle : SignOutState()
    data object Loading : SignOutState()
    data object Success : SignOutState()
    data class Error(val message: String) : SignOutState()
}
