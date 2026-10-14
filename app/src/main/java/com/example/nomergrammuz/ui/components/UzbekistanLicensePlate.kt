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

/**
 * Photorealistic and 100% compliant rendering of Uzbekistan license plates
 * in accordance with Cabinet of Ministers Decree № 131 from 27.06.2007 (Annex 2).
 */
@Composable
fun UzbekistanLicensePlate(
    result: ParsedPlateResult,
    modifier: Modifier = Modifier,
    placeholderText: String = "01 A 001 AA"
) {
    val plateTheme = result.theme
    val formatType = result.formatType

    val targetBgColor = when (plateTheme) {
        PlateTheme.GREEN -> PlateGreenBg
        PlateTheme.BLUE -> PlateBlueBg
        PlateTheme.YELLOW -> PlateYellowBg
        PlateTheme.BLACK -> Color(0xFF101214)
        else -> PlateWhiteBg
    }

    val targetBorderColor = when (plateTheme) {
        PlateTheme.GREEN -> PlateGreenBorder
        PlateTheme.BLUE -> PlateBlueBorder
        PlateTheme.YELLOW -> PlateYellowBorder
        PlateTheme.BLACK -> Color(0xFFF1F5F9)
        else -> PlateWhiteBorder
    }

    val targetTextColor = when (plateTheme) {
        PlateTheme.GREEN, PlateTheme.BLUE, PlateTheme.BLACK -> PlateTextWhite
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
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner embossed border line typical of authentic plates
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    color = if (plateTheme in listOf(PlateTheme.GREEN, PlateTheme.BLUE, PlateTheme.BLACK)) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(5.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when {
                // -----------------------------------------------------------------
                // Case 1: Type II Government Special (Flag on the LEFT, full height, no UZ, no region)
                // -----------------------------------------------------------------
                formatType == PlateFormatType.GOV_SPECIAL || plateTheme == PlateTheme.WHITE_GOV_FLAG -> {
                    // Left: Full-height Flag of Uzbekistan
                    UzbekistanFlagBadge(
                        modifier = Modifier.padding(start = 2.dp),
                        flagWidth = 46.dp,
                        fullHeight = true
                    )

                    // Solid black vertical divider
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.5.dp)
                            .background(Color(0xFF0F172A))
                    )

                    // Middle: Series and Number (e.g. PAA 001, ZDF 050)
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

                    // Right spacer to keep number centered relative to full plate
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // -----------------------------------------------------------------
                // Case 2: Diplomatic (CMD, D, T, X) or UN Agency (Pure seamless background, no flag, no region)
                // -----------------------------------------------------------------
                formatType in listOf(
                    PlateFormatType.DIPLOMATIC_HEAD,
                    PlateFormatType.DIPLOMAT,
                    PlateFormatType.UN_AGENCY,
                    PlateFormatType.TECHNICAL_STAFF,
                    PlateFormatType.CONSULAR
                ) -> {
                    Text(
                        text = result.formattedPlate.ifEmpty { placeholderText },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        color = animatedTextColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // -----------------------------------------------------------------
                // Case 3: Type IX (M - Foreign Firm / Investor / Media)
                // Green background with left region box, NO flag
                // -----------------------------------------------------------------
                formatType == PlateFormatType.FOREIGN_FIRM -> {
                    val regionPart = result.secondaryRegionRule?.regionCode
                        ?: result.formattedPlate.takeWhile { it.isDigit() }.take(2).ifEmpty { "01" }
                    val bodyPart = result.formattedPlate.substringAfter("M").trim().ifEmpty { "000000" }

                    // Left: Region Box (White text on Green)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = regionPart,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    }

                    // White vertical divider
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(Color.White.copy(alpha = 0.8f))
                    )

                    // Right: M + Digits
                    Text(
                        text = "M $bodyPart",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(44.dp))
                }

                // -----------------------------------------------------------------
                // Case 4: Type X (H - Foreign Citizens & Stateless Persons)
                // Yellow background with left region box, NO flag
                // -----------------------------------------------------------------
                formatType == PlateFormatType.FOREIGN_CITIZEN -> {
                    val regionPart = result.secondaryRegionRule?.regionCode
                        ?: result.formattedPlate.takeWhile { it.isDigit() }.take(2).ifEmpty { "01" }
                    val bodyPart = result.formattedPlate.substringAfter("H").trim().ifEmpty { "000000" }

                    // Left: Region Box (Black text on Yellow)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = regionPart,
                            color = Color(0xFF0F172A),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Black vertical divider
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.5.dp)
                            .background(Color(0xFF0F172A))
                    )

                    // Right: H + Digits
                    Text(
                        text = "H $bodyPart",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF0F172A),
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(44.dp))
                }

                // -----------------------------------------------------------------
                // Case 5: Military Vehicles (Mudofaa vazirligi - Type Military)
                // Black background, white border, left region box, 4 digits + MV, no country flag
                // -----------------------------------------------------------------
                formatType == PlateFormatType.MILITARY || plateTheme == PlateTheme.BLACK -> {
                    val regionPart = result.secondaryRegionRule?.regionCode
                        ?: result.formattedPlate.takeWhile { it.isDigit() }.take(2).ifEmpty { "01" }
                    val bodyPart = if (result.formattedPlate.length > 2 && result.formattedPlate.startsWith(regionPart)) {
                        result.formattedPlate.substring(2).trim()
                    } else {
                        result.formattedPlate.ifEmpty { "0000 MV" }
                    }

                    // Left: Region Code Box (White text on black background)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(46.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = regionPart,
                            color = Color.White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Solid white vertical divider line
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.5.dp)
                            .background(Color.White.copy(alpha = 0.85f))
                    )

                    // Middle: 4 digits + MV
                    Text(
                        text = bodyPart,
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center
                    )

                    // Right spacer for visual symmetry
                    Spacer(modifier = Modifier.width(46.dp))
                }

                // -----------------------------------------------------------------
                // Case 6: Standard Regional Plates (Type I Legal, Type I-1 DAV, Type I-2 STY, Type III Individual, Taxi)
                // Left region box + divider, middle text, right flag badge + UZ text
                // -----------------------------------------------------------------
                else -> {
                    val regionPart = result.secondaryRegionRule?.regionCode
                        ?: result.formattedPlate.takeWhile { it.isDigit() }.take(2)
                        .ifEmpty { "01" }

                    val numberPart = if (result.formattedPlate.length > 2 && result.formattedPlate.startsWith(regionPart)) {
                        result.formattedPlate.substring(2).trim()
                    } else if (result.formattedPlate.isNotBlank()) {
                        result.formattedPlate
                    } else {
                        placeholderText.substring(2).trim()
                    }

                    val isEV = result.isElectricVehicle || plateTheme == PlateTheme.EV_GREEN_ZONE

                    // Left: Region Code Box
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(46.dp)
                            .background(if (isEV) PlateEvGreenBadge else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = regionPart,
                            color = if (isEV) Color.White else animatedTextColor,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Solid vertical divider line
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.5.dp)
                            .background(Color(0xFF0F172A))
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

                    // Right: Flag Badge with UZ text (no vertical line before it, as in Decree № 131)
                    UzbekistanFlagBadge(
                        modifier = Modifier.padding(end = 6.dp),
                        flagWidth = 26.dp,
                        flagHeight = 16.dp,
                        textColor = animatedTextColor,
                        showUzText = true,
                        fullHeight = false
                    )
                }
            }
        }
    }
}
