package lk.chargehere.app.ui.utils

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Keyboard-aware padding utilities for ChargeHere app.
 * Provides smooth animations when keyboard appears/disappears.
 * 
 * Based on Clarity Design System with 8-point grid spacing.
 * Prevents TextFields from being covered by keyboard.
 */

/**
 * Applies smooth animated IME (keyboard) padding to content.
 * Automatically handles keyboard visibility transitions.
 * 
 * Usage:
 * ```
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .keyboardImePadding()
 * ) {
 *     // Content with TextFields
 * }
 * ```
 */
@Composable
fun Modifier.keyboardImePadding(): Modifier {
    return this.imePadding()
}

/**
 * Returns the animated keyboard height that can be used for custom padding.
 * Smoothly animates between 0 and keyboard height.
 * 
 * Usage:
 * ```
 * val keyboardHeight = rememberAnimatedKeyboardHeight()
 * Column(
 *     modifier = Modifier.padding(bottom = keyboardHeight)
 * ) {
 *     // Content
 * }
 * ```
 */
@Composable
fun rememberAnimatedKeyboardHeight(): Dp {
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    
    val animatedHeight by animateDpAsState(
        targetValue = with(density) {
            if (imeHeight > 0) imeHeight.toDp() else 0.dp
        },
        animationSpec = tween(durationMillis = 300),
        label = "keyboard_height_animation"
    )
    
    return animatedHeight
}

/**
 * Applies keyboard-aware padding with smooth animation.
 * Combines IME padding with animated transition.
 * 
 * Parameters:
 * - additionPadding: Extra padding in dp to add on top of keyboard padding
 * 
 * Usage:
 * ```
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .keyboardAwareAnimatedPadding(additionPadding = 16.dp)
 * ) {
 *     ChargeHereTextField(...)
 *     ChargeHereTextField(...)
 * }
 * ```
 */
@Composable
fun Modifier.keyboardAwareAnimatedPadding(additionPadding: Dp = 0.dp): Modifier {
    val keyboardHeight = rememberAnimatedKeyboardHeight()
    return this
        .imePadding()
        .then(
            if (keyboardHeight > 0.dp) {
                Modifier.padding(bottom = additionPadding)
            } else {
                Modifier
            }
        )
}

/**
 * Applies keyboard-aware padding specifically for scrollable content.
 * Ensures keyboard won't cover any content at the bottom of scrollable columns.
 * 
 * Usage:
 * ```
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .verticalScroll(scrollState)
 *         .keyboardAwareScrollPadding()
 * ) {
 *     // Scrollable content with TextFields
 * }
 * ```
 */
@Composable
fun Modifier.keyboardAwareScrollPadding(): Modifier {
    return this.imePadding()
}

/**
 * Custom padding modifier with keyboard animation for specific bottom spacing.
 * Smoothly animates additional padding when keyboard is visible.
 * 
 * Parameters:
 * - baselinePadding: Padding when keyboard is hidden
 * - keyboardOffset: Additional padding when keyboard is visible
 * 
 * Usage:
 * ```
 * Column(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .keyboardAdaptivePadding(
 *             baselinePadding = 24.dp,
 *             keyboardOffset = 16.dp
 *         )
 * )
 * ```
 */
@Composable
fun Modifier.keyboardAdaptivePadding(
    baselinePadding: Dp = 0.dp,
    keyboardOffset: Dp = 8.dp
): Modifier {
    val keyboardHeight = rememberAnimatedKeyboardHeight()
    
    val animatedBottomPadding by animateDpAsState(
        targetValue = if (keyboardHeight > 0.dp) keyboardOffset else baselinePadding,
        animationSpec = tween(durationMillis = 300),
        label = "adaptive_padding_animation"
    )
    
    return this
        .imePadding()
        .padding(bottom = animatedBottomPadding)
}
