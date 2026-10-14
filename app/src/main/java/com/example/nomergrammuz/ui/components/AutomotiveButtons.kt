package com.example.nomergrammuz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextPrimary

/**
 * Auto-shrinking single-line text composable that dynamically reduces font size
 * to fit available space without word wrapping or line breaks.
 */
@Composable
fun AutoShrinkText(
    text: String,
    modifier: Modifier = Modifier,
    initialFontSize: TextUnit = 13.sp,
    minFontSize: TextUnit = 9.sp,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = TextStyle.Default
) {
    var fontSizeValue by remember(text, initialFontSize) { mutableStateOf(initialFontSize) }
    var readyToDraw by remember(text, initialFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        style = style.copy(fontSize = fontSizeValue),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth && fontSizeValue > minFontSize) {
                val nextVal = (fontSizeValue.value - 0.75f).sp
                fontSizeValue = if (nextVal >= minFontSize) nextVal else minFontSize
            } else {
                readyToDraw = true
            }
        }
    )
}

/**
 * Unified action button with fixed height, consistent border/corner radius,
 * and auto-shrinking text to prevent any word wrapping across different screen widths.
 */
@Composable
fun AutoSizedActionButton(
    modifier: Modifier = Modifier,
    icon: String? = null,
    text: String,
    height: Dp = 48.dp,
    backgroundColor: Color = AutoSurface,
    borderColor: Color = AutoBorder,
    textColor: Color = AutoTextPrimary,
    initialFontSize: TextUnit = 13.sp,
    minFontSize: TextUnit = 9.5.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            AutoShrinkText(
                text = text,
                color = textColor,
                initialFontSize = initialFontSize,
                minFontSize = minFontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
