package lk.chargehere.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import lk.chargehere.app.ui.components.ChargeHereButton
import lk.chargehere.app.ui.components.ButtonVariant
import lk.chargehere.app.ui.theme.*

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToStationManagerLogin: () -> Unit = {}
) {
    val pages = listOf(
        OnboardingPage(
            title = "Find Stations",
            description = "Discover compatible charging stations near you with real-time availability.",
            icon = "🔍",
            gradientColors = listOf(ClarityAccentBlue.copy(alpha = 0.1f), ClarityPureWhite)
        ),
        OnboardingPage(
            title = "Reserve & Plan",
            description = "Plan your trips with confidence by reserving charging slots in advance.",
            icon = "📅",
            gradientColors = listOf(ClaritySuccessGreen.copy(alpha = 0.1f), ClarityPureWhite)
        ),
        OnboardingPage(
            title = "Track & Manage",
            description = "Easily track your bookings and manage charging sessions from one place.",
            icon = "⚡",
            gradientColors = listOf(ClarityWarningOrange.copy(alpha = 0.1f), ClarityPureWhite)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClarityBackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Modern Logo with gradient
            Box(
                modifier = Modifier
                    .size(100.dp)
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
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 56.sp
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.md))

            Text(
                text = "ChargeHere",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ClarityDarkGray
            )

            Text(
                text = "Smart EV Charging",
                style = MaterialTheme.typography.bodyMedium,
                color = ClarityMediumGray,
                modifier = Modifier.padding(top = ClaritySpacing.xs)
            )

            Spacer(modifier = Modifier.height(ClaritySpacing.xxxl))

            // Modern Pager with animation
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            // Modern Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(ClaritySpacing.xs),
                modifier = Modifier.padding(vertical = ClaritySpacing.lg)
            ) {
                repeat(pages.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 32.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) {
                                    ClarityAccentBlue
                                } else {
                                    ClarityMediumGray.copy(alpha = 0.4f)
                                }
                            )
                    )
                }
            }

            // Action buttons
            AnimatedVisibility(
                visible = isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ChargeHereButton(
                        text = "Get Started",
                        onClick = onNavigateToLogin,
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChargeHereButton(
                        text = "Skip",
                        onClick = onNavigateToLogin,
                        variant = ButtonVariant.Tertiary,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(ClaritySpacing.md))

                    ChargeHereButton(
                        text = "Next",
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.lg))

            // Subtle station operator button
            TextButton(
                onClick = onNavigateToStationManagerLogin,
                modifier = Modifier.padding(vertical = ClaritySpacing.sm)
            ) {
                Text(
                    text = "Station Operator Sign In",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClarityMediumGray.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(ClaritySpacing.md))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Modern Icon with gradient background
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = page.gradientColors
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = page.icon,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 72.sp
            )
        }

        Spacer(modifier = Modifier.height(ClaritySpacing.xl))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = ClarityDarkGray
        )

        Spacer(modifier = Modifier.height(ClaritySpacing.md))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = ClarityDarkGray.copy(alpha = 0.85f),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
            modifier = Modifier.padding(horizontal = ClaritySpacing.lg)
        )
    }
}