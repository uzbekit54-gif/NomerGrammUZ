package com.example.nomergrammuz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    showUzText: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Authentic Flag drawing via Canvas
        Canvas(
            modifier = Modifier
                .width(flagWidth)
                .height(flagHeight)
        ) {
            val w = size.width
            val h = size.height

            val stripeHeight = h / 3f
            val redThickness = h * 0.05f

            // Top blue stripe
            drawRect(
                color = FlagBlue,
                topLeft = Offset(0f, 0f),
                size = Size(w, stripeHeight)
            )

            // Red upper separator
            drawRect(
                color = FlagRed,
                topLeft = Offset(0f, stripeHeight - (redThickness / 2f)),
                size = Size(w, redThickness)
            )

            // Middle white stripe
            drawRect(
                color = FlagWhite,
                topLeft = Offset(0f, stripeHeight),
                size = Size(w, stripeHeight)
            )

            // Red lower separator
            drawRect(
                color = FlagRed,
                topLeft = Offset(0f, (stripeHeight * 2f) - (redThickness / 2f)),
                size = Size(w, redThickness)
            )

            // Bottom green stripe
            drawRect(
                color = FlagGreen,
                topLeft = Offset(0f, stripeHeight * 2f),
                size = Size(w, stripeHeight)
            )
        }

        if (showUzText) {
            Text(
                text = "UZ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = textColor
            )
        }
    }
}
