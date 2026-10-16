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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.domain.model.DiplomaticCountryMission
import com.example.nomergrammuz.domain.model.PlateRule
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextMuted
import com.example.nomergrammuz.ui.theme.AutoTextPrimary
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge

@Composable
fun DiplomaticCountryCard(
    mission: DiplomaticCountryMission,
    isRussian: Boolean = true,
    onSelectPlate: (plateText: String, rule: PlateRule) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isUN = mission.codeDisplay == "UN"
    val defaultPlate = mission.cmdPlate ?: mission.unPlate ?: mission.dPlate ?: "CMD 0101"
    val defaultRule = mission.cmdRule ?: mission.primaryRule

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AutoSurface)
            .border(1.dp, AutoBorder, RoundedCornerShape(12.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectPlate(defaultPlate, defaultRule)
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block: Flag + Info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large National Flag Emoji
                Text(
                    text = mission.flagEmoji,
                    fontSize = 26.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Numeric Code Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isUN) Color(0xFF1D4ED8).copy(alpha = 0.2f)
                                    else PlateEvGreenBadge.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (isUN) Color(0xFF60A5FA).copy(alpha = 0.4f)
                                    else PlateEvGreenBadge.copy(alpha = 0.3f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isUN) "UN" else "Код ${mission.codeDisplay}",
                                color = if (isUN) Color(0xFF60A5FA) else PlateEvGreenBadge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Country Title
                        Text(
                            text = if (isRussian) mission.countryNameRu else mission.countryNameUz,
                            color = AutoTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Embassy / Organization Name
                    Text(
                        text = if (isRussian) mission.orgNameRu else mission.orgNameUz,
                        color = AutoTextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right block: Instant Selection Chips [CMD] and [D] or [UN]
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mission.cmdPlate != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF064E3B))
                            .border(1.dp, PlateEvGreenBadge, RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectPlate(mission.cmdPlate, mission.cmdRule ?: mission.primaryRule)
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CMD",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (isRussian) "Посол" else "Elchi",
                                color = PlateEvGreenBadge,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (mission.dPlate != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F291E))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectPlate(mission.dPlate, mission.dRule ?: mission.primaryRule)
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "D",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (isRussian) "Дипломат" else "Diplomat",
                                color = Color(0xFFA7F3D0),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (mission.unPlate != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E3A8A))
                            .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectPlate(mission.unPlate, mission.primaryRule)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "UN",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "0001",
                                color = Color(0xFF93C5FD),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
