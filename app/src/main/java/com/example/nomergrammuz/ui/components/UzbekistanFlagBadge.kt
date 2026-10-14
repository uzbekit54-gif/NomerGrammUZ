package com.example.nomergrammuz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.ui.theme.FlagBlue
import com.example.nomergrammuz.ui.theme.FlagGreen
import com.example.nomergrammuz.ui.theme.FlagRed
import com.example.nomergrammuz.ui.theme.FlagWhite

@Composable
fun UzbekistanFlagBadge(
    modifier: Modifier = Modifier,
    flagWidth: Dp = 26.dp,
    flagHeight: Dp = 16.dp,
    textColor: Color = Color(0xFF0F172A),
    showUzText: Boolean = true,
    fullHeight: Boolean = false
) {
    if (fullHeight) {
        // Full height flag for Type II Government plates (PAA, PPP, APC, etc.)
        Canvas(
            modifier = modifier
                .width(flagWidth)
                .fillMaxHeight()
        ) {
            drawFlagCanvas(size.width, size.height)
        }
    } else {
        // Standard badge with flag at top and UZ below
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(
                modifier = Modifier
                    .width(flagWidth)
                    .height(flagHeight)
            ) {
                drawFlagCanvas(size.width, size.height)
            }

            if (showUzText) {
                Text(
                    text = "UZ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF002B7F), // Authentic deep blue UZ
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlagCanvas(w: Float, h: Float) {
    val stripeHeight = h / 3f
    val redThickness = (h * 0.05f).coerceAtLeast(1f)

    // 1. Top blue stripe
    drawRect(
        color = FlagBlue,
        topLeft = Offset(0f, 0f),
        size = Size(w, stripeHeight)
    )

    // Crescent moon on the blue stripe
    val crescentRadius = (stripeHeight * 0.36f).coerceAtLeast(2f)
    val crescentCenter = Offset(w * 0.28f, stripeHeight * 0.5f)
    drawCircle(
        color = FlagWhite,
        radius = crescentRadius,
        center = crescentCenter
    )
    drawCircle(
        color = FlagBlue,
        radius = crescentRadius * 0.82f,
        center = Offset(crescentCenter.x + crescentRadius * 0.35f, crescentCenter.y)
    )

    // 12 Stars (3 rows: 3, 4, 5)
    val starRadius = (stripeHeight * 0.055f).coerceAtLeast(1f)
    val startX = crescentCenter.x + crescentRadius * 0.85f
    val startY = stripeHeight * 0.24f
    val stepX = starRadius * 3.2f
    val stepY = starRadius * 2.8f

    // Row 1 (3 stars)
    for (i in 0 until 3) {
        drawCircle(color = FlagWhite, radius = starRadius, center = Offset(startX + stepX * (i + 2), startY))
    }
    // Row 2 (4 stars)
    for (i in 0 until 4) {
        drawCircle(color = FlagWhite, radius = starRadius, center = Offset(startX + stepX * (i + 1), startY + stepY))
    }
    // Row 3 (5 stars)
    for (i in 0 until 5) {
        drawCircle(color = FlagWhite, radius = starRadius, center = Offset(startX + stepX * i, startY + stepY * 2))
    }

    // 2. Red upper separator
    drawRect(
        color = FlagRed,
        topLeft = Offset(0f, stripeHeight - (redThickness / 2f)),
        size = Size(w, redThickness)
    )

    // 3. Middle white stripe
    drawRect(
        color = FlagWhite,
        topLeft = Offset(0f, stripeHeight),
        size = Size(w, stripeHeight)
    )

    // 4. Red lower separator
    drawRect(
        color = FlagRed,
        topLeft = Offset(0f, (stripeHeight * 2f) - (redThickness / 2f)),
        size = Size(w, redThickness)
    )

    // 5. Bottom green stripe
    drawRect(
        color = FlagGreen,
        topLeft = Offset(0f, stripeHeight * 2f),
        size = Size(w, stripeHeight)
    )
}
