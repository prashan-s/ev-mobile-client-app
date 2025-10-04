package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lk.chargehere.app.ui.components.*
import lk.chargehere.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationConfirmationScreen(
    reservationId: String,
    onNavigateToHome: () -> Unit,
    onNavigateToReservations: () -> Unit
) {
    // === CLARITY DESIGN SYSTEM IMPLEMENTATION ===
    Box(modifier = Modifier.fillMaxSize()) {
        ClarityBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ClaritySpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

                // Success Icon with animated background
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(ClaritySuccessGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = ClaritySuccessGreen,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(ClaritySpacing.xl))

                // Success Message
                Text(
                    text = "Booking Confirmed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClarityDarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.sm))

                Text(
                    text = "Your reservation has been successfully created.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClarityMediumGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(ClaritySpacing.xl))

                // Booking Details Card
                ClarityCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
                    ) {
                        Text(
                            text = "Booking Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ClarityDarkGray
                        )

                        Divider(
                            color = ClarityLightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = ClaritySpacing.xs)
                        )

                        ConfirmationDetailRow(
                            icon = Icons.Default.ConfirmationNumber,
                            label = "Booking ID",
                            value = "#${reservationId.takeLast(8).uppercase()}"
                        )

                        ConfirmationDetailRow(
                            icon = Icons.Default.CheckCircle,
                            label = "Status",
                            value = "Confirmed",
                            valueColor = ClaritySuccessGreen
                        )

                        // Success message box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(ClaritySpacing.sm))
                                .background(ClaritySuccessGreen.copy(alpha = 0.1f))
                                .padding(ClaritySpacing.md)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ClaritySuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "You'll receive a confirmation shortly",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClaritySuccessGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ClaritySpacing.xl))

                // Next Steps Section
                ClarityCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
                    ) {
                        Text(
                            text = "What's Next?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ClarityDarkGray
                        )

                        NextStepItem(
                            number = "1",
                            title = "Check Your Bookings",
                            description = "View your reservation details and QR code"
                        )

                        NextStepItem(
                            number = "2",
                            title = "Arrive on Time",
                            description = "Make sure to arrive at the station on time"
                        )

                        NextStepItem(
                            number = "3",
                            title = "Show QR Code",
                            description = "Present your QR code to start charging"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ClaritySpacing.xl))

                // Action Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(ClaritySpacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ClarityPrimaryButton(
                        text = "View My Reservations",
                        onClick = onNavigateToReservations,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.EventAvailable
                    )

                    ClaritySecondaryButton(
                        text = "Back to Home",
                        onClick = onNavigateToHome,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))
            }
        }
    }
}

@Composable
private fun ConfirmationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = ClarityDarkGray
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ClarityAccentBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(ClaritySpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NextStepItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
    ) {
        // Number Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ClarityAccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = ClarityPureWhite,
                fontWeight = FontWeight.Bold
            )
        }

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = ClarityDarkGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ClarityMediumGray
            )
        }
    }
}
