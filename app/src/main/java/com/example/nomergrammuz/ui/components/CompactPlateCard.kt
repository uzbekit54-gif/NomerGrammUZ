package com.example.nomergrammuz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.domain.model.ParsedPlateResult
import com.example.nomergrammuz.domain.model.PlateFormatType
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextMuted
import com.example.nomergrammuz.ui.theme.AutoTextPrimary
import com.example.nomergrammuz.ui.theme.PlateBlueBorder
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge
import com.example.nomergrammuz.ui.theme.PlateYellowBg

@Composable
fun CompactPlateCard(
    result: ParsedPlateResult,
    isRussian: Boolean = true,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    val (badgeText, badgeColor) = when {
        result.theme == PlateTheme.EV_GREEN_ZONE || result.isElectricVehicle -> (if (isRussian) "ЭЛЕКТРОМОБИЛЬ (EV)" else "ELEKTROMOBIL (EV)") to PlateEvGreenBadge
        result.formatType == PlateFormatType.MILITARY || result.theme == PlateTheme.BLACK -> (if (isRussian) "ВОЕННЫЙ АВТОТРАНСПОРТ" else "HARBIY AVTOTRANSPORT") to Color(0xFF22C55E)
        result.formatType == PlateFormatType.DIPLOMATIC_HEAD -> (if (isRussian) "РУКОВОДИТЕЛЬ ДИПМИССИИ" else "DIPLOMATIK MISSIYA RAHBARI") to PlateEvGreenBadge
        result.formatType in listOf(PlateFormatType.DIPLOMAT, PlateFormatType.TECHNICAL_STAFF, PlateFormatType.CONSULAR) -> (if (isRussian) "ДИПЛОМАТИЧЕСКИЙ КОРПУС" else "DIPLOMATIK KORPUS") to PlateEvGreenBadge
        result.formatType == PlateFormatType.UN_AGENCY -> (if (isRussian) "МИССИЯ ООН" else "BMT MISSIYASI") to PlateBlueBorder
        result.formatType == PlateFormatType.FOREIGN_FIRM -> (if (isRussian) "ИНОФИРМА / БАНК (M)" else "XORIJIY KOMPANIYA (M)") to PlateEvGreenBadge
        result.formatType == PlateFormatType.GOV_SPECIAL -> (if (isRussian) "ВЫСШИЙ ОРГАН ВЛАСТИ" else "OLIY DAVLAT ORGANI") to Color(0xFFEF4444)
        result.formatType == PlateFormatType.GOV_DUTY_REGIONAL -> (if (isRussian) "ХОКИМИЯТ / ГОСУПРАВЛЕНИЕ" else "HOKIMLIK / BOSHQARUV") to Color(0xFFF97316)
        result.formatType == PlateFormatType.PUBLIC_TAXI -> (if (isRussian) "ПАССАЖИРСКОЕ ТАКСИ" else "YO'NALISHLI TAKSI") to PlateYellowBg
        result.formatType == PlateFormatType.FOREIGN_CITIZEN -> (if (isRussian) "ИНОСТРАННЫЙ ГРАЖДАНИН (H)" else "CHET EL FUQAROSI (H)") to PlateYellowBg
        result.formatType == PlateFormatType.CIVILIAN_INDIVIDUAL -> (if (isRussian) "ФИЗИЧЕСКОЕ ЛИЦО" else "JISMONIY SHAXS") to Color(0xFF64748B)
        result.formatType == PlateFormatType.CIVILIAN_LEGAL -> (if (isRussian) "ЮРИДИЧЕСКОЕ ЛИЦО" else "YURIDIK SHAXS") to Color(0xFF3B82F6)
        result.formatType == PlateFormatType.EMERGENCY_SERVICE -> (if (isRussian) "ЭКСТРЕННАЯ СЛУЖБА" else "MAXSUS XIZMAT") to Color(0xFFDC2626)
        else -> (if (isRussian) "СТАТУС" else "HOLAT") to Color(0xFF475569)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AutoSurface)
            .border(width = 1.dp, color = AutoBorder, shape = shape)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pill
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        maxLines = 1
                    )
                }

                // Region / Country pill
                val flagEmoji = if (result.countryName != null) {
                    com.example.nomergrammuz.domain.model.DiplomaticFlags.getFlag(result.primaryRule?.countryCode)
                } else null
                val locationTag = when {
                    result.countryName != null -> "$flagEmoji  ${result.countryName}"
                    result.regionTitle != null -> result.regionTitle
                    else -> null
                }
                if (!locationTag.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = locationTag,
                        color = AutoTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Organization Title
            Text(
                text = result.organizationName,
                color = AutoTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Concise Status Description (Decision 2B: No cluttered long articles, clean status)
            Text(
                text = result.description,
                color = AutoTextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            // Action Buttons Row (Favorite, Share, Copy) - Decision 1A
            if (result.isIdentified && result.formattedPlate.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Favorite Toggle Button
                    if (onToggleFavorite != null) {
                        val favActiveBg = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        val favActiveColor = Color(0xFFFBBF24)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFavorite) favActiveBg else Color.White.copy(alpha = 0.05f))
                                .border(
                                    1.dp,
                                    if (isFavorite) favActiveColor else AutoBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onToggleFavorite() }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isFavorite) "★" else "☆",
                                    color = if (isFavorite) favActiveColor else AutoTextMuted,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoShrinkText(
                                    text = if (isFavorite) {
                                        if (isRussian) "В избранном" else "Saqlangan"
                                    } else {
                                        if (isRussian) "В избранное" else "Saqlash"
                                    },
                                    color = if (isFavorite) favActiveColor else AutoTextPrimary,
                                    initialFontSize = 11.sp,
                                    minFontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 2. Share Button (Decision 1A)
                    if (onShare != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, AutoBorder, RoundedCornerShape(10.dp))
                                .clickable { onShare() }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "📤", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoShrinkText(
                                    text = if (isRussian) "Поделиться" else "Ulashish",
                                    color = AutoTextPrimary,
                                    initialFontSize = 11.sp,
                                    minFontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 3. Copy Plate Button
                    if (onCopy != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, AutoBorder, RoundedCornerShape(10.dp))
                                .clickable { onCopy() }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "📋", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                AutoShrinkText(
                                    text = if (isRussian) "Копия" else "Nusxa",
                                    color = AutoTextPrimary,
                                    initialFontSize = 11.sp,
                                    minFontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
