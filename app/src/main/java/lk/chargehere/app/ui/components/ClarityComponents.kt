package lk.chargehere.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lk.chargehere.app.ui.theme.*
import lk.chargehere.app.utils.ErrorParser

// === CLARITY DESIGN SYSTEM COMPONENTS ===
// Universal components following strict Clarity principles

/**
 * Micro-gradient background following Clarity design
 * From Pure White (#FFFFFF) to Light Gray (#F9FAFB)
 */
@Composable
fun ClarityBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ClarityPureWhite,
                        ClarityBackgroundGray
                    )
                )
            )
    ) {
        content()
    }
}

/**
 * Primary action button - Solid Accent Blue
 */
@Composable
fun ClarityPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp), // 8-point grid: 6 * 8 = 48
        shape = RoundedCornerShape(8.dp), // 8-point grid
        colors = ButtonDefaults.buttonColors(
            containerColor = ClarityAccentBlue,
            contentColor = ClarityPureWhite,
            disabledContainerColor = ClarityLightGray,
            disabledContentColor = ClarityMediumGray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp)) // 8-point grid
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Secondary action button - Outline style
 */
@Composable
fun ClaritySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp), // 8-point grid
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ClarityAccentBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ClarityAccentBlue,
            disabledContentColor = ClarityMediumGray
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Text button for dismissive actions
 */
@Composable
fun ClarityTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = ClarityAccentBlue,
            disabledContentColor = ClarityMediumGray
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Input field with Clarity styling
 * Light gray border that changes to Accent Blue on focus
 */
@Composable
fun ClarityTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        enabled = enabled,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ClarityAccentBlue,
            unfocusedBorderColor = ClarityLightGray,
            errorBorderColor = ClarityErrorRed,
            focusedTextColor = ClarityDarkGray,
            unfocusedTextColor = ClarityDarkGray,
            disabledTextColor = ClarityMediumGray,
            focusedContainerColor = ClarityPureWhite,
            unfocusedContainerColor = ClarityPureWhite,
            disabledContainerColor = ClarityBackgroundGray
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Card with subtle shadow following Clarity principles
 */
@Composable
fun ClarityCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = ClarityShadow,
                spotColor = ClarityShadow
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClaritySurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp), // 8-point grid: 2 * 8 = 16
            content = content
        )
    }
}

/**
 * List item with proper spacing instead of dividers
 */
@Composable
fun ClarityListItem(
    primaryText: String,
    secondaryText: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = Icons.Default.ArrowForward
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 16.dp, horizontal = 16.dp) // 8-point grid
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClarityDarkGray
                )
                if (secondaryText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = ClarityMediumGray
                    )
                }
            }
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = ClarityMediumGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Section header for organizing content
 */
@Composable
fun ClaritySectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = ClarityMediumGray,
        modifier = modifier.padding(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    )
}

/**
 * Label-data pair for detail screens
 */
@Composable
fun ClarityLabelDataPair(
    label: String,
    data: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ClarityMediumGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data,
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityDarkGray
        )
    }
}

/**
 * Toggle switch with Clarity styling
 */
@Composable
fun ClaritySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = ClarityPureWhite,
            checkedTrackColor = ClarityAccentBlue,
            uncheckedThumbColor = ClarityPureWhite,
            uncheckedTrackColor = ClarityLightGray,
            uncheckedBorderColor = ClarityMediumGray
        )
    )
}

/**
 * Status indicator chip
 */
@Composable
fun ClarityStatusChip(
    text: String,
    status: ClarityStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ClarityStatus.Success -> ClaritySuccessGreen.copy(alpha = 0.1f) to ClaritySuccessGreen
        ClarityStatus.Error -> ClarityErrorRed.copy(alpha = 0.1f) to ClarityErrorRed
        ClarityStatus.Warning -> ClarityWarningOrange.copy(alpha = 0.1f) to ClarityWarningOrange
        ClarityStatus.Neutral -> ClarityLightGray to ClarityMediumGray
    }
    
    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

enum class ClarityStatus {
    Success, Error, Warning, Neutral
}

/**
 * Modal overlay with semi-transparent background
 */
@Composable
fun ClarityModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityOverlay)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ClaritySurfaceWhite)
                .clickable(enabled = false) { }
        ) {
            content()
        }
    }
}

/**
 * Formatted error display component
 * Automatically parses error JSON and displays title and description
 */
