package com.example.nomergrammuz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    val (badgeText, badgeColor) = when (result.formatType) {
        PlateFormatType.DIPLOMATIC_HEAD -> (if (isRussian) "РУКОВОДИТЕЛЬ ДИПМИССИИ" else "DIPLOMATIK MISSIYA RAHBARI") to PlateEvGreenBadge
        PlateFormatType.DIPLOMAT, PlateFormatType.TECHNICAL_STAFF, PlateFormatType.CONSULAR -> (if (isRussian) "ДИПЛОМАТИЧЕСКИЙ КОРПУС" else "DIPLOMATIK KORPUS") to PlateEvGreenBadge
        PlateFormatType.UN_AGENCY -> (if (isRussian) "МИССИЯ ООН" else "BMT MISSIYASI") to PlateBlueBorder
        PlateFormatType.GOV_SPECIAL -> (if (isRussian) "ВЫСШИЙ ОРГАН ВЛАСТИ" else "OLIY DAVLAT ORGANI") to Color(0xFFEF4444)
        PlateFormatType.GOV_DUTY_REGIONAL -> (if (isRussian) "ХОКИМИЯТ / ГОСУПРАВЛЕНИЕ" else "HOKIMLIK / BOSHQARUV") to Color(0xFFF97316)
        PlateFormatType.PUBLIC_TAXI -> (if (isRussian) "ПАССАЖИРСКОЕ ТАКСИ" else "YO'NALISHLI TAKSI") to PlateYellowBg
        PlateFormatType.FOREIGN_CITIZEN -> (if (isRussian) "ИНОСТРАННЫЙ ГРАЖДАНИН" else "CHET EL FUQAROSI") to PlateYellowBg
        PlateFormatType.CIVILIAN_INDIVIDUAL -> (if (isRussian) "ФИЗИЧЕСКОЕ ЛИЦО" else "JISMONIY SHAXS") to Color(0xFF64748B)
        PlateFormatType.CIVILIAN_LEGAL -> (if (isRussian) "ЮРИДИЧЕСКОЕ ЛИЦО" else "YURIDIK SHAXS") to Color(0xFF3B82F6)
        PlateFormatType.EMERGENCY_SERVICE -> (if (isRussian) "ЭКСТРЕННАЯ СЛУЖБА" else "MAXSUS XIZMAT") to Color(0xFFDC2626)
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
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Region / Country pill
                val locationTag = result.countryName ?: result.regionTitle
                if (!locationTag.isNullOrBlank()) {
                    Text(
                        text = locationTag,
                        color = AutoTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
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
        }
    }
}
