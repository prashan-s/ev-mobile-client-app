package lk.chargehere.app.ui.screens.operator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import lk.chargehere.app.auth.TokenManager
import javax.inject.Inject

@HiltViewModel
class OperatorHomeViewModel @Inject constructor(
    val tokenManager: TokenManager
) : ViewModel()