@Composable
fun ClarityFormattedError(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    val formattedError = remember(errorMessage) {
        ErrorParser.parseAndFormatError(errorMessage)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ClaritySpacing.sm))
            .background(ClarityErrorRed.copy(alpha = 0.1f))
            .padding(ClaritySpacing.md),
        verticalArrangement = Arrangement.spacedBy(ClaritySpacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ClarityErrorRed,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedError.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = ClarityErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedError.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClarityErrorRed.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Inline error banner with formatted error display
 */
@Composable
fun ClarityErrorBanner(
    errorMessage: String?,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (errorMessage != null) {
        val formattedError = remember(errorMessage) {
            ErrorParser.parseAndFormatError(errorMessage)
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ClaritySpacing.sm))
                .background(ClarityErrorRed.copy(alpha = 0.1f))
                .padding(ClaritySpacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = ClarityErrorRed,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedError.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = ClarityErrorRed,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedError.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = ClarityErrorRed.copy(alpha = 0.9f)
                    )
                }
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = ClarityErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Duration selector with preset options and custom slider
 */
@Composable
fun ClarityDurationSelector(
    selectedDurationMinutes: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minDuration: Int = 15,
    maxDuration: Int = 240
) {
    val presetDurations = listOf(15, 30, 60, 120, 180, 240)

    Column(modifier = modifier) {
        // Preset duration chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetDurations.take(4).forEach { duration ->
                val isSelected = selectedDurationMinutes == duration
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) ClarityAccentBlue else ClarityPureWhite
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) ClarityAccentBlue else ClarityLightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onDurationSelected(duration) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${duration}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) ClarityPureWhite else ClarityDarkGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "min",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) ClarityPureWhite else ClarityMediumGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom slider for fine-tuning
        Column {
            Text(
                text = "Custom duration",
                style = MaterialTheme.typography.labelMedium,
                color = ClarityMediumGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = selectedDurationMinutes.toFloat(),
                onValueChange = { onDurationSelected(it.toInt()) },
                valueRange = minDuration.toFloat()..maxDuration.toFloat(),
                steps = (maxDuration - minDuration) / 15 - 1,
                colors = SliderDefaults.colors(
                    thumbColor = ClarityAccentBlue,
                    activeTrackColor = ClarityAccentBlue,
                    inactiveTrackColor = ClarityLightGray
                )
            )
            Text(
                text = "${selectedDurationMinutes} minutes",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityDarkGray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * Physical slot selector with visual grid
 */
@Composable
fun ClaritySlotSelector(
    totalSlots: Int,
    availableSlots: Int,
    selectedSlot: Int,
    onSlotSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Select Charging Slot",
            style = MaterialTheme.typography.titleMedium,
            color = ClarityDarkGray,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$availableSlots of $totalSlots slots available",
            style = MaterialTheme.typography.bodyMedium,
            color = ClarityMediumGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of slots (2 columns)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (row in 0 until (totalSlots + 1) / 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (col in 0..1) {
                        val slotNumber = row * 2 + col + 1
                        if (slotNumber <= totalSlots) {
                            val isAvailable = slotNumber <= availableSlots
                            val isSelected = slotNumber == selectedSlot

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> ClarityAccentBlue
                                            isAvailable -> ClarityPureWhite
                                            else -> ClarityLightGray
                                        }
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = when {
                                            isSelected -> ClarityAccentBlue
                                            isAvailable -> ClarityLightGray
                                            else -> ClarityMediumGray.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = isAvailable) {
                                        onSlotSelected(slotNumber)
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Slot $slotNumber",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = when {
                                            isSelected -> ClarityPureWhite
                                            isAvailable -> ClarityDarkGray
                                            else -> ClarityMediumGray
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isAvailable) "Available" else "Occupied",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isSelected -> ClarityPureWhite.copy(alpha = 0.9f)
                                            isAvailable -> ClaritySuccessGreen
                                            else -> ClarityErrorRed
                                        }
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step indicator for multi-step flows
 */
@Composable
fun ClarityStepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        if (step <= currentStep) ClarityAccentBlue else ClarityLightGray
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (step <= currentStep) ClarityPureWhite else ClarityMediumGray,
                    fontWeight = FontWeight.Bold
                )
            }

            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(2.dp)
                        .background(
                            if (step < currentStep) ClarityAccentBlue else ClarityLightGray
                        )
                )
            }
        }
    }
}

/**
 * Shimmer loading effect for skeleton screens
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        ClarityLightGray,
                        ClarityLightGray.copy(alpha = 0.5f),
                        ClarityLightGray
                    ),
                    start = Offset(translateAnim - 1000f, translateAnim - 1000f),
                    end = Offset(translateAnim, translateAnim)
                )
            )
    )
}

/**
 * Shimmer loading skeleton for booking cards
 */
@Composable
fun BookingCardShimmer(modifier: Modifier = Modifier) {
    ClarityCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(ClaritySpacing.md)) {
            // Title shimmer
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.sm))

            // Highlighted info rows
            repeat(3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ClaritySpacing.sm),
                    colors = CardDefaults.cardColors(
                        containerColor = ClarityLightGray.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ClaritySpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon shimmer
                        ShimmerEffect(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(ClaritySpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            ShimmerEffect(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerEffect(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Success toast notification with icon
 */
@Composable
fun ClaritySuccessToast(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.CheckCircle
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClaritySurfaceWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ClaritySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ClaritySuccessGreen.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ClaritySuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = ClarityDarkGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}