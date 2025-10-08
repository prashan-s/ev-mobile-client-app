package lk.chargehere.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.ui.utils.keyboardImePadding

@Composable
fun StationManagerLoginScreen(
    onNavigateToOperatorHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: StationManagerLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onNavigateToOperatorHome()
            viewModel.onNavigationHandled()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityBackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ClarityDarkGray
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.xl))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .keyboardImePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                // Modern Logo with gradient
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ClarityAccentBlue.copy(alpha = 0.2f),
                                    ClarityAccentBlue.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(ClaritySpacing.md))

                Text(
                    text = "Station Operator",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClarityDarkGray,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sign in to manage your station",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClarityMediumGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = ClaritySpacing.xs, bottom = ClaritySpacing.xxxl)
                )

                // Email field
                ChargeHereTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = "Email address",
                    placeholder = "operator@station.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.md))

                // Password field
                ChargeHereTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = "Password",
                    placeholder = "Enter your password",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        viewModel.login()
                    },
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(ClaritySpacing.md))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClaritySpacing.sm),
                        colors = CardDefaults.cardColors(
                            containerColor = ClarityErrorRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClarityErrorRed,
                            modifier = Modifier.padding(ClaritySpacing.md)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ClaritySpacing.xl))

                // Sign in button
                ChargeHereButton(
                    text = if (uiState.isLoading) "Signing in..." else "Sign In",
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.login()
                    },
                    loading = uiState.isLoading,
                    enabled = !uiState.isLoading,
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
