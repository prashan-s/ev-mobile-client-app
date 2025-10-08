package lk.chargehere.app.ui.screens.operator

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.auth.TokenManager
import lk.chargehere.app.ui.utils.keyboardImePadding

@Composable
fun OperatorHomeScreen(
    onNavigateToQRScanner: () -> Unit,
    onNavigateToAuth: () -> Unit,
    tokenManager: TokenManager = hiltViewModel<OperatorHomeViewModel>().tokenManager
) {
    val stationName = tokenManager.getStationName()

    // Pulse animation for the QR icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ClarityBackgroundGray,
                        ClarityPureWhite
                    )
                )
            )
    ) {
        // Sign out button in top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = onNavigateToAuth,
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = ClaritySurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = ClarityDarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ClaritySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

            // Station name at top with enhanced styling
            if (stationName != null) {
                Text(
                    text = stationName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClarityDarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.xs))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ClaritySuccessGreen, CircleShape)
                    )
                    Text(
                        text = "Station Manager",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityMediumGray
                    )
                }
            } else {
                Text(
                    text = "Station Manager",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClarityDarkGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center QR scanner button with animated design
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
                    // QR icon with animated gradient background
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ClarityAccentBlue.copy(alpha = pulseAlpha),
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
                            modifier = Modifier.size(70.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(ClaritySpacing.xl))

                    Text(
                        text = "Scan Customer QR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ClarityDarkGray
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.xs))

                    Text(
                        text = "Scan the QR code to view booking details and start the charging session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClarityMediumGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(ClaritySpacing.xl))

                    Surface(
                        onClick = onNavigateToQRScanner,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(ClaritySpacing.md),
                        color = ClarityAccentBlue,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = ClarityPureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(ClaritySpacing.sm))
                            Text(
                                text = "Open Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                color = ClarityPureWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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
    var showManualEntry by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityDarkGray)
    ) {
        // Live Camera Preview with QR Scanner
        QRScannerComponent(
            onQRCodeScanned = { qrCode ->
                // Navigate to booking detail with scanned code
                onNavigateToBookingDetail(qrCode)
            },
            showInstructions = !showManualEntry
        )

        // Header with back button and manual entry toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF000000).copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = ClarityPureWhite,
                    modifier = Modifier.size(68.dp)
                )
            }

            // Manual entry toggle
            IconButton(
                onClick = { showManualEntry = !showManualEntry },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF000000).copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (showManualEntry) Icons.Default.QrCodeScanner else Icons.Default.Keyboard,
                    contentDescription = if (showManualEntry) "Scan Mode" else "Manual Entry",
                    tint = ClarityPureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Manual input overlay (optional)
        if (showManualEntry) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF000000).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .keyboardImePadding()
                    .padding(ClaritySpacing.lg)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ClaritySpacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = ClaritySurfaceWhite.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(ClaritySpacing.md)
                    ) {
                        Text(
                            text = "Manual Entry",
                            style = MaterialTheme.typography.titleMedium,
                            color = ClarityPureWhite,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                        ChargeHereTextField(
                            value = scannedCode,
                            onValueChange = { scannedCode = it },
                            label = "Booking ID",
                            placeholder = "Enter booking ID",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                        ClarityPrimaryButton(
                            text = "Continue",
                            onClick = {
                                if (scannedCode.isNotBlank()) {
                                    onNavigateToBookingDetail(scannedCode)
                                }
                            },
                            enabled = scannedCode.isNotBlank(),
                            icon = Icons.Default.ArrowForward,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
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