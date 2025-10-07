package lk.chargehere.app.ui.screens.operator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.auth.TokenManager

@Composable
fun OperatorHomeScreen(
    onNavigateToQRScanner: () -> Unit,
    onNavigateToAuth: () -> Unit,
    tokenManager: TokenManager = hiltViewModel<OperatorHomeViewModel>().tokenManager
) {
    val stationName = tokenManager.getStationName()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityBackgroundGray)
    ) {
        // Sign out button in top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onNavigateToAuth,
                modifier = Modifier
                    .size(40.dp)
                    .background(ClaritySurfaceWhite, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sign Out",
                    tint = ClarityDarkGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ClaritySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

            // Station name at top
            if (stationName != null) {
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClarityDarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.xs))

                Text(
                    text = "Station Manager",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClarityMediumGray
                )
            } else {
                Text(
                    text = "Station Manager",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClarityDarkGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center QR scanner button with modern design
            ClarityCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ClaritySpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // QR icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ClarityAccentBlue.copy(alpha = 0.15f),
                                        ClarityAccentBlue.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "QR Scanner",
                            tint = ClarityAccentBlue,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(ClaritySpacing.lg))

                    Text(
                        text = "Scan Customer QR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ClarityDarkGray
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.xs))

                    Text(
                        text = "Scan the QR code to view booking details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityMediumGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.lg))

                    ClarityPrimaryButton(
                        text = "Open Scanner",
                        onClick = onNavigateToQRScanner,
                        icon = Icons.Default.QrCodeScanner,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun QRScannerScreen(
    onNavigateToSessionValidation: (String) -> Unit,
    onNavigateToBookingDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var scannedCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityDarkGray)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(ClaritySurfaceWhite.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = ClarityPureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ClaritySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

            // Camera preview placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(ClarityDarkGray.copy(alpha = 0.5f), RoundedCornerShape(ClaritySpacing.md)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Scanner",
                        tint = ClarityPureWhite,
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.md))

                    Text(
                        text = "Position QR code within frame",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ClarityPureWhite,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Camera scanning will be activated here",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClarityPureWhite.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = ClaritySpacing.xs)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Manual input for testing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ClaritySpacing.md),
                colors = CardDefaults.cardColors(
                    containerColor = ClaritySurfaceWhite.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(ClaritySpacing.md)
                ) {
                    Text(
                        text = "Test Mode - Manual Entry",
                        style = MaterialTheme.typography.labelMedium,
                        color = ClarityPureWhite.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                    ChargeHereTextField(
                        value = scannedCode,
                        onValueChange = { scannedCode = it },
                        label = "Booking ID",
                        placeholder = "Enter booking ID for testing",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                    ClarityPrimaryButton(
                        text = "Navigate to Booking",
                        onClick = {
                            if (scannedCode.isNotBlank()) {
                                onNavigateToBookingDetail(scannedCode)
                            }
                        },
                        enabled = scannedCode.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))
        }
    }
}

@Composable
fun SessionValidationScreen(
    sessionId: String,
    onNavigateToSessionDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Session Validation",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Session ID: $sessionId",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Text(
            text = "Verify customer and vehicle details",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = { onNavigateToSessionDetail(sessionId) }) {
            Text("Validate & Start Session")
        }
        
        Button(onClick = onNavigateBack) {
            Text("Cancel")
        }
    }
}

@Composable
fun SessionDetailScreen(
    sessionId: String,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Active Session",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Session ID: $sessionId",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Text(
            text = "Monitor charging progress and close when complete",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onNavigateToHome) {
            Text("Close Session & Return Home")
        }
    }
}