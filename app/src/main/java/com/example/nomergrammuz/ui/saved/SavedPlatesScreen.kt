package com.example.nomergrammuz.ui.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.domain.model.SavedPlateItem
import com.example.nomergrammuz.ui.components.AutoShrinkText
import com.example.nomergrammuz.ui.theme.AutoBackground
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextMuted
import com.example.nomergrammuz.ui.theme.AutoTextPrimary
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge

enum class SavedPlatesTab {
    FAVORITES,
    HISTORY
}

@Composable
fun SavedPlatesScreen(
    favorites: List<SavedPlateItem>,
    history: List<SavedPlateItem>,
    initialTab: SavedPlatesTab = SavedPlatesTab.FAVORITES,
    isRussian: Boolean = true,
    onPlateSelected: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutoBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AutoSurface)
                    .clickable { onBack() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "←",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isRussian) "Избранное и история" else "Saqlanganlar va tarix",
                    color = AutoTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRussian) "Быстрый доступ к номерам" else "Raqamlarga tezkor kirish",
                    color = AutoTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs Selector (Favorites vs History)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isFav = selectedTab == SavedPlatesTab.FAVORITES
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isFav) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                    .border(1.dp, if (isFav) PlateEvGreenBadge else AutoBorder, RoundedCornerShape(12.dp))
                    .clickable { selectedTab = SavedPlatesTab.FAVORITES }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "⭐", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    AutoShrinkText(
                        text = if (isRussian) "Избранное (${favorites.size})" else "Saqlanganlar (${favorites.size})",
                        color = if (isFav) PlateEvGreenBadge else AutoTextPrimary,
                        initialFontSize = 13.sp,
                        minFontSize = 9.5.sp,
                        fontWeight = if (isFav) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            val isHist = selectedTab == SavedPlatesTab.HISTORY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isHist) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                    .border(1.dp, if (isHist) PlateEvGreenBadge else AutoBorder, RoundedCornerShape(12.dp))
                    .clickable { selectedTab = SavedPlatesTab.HISTORY }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🕒", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    AutoShrinkText(
                        text = if (isRussian) "История (${history.size})" else "Tarix (${history.size})",
                        color = if (isHist) PlateEvGreenBadge else AutoTextPrimary,
                        initialFontSize = 13.sp,
                        minFontSize = 9.5.sp,
                        fontWeight = if (isHist) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Content
        when (selectedTab) {
            SavedPlatesTab.FAVORITES -> {
                if (favorites.isEmpty()) {
                    EmptyStateCard(
                        icon = "⭐",
                        title = if (isRussian) "В избранном пока пусто" else "Saqlangan raqamlar yo'q",
                        subtitle = if (isRussian) "Нажмите «В избранное» в карточке любого распознанного номера, чтобы сохранить его сюда" else "Har qanday raqam kartasida «Saqlash» tugmasini bosing"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(favorites, key = { it.plate + it.timestamp }) { item ->
                            SavedPlateCard(
                                item = item,
                                isRussian = isRussian,
                                onClick = { onPlateSelected(item.plate) },
                                onDelete = { onRemoveFavorite(item.plate) }
                            )
                        }
                    }
                }
            }

            SavedPlatesTab.HISTORY -> {
                if (history.isEmpty()) {
                    EmptyStateCard(
                        icon = "🕒",
                        title = if (isRussian) "История поиска пуста" else "Qidiruv tarixi bo'sh",
                        subtitle = if (isRussian) "Здесь будут автоматически сохраняться ваши недавние поисковые запросы" else "Bu yerda siz qidirgan so'nggi raqamlar saqlanadi"
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isRussian) "Последние запросы" else "So'nggi so'rovlar",
                                color = AutoTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (isRussian) "Очистить всё" else "Tozalash",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onClearHistory() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(history, key = { it.plate + it.timestamp }) { item ->
                                SavedPlateCard(
                                    item = item,
                                    isRussian = isRussian,
                                    onClick = { onPlateSelected(item.plate) },
                                    onDelete = { onRemoveHistory(item.plate) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPlateCard(
    item: SavedPlateItem,
    isRussian: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val plateColor = when (item.theme) {
        PlateTheme.GREEN -> Color(0xFF064E3B)
        PlateTheme.BLUE -> Color(0xFF1E3A8A)
        PlateTheme.YELLOW -> Color(0xFFCA8A04)
        PlateTheme.BLACK -> Color(0xFF111418)
        else -> Color.White
    }

    val plateTextColor = when (item.theme) {
        PlateTheme.WHITE, PlateTheme.EV_GREEN_ZONE, PlateTheme.WHITE_GOV_FLAG -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AutoSurface)
            .border(1.dp, AutoBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Mini Plate Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(plateColor)
                        .border(
                            1.dp,
                            if (plateColor == Color.White) Color(0xFF94A3B8) else plateColor,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.plate,
                        color = plateTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        color = AutoTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        color = AutoTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = AutoTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AutoSurface)
                    .border(1.dp, AutoBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = AutoTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = AutoTextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
