package lk.chargehere.app.ui.theme

import androidx.compose.ui.unit.dp

// === CLARITY DESIGN SYSTEM SPACING ===
// Strict 8-point grid system for consistent spacing throughout the app

object ClaritySpacing {
    // Base unit - 8dp
    val base = 8.dp
    
    // Standard spacing values (multiples of 8)
    val xs = 4.dp      // 0.5 * 8 = 4dp  (micro spacing)
    val sm = 8.dp      // 1 * 8 = 8dp    (small spacing)
    val md = 16.dp     // 2 * 8 = 16dp   (medium spacing)
    val lg = 24.dp     // 3 * 8 = 24dp   (large spacing)
    val xl = 32.dp     // 4 * 8 = 32dp   (extra large)
    val xxl = 40.dp    // 5 * 8 = 40dp   (extra extra large)
    val xxxl = 48.dp   // 6 * 8 = 48dp   (huge spacing)
    
    // Component-specific spacing
    val cardPadding = md           // 16dp - internal card padding
    val sectionSpacing = lg        // 24dp - between major sections
    val itemSpacing = md           // 16dp - between list items
    val screenPadding = md         // 16dp - screen edge padding
    val buttonHeight = xxxl        // 48dp - standard button height
    val inputHeight = xxxl         // 48dp - standard input field height
    
    // Layout spacing
    val headerSpacing = xl         // 32dp - after headers
    val footerSpacing = xl         // 32dp - before footers
    val modalPadding = lg          // 24dp - modal internal padding
    
    // Micro spacing for fine-tuning
    val micro = 2.dp               // For borders, small adjustments
    val tiny = xs                  // 4dp - very small spacing
}

// Extension properties for easy access
val Int.clarityDp get() = (this * 8).dp  // Convert multiplier to dp
val Double.clarityDp get() = (this * 8).dp