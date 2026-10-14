package com.example.nomergrammuz.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.domain.model.ParsedPlateResult
import com.example.nomergrammuz.domain.model.PlateFormatType
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.ui.theme.PlateBlueBg
import com.example.nomergrammuz.ui.theme.PlateBlueBorder
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge
import com.example.nomergrammuz.ui.theme.PlateGreenBg
import com.example.nomergrammuz.ui.theme.PlateGreenBorder
import com.example.nomergrammuz.ui.theme.PlateTextBlack
import com.example.nomergrammuz.ui.theme.PlateTextWhite
import com.example.nomergrammuz.ui.theme.PlateWhiteBg
import com.example.nomergrammuz.ui.theme.PlateWhiteBorder
import com.example.nomergrammuz.ui.theme.PlateYellowBg
import com.example.nomergrammuz.ui.theme.PlateYellowBorder

@Composable
fun UzbekistanLicensePlate(
    result: ParsedPlateResult,
    modifier: Modifier = Modifier,
    placeholderText: String = "01 A 001 AA"
) {
    val plateTheme = result.theme

    val targetBgColor = when (plateTheme) {
        PlateTheme.GREEN -> PlateGreenBg
        PlateTheme.BLUE -> PlateBlueBg
        PlateTheme.YELLOW -> PlateYellowBg
        else -> PlateWhiteBg
    }

    val targetBorderColor = when (plateTheme) {
        PlateTheme.GREEN -> PlateGreenBorder
        PlateTheme.BLUE -> PlateBlueBorder
        PlateTheme.YELLOW -> PlateYellowBorder
        else -> PlateWhiteBorder
    }

    val targetTextColor = when (plateTheme) {
        PlateTheme.GREEN, PlateTheme.BLUE -> PlateTextWhite
        else -> PlateTextBlack
    }

    val animatedBgColor by animateColorAsState(targetBgColor, animationSpec = tween(300), label = "bg")
    val animatedBorderColor by animateColorAsState(targetBorderColor, animationSpec = tween(300), label = "border")
    val animatedTextColor by animateColorAsState(targetTextColor, animationSpec = tween(300), label = "text")

    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .shadow(elevation = 8.dp, shape = shape, spotColor = Color.Black)
            .clip(shape)
            .background(animatedBgColor)
            .border(width = 3.dp, color = animatedBorderColor, shape = shape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Case 1: Type II Government Special (Flag on the LEFT, no region code)
            if (result.formatType == PlateFormatType.GOV_SPECIAL || plateTheme == PlateTheme.WHITE_GOV_FLAG) {
                UzbekistanFlagBadge(
                    modifier = Modifier.padding(start = 6.dp, end = 12.dp),
                    flagWidth = 32.dp,
                    flagHeight = 20.dp,
                    textColor = animatedTextColor,
                    showUzText = false
                )

                Text(
                    text = result.formattedPlate.ifEmpty { placeholderText },
                    modifier = Modifier.weight(1f),
                    color = animatedTextColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(32.dp))
            }
            // Case 2: Diplomatic (CMD, D, T, X) or UN
            else if (result.formatType in listOf(
                    PlateFormatType.DIPLOMATIC_HEAD,
                    PlateFormatType.DIPLOMAT,
                    PlateFormatType.UN_AGENCY,
                    PlateFormatType.TECHNICAL_STAFF,
                    PlateFormatType.CONSULAR
                )
            ) {
                Text(
                    text = result.formattedPlate.ifEmpty { placeholderText },
                    modifier = Modifier.fillMaxWidth(),
                    color = animatedTextColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )
            }
            // Case 3: Standard Regional Plate (Civilian, Legal, DAV, STY, Taxi)
            else {
                val regionPart = result.secondaryRegionRule?.regionCode
                    ?: result.formattedPlate.takeWhile { it.isDigit() }.take(2)
                    .ifEmpty { "01" }

                val numberPart = if (result.formattedPlate.length > 2) {
                    result.formattedPlate.substring(2).trim()
                } else {
                    placeholderText.substring(2).trim()
                }

                val isEV = result.isElectricVehicle || plateTheme == PlateTheme.EV_GREEN_ZONE

                // Left: Region Code Box
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(44.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isEV) PlateEvGreenBadge else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = regionPart,
                        color = if (isEV) Color.White else animatedTextColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                }

                // Vertical Divider Line
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .width(2.dp)
                        .background(animatedBorderColor.copy(alpha = 0.4f))
                )

                // Middle: Registration Numbers & Letters
                Text(
                    text = numberPart.ifEmpty { "A 001 AA" },
                    modifier = Modifier.weight(1f),
                    color = animatedTextColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                // Vertical Divider Line
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .width(2.dp)
                        .background(animatedBorderColor.copy(alpha = 0.4f))
                )

                // Right: Flag Badge with UZ text
                UzbekistanFlagBadge(
                    modifier = Modifier.padding(start = 6.dp, end = 4.dp),
                    flagWidth = 24.dp,
                    flagHeight = 15.dp,
                    textColor = animatedTextColor,
                    showUzText = true
                )
            }
        }
    }
}
