package lk.chargehere.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lk.chargehere.app.ui.theme.*

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