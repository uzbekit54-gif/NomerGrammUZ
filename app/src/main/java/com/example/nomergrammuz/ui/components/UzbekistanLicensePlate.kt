package com.example.nomergrammuz.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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

val UZ_REGION_CODES = setOf(
    "01", "10", "20", "25", "30", "40", "50", "60", "70", "75", "80", "85", "90", "95"
)

data class RegionalPlateParts(
    val region: String,
    val number: String,
    val isNumberPlaceholder: Boolean,
    val isRegionPlaceholder: Boolean
)

data class MilitaryPlateParts(
    val region: String,
    val body: String
)

fun resolveRegionalPlateParts(
    result: ParsedPlateResult,
    defaultPlaceholder: String = "A 001 AA"
): RegionalPlateParts {
    val rawClean = result.rawInput.trim()
    val formatted = result.formattedPlate.trim()

    val knownReg = result.secondaryRegionRule?.regionCode
        ?: result.primaryRule?.regionCode

    return when {
        // 1. If input is completely blank, show clean default placeholder
        rawClean.isEmpty() && formatted.isEmpty() -> {
            RegionalPlateParts("01", defaultPlaceholder, isNumberPlaceholder = true, isRegionPlaceholder = true)
        }

        // 2. If user entered only 1 digit (e.g. "0", "1", "7", "3"):
        // Show that digit in the region box, leave number part as placeholder
        rawClean.length == 1 && rawClean[0].isDigit() -> {
            RegionalPlateParts(rawClean, defaultPlaceholder, isNumberPlaceholder = true, isRegionPlaceholder = false)
        }

        // 3. If known region was identified (e.g. by region rule):
        knownReg != null -> {
            val rest = if (formatted.startsWith(knownReg)) {
                formatted.substring(knownReg.length).trim()
            } else {
                rawClean.removePrefix(knownReg).trim()
            }
            if (rawClean == knownReg || rest.isEmpty()) {
                RegionalPlateParts(knownReg, defaultPlaceholder, isNumberPlaceholder = true, isRegionPlaceholder = false)
            } else {
                RegionalPlateParts(knownReg, rest, isNumberPlaceholder = false, isRegionPlaceholder = false)
            }
        }

        // 4. If formatted starts with a valid 2-digit Uzbekistan region code:
        UZ_REGION_CODES.contains(formatted.take(2)) -> {
            val reg = formatted.take(2)
            val rest = formatted.substring(2).trim()
            if (rest.isEmpty()) {
                RegionalPlateParts(reg, defaultPlaceholder, isNumberPlaceholder = true, isRegionPlaceholder = false)
            } else {
                RegionalPlateParts(reg, rest, isNumberPlaceholder = false, isRegionPlaceholder = false)
            }
        }

        // 5. If raw input starts with a valid 2-digit region code:
        UZ_REGION_CODES.contains(rawClean.take(2)) -> {
            val reg = rawClean.take(2)
            val rest = rawClean.substring(2).trim()
            if (rest.isEmpty()) {
                RegionalPlateParts(reg, defaultPlaceholder, isNumberPlaceholder = true, isRegionPlaceholder = false)
            } else {
                RegionalPlateParts(reg, rest, isNumberPlaceholder = false, isRegionPlaceholder = false)
            }
        }

        // 6. Otherwise: user entered a number or text WITHOUT a region prefix
        // (e.g. "777", "777 AAA", "A 777 AA", "1234", "11", "PA"):
        // Region box is default "01" (placeholder style), and user's entire input goes into numberPart!
        else -> {
            val text = formatted.ifEmpty { rawClean }
            RegionalPlateParts("01", text, isNumberPlaceholder = false, isRegionPlaceholder = true)
        }
    }
}

fun resolveMilitaryPlateParts(result: ParsedPlateResult): MilitaryPlateParts {
    val rawClean = result.rawInput.trim()
    val formatted = result.formattedPlate.trim()
    val knownReg = result.secondaryRegionRule?.regionCode

    val (regionPart, bodyPart) = when {
        knownReg != null -> {
            val body = formatted.removePrefix(knownReg).trim().ifEmpty { "0000 MV" }
            knownReg to body
        }
        UZ_REGION_CODES.contains(formatted.take(2)) -> {
            val body = formatted.substring(2).trim().ifEmpty { "0000 MV" }
            formatted.take(2) to body
        }
        UZ_REGION_CODES.contains(rawClean.take(2)) -> {
            val body = rawClean.substring(2).trim().ifEmpty { "0000 MV" }
            rawClean.take(2) to body
        }
        formatted.contains("MV") -> {
            "01" to formatted
        }
        else -> {
            "01" to formatted.ifEmpty { "0000 MV" }
        }
    }
    return MilitaryPlateParts(regionPart, bodyPart)
}

/**
 * Photorealistic and 100% compliant rendering of Uzbekistan license plates
 * in accordance with Cabinet of Ministers Decree № 131 from 27.06.2007 (Annex 2).
 */
@Composable
fun UzbekistanLicensePlate(
    result: ParsedPlateResult,
    modifier: Modifier = Modifier,
    placeholderText: String = "01 A 001 AA",
    isEditable: Boolean = false,
    inputText: String = "",
    onInputTextChange: (String) -> Unit = {},
    focusRequester: FocusRequester? = null,
    isFocused: Boolean = false,
    onFocusChange: ((Boolean) -> Unit)? = null,
    onClearText: (() -> Unit)? = null
) {
    val plateTheme = result.theme
    val formatType = result.formatType

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

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
    val activeBorderColor = if (isFocused) PlateEvGreenBadge else animatedBorderColor
    val activeBorderWidth = if (isFocused) 3.5.dp else 3.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .shadow(
                elevation = if (isFocused) 12.dp else 8.dp,
                shape = shape,
                spotColor = if (isFocused) PlateEvGreenBadge.copy(alpha = 0.5f) else Color.Black
            )
            .clip(shape)
            .background(animatedBgColor)
            .border(width = activeBorderWidth, color = activeBorderColor, shape = shape)
            .then(
                if (isEditable && focusRequester != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                } else Modifier
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner embossed border line typical of authentic plates
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    color = if (plateTheme in listOf(PlateTheme.GREEN, PlateTheme.BLUE, PlateTheme.BLACK))
                        Color.White.copy(alpha = 0.5f)
                    else Color.Black.copy(alpha = 0.2f),
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.formattedPlate.ifEmpty { placeholderText },
                            color = animatedTextColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center
                        )
                        if (isEditable && isFocused) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(28.dp)
                                    .background(animatedTextColor.copy(alpha = cursorAlpha))
                            )
                        }
                    }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.formattedPlate.ifEmpty { placeholderText },
                            color = animatedTextColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                        if (isEditable && isFocused) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(28.dp)
                                    .background(animatedTextColor.copy(alpha = cursorAlpha))
                            )
                        }
                    }
                }

                // -----------------------------------------------------------------
                // Case 3: Type IX (M - Foreign Firm / Investor / Media)
                // Green background with left region box, NO flag
                // -----------------------------------------------------------------
                formatType == PlateFormatType.FOREIGN_FIRM -> {
                    val rawClean = result.rawInput.trim()
                    val formatted = result.formattedPlate.trim()
                    val knownReg = result.secondaryRegionRule?.regionCode
                    val regionPart = knownReg
                        ?: if (UZ_REGION_CODES.contains(formatted.take(2))) formatted.take(2)
                        else if (UZ_REGION_CODES.contains(rawClean.take(2))) rawClean.take(2)
                        else "01"
                    val bodyPart = if (formatted.contains("M")) {
                        formatted.substringAfter("M").trim().ifEmpty { "000000" }
                    } else if (rawClean.contains("M")) {
                        rawClean.substringAfter("M").trim().ifEmpty { "000000" }
                    } else {
                        formatted.removePrefix(regionPart).trim().ifEmpty { "000000" }
                    }

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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "M $bodyPart",
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center
                        )
                        if (isEditable && isFocused) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(27.dp)
                                    .background(Color.White.copy(alpha = cursorAlpha))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(44.dp))
                }

                // -----------------------------------------------------------------
                // Case 4: Type X (H - Foreign Citizens & Stateless Persons)
                // Yellow background with left region box, NO flag
                // -----------------------------------------------------------------
                formatType == PlateFormatType.FOREIGN_CITIZEN -> {
                    val rawClean = result.rawInput.trim()
                    val formatted = result.formattedPlate.trim()
                    val knownReg = result.secondaryRegionRule?.regionCode
                    val regionPart = knownReg
                        ?: if (UZ_REGION_CODES.contains(formatted.take(2))) formatted.take(2)
                        else if (UZ_REGION_CODES.contains(rawClean.take(2))) rawClean.take(2)
                        else "01"
                    val bodyPart = if (formatted.contains("H")) {
                        formatted.substringAfter("H").trim().ifEmpty { "000000" }
                    } else if (rawClean.contains("H")) {
                        rawClean.substringAfter("H").trim().ifEmpty { "000000" }
                    } else {
                        formatted.removePrefix(regionPart).trim().ifEmpty { "000000" }
                    }

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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "H $bodyPart",
                            color = Color(0xFF0F172A),
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center
                        )
                        if (isEditable && isFocused) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(27.dp)
                                    .background(Color(0xFF0F172A).copy(alpha = cursorAlpha))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(44.dp))
                }

                // -----------------------------------------------------------------
                // Case 5: Military Vehicles (Mudofaa vazirligi - Type Military)
                // Black background, white border, left region box, 4 digits + MV, no country flag
                // -----------------------------------------------------------------
                formatType == PlateFormatType.MILITARY || plateTheme == PlateTheme.BLACK -> {
                    val (regionPart, bodyPart) = resolveMilitaryPlateParts(result)

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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bodyPart,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                        if (isEditable && isFocused) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(28.dp)
                                    .background(Color.White.copy(alpha = cursorAlpha))
                            )
                        }
                    }

                    // Right spacer for visual symmetry
                    Spacer(modifier = Modifier.width(46.dp))
                }

                // -----------------------------------------------------------------
                // Case 6: Standard Regional Plates (Type I Legal, Type I-1 DAV, Type I-2 STY, Type III Individual, Taxi)
                // Left region box + divider, middle text, right flag badge + UZ text
                // -----------------------------------------------------------------
                else -> {
                    val defaultPlaceholder = placeholderText.substring(2).trim()
                    val parts = resolveRegionalPlateParts(result, defaultPlaceholder)

                    val isEV = result.isElectricVehicle || plateTheme == PlateTheme.EV_GREEN_ZONE

                    // Region text color: full opacity if typed/selected, muted 35% if placeholder
                    val regionColor = when {
                        isEV -> if (parts.isRegionPlaceholder) Color.White.copy(alpha = 0.5f) else Color.White
                        parts.isRegionPlaceholder -> animatedTextColor.copy(alpha = 0.35f)
                        else -> animatedTextColor
                    }

                    // Number text color: full opacity if typed, muted 35% if placeholder
                    val numberColor = if (parts.isNumberPlaceholder) {
                        animatedTextColor.copy(alpha = 0.35f)
                    } else {
                        animatedTextColor
                    }

                    // Left: Region Code Box
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(46.dp)
                            .background(if (isEV) PlateEvGreenBadge else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = parts.region,
                            color = regionColor,
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
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = parts.number,
                            color = numberColor,
                            fontSize = if (parts.number.length > 9) 22.sp else 26.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                        if (isEditable && isFocused) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(26.dp)
                                    .background(animatedTextColor.copy(alpha = cursorAlpha))
                            )
                        }
                    }

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

        // Invisible BasicTextField layered over plate to capture software keyboard inputs
        if (isEditable && focusRequester != null) {
            BasicTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .matchParentSize()
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        onFocusChange?.invoke(state.isFocused)
                    },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true,
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = TextStyle(
                    color = Color.Transparent,
                    fontSize = 1.sp
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.size(0.dp)) {
                        innerTextField()
                    }
                }
            )
        }
    }
}
