package com.example.nomergrammuz.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.domain.model.PlateCategory
import com.example.nomergrammuz.domain.model.PlateRule
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.ui.components.AutoShrinkText
import com.example.nomergrammuz.ui.theme.AutoBackground
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextMuted
import com.example.nomergrammuz.ui.theme.AutoTextPrimary
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge

enum class CatalogFilterTab {
    ALL,
    REGIONS,
    DIPLOMATIC,
    GOVERNMENT,
    MILITARY,
    FOREIGN,
    ELECTRIC
}

@Composable
fun CatalogScreen(
    rules: List<PlateRule>,
    initialTab: CatalogFilterTab = CatalogFilterTab.ALL,
    isRussian: Boolean = true,
    onRuleSelected: (PlateRule) -> Unit,
    onRefreshDatabase: (suspend () -> String)? = null,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val filteredRules = remember(rules, selectedTab, searchQuery, isRussian) {
        rules.filter { rule ->
            val matchesTab = when (selectedTab) {
                CatalogFilterTab.ALL -> true
                CatalogFilterTab.REGIONS -> rule.category == PlateCategory.REGIONAL
                CatalogFilterTab.DIPLOMATIC -> rule.category == PlateCategory.DIPLOMATIC && rule.series !in listOf("M", "H")
                CatalogFilterTab.GOVERNMENT -> (rule.category == PlateCategory.STATE_AUTHORITY || rule.series in listOf("DAV", "STY", "PAA", "PPP", "APC", "UZB", "ZDF")) && rule.category != PlateCategory.MILITARY
                CatalogFilterTab.MILITARY -> rule.category in listOf(
                    PlateCategory.MILITARY,
                    PlateCategory.LAW_ENFORCEMENT,
                    PlateCategory.EMERGENCY
                ) || rule.series in listOf("MV", "TSS", "TST", "USB", "PSF", "PSP", "PSV", "FVV")
                CatalogFilterTab.FOREIGN -> rule.series in listOf("M", "H") || rule.id.startsWith("foreign_")
                CatalogFilterTab.ELECTRIC -> rule.plateTheme == PlateTheme.EV_GREEN_ZONE || rule.subType == "ELECTRIC_VEHICLE"
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                (rule.countryNameRu?.lowercase()?.contains(q) == true) ||
                        (rule.countryNameUz?.lowercase()?.contains(q) == true) ||
                        (rule.orgNameRu.lowercase().contains(q)) ||
                        (rule.orgNameUz.lowercase().contains(q)) ||
                        (rule.descriptionRu.lowercase().contains(q)) ||
                        (rule.descriptionUz.lowercase().contains(q)) ||
                        (rule.series?.lowercase()?.contains(q) == true) ||
                        (rule.regionCode?.contains(q) == true) ||
                        (rule.countryCode?.toString() == q)
            }

            matchesTab && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AutoBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AutoSurface)
                        .border(1.dp, AutoBorder, RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.padding(start = 12.dp))
                Text(
                    text = if (isRussian) "Справочник" else "Katalog",
                    color = AutoTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Manual Refresh Button (Decision 1B)
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AutoSurface)
                    .border(1.dp, AutoBorder, RoundedCornerShape(10.dp))
                    .clickable(enabled = !isRefreshing) {
                        if (onRefreshDatabase != null) {
                            coroutineScope.launch {
                                isRefreshing = true
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                val msg = onRefreshDatabase()
                                statusMessage = msg
                                isRefreshing = false
                            }
                        }
                    }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isRefreshing) "⏳" else "🔄", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    AutoShrinkText(
                        text = if (isRefreshing) {
                            if (isRussian) "Проверка..." else "Tekshirilmoqda..."
                        } else {
                            if (isRussian) "Обновить" else "Yangilash"
                        },
                        color = PlateEvGreenBadge,
                        initialFontSize = 12.sp,
                        minFontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage!!,
                color = PlateEvGreenBadge,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar in Catalog
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = if (isRussian) "Поиск страны, ведомства, кода..." else "Mamlakat, idora yoki kod qidirish...",
                    color = AutoTextMuted
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlateEvGreenBadge,
                unfocusedBorderColor = AutoBorder,
                focusedContainerColor = AutoSurface,
                unfocusedContainerColor = AutoSurface,
                focusedTextColor = AutoTextPrimary,
                unfocusedTextColor = AutoTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips (Horizontal)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                CatalogFilterTab.ALL to (if (isRussian) "Все" else "Barchasi"),
                CatalogFilterTab.REGIONS to (if (isRussian) "📍 Регионы" else "📍 Hududlar"),
                CatalogFilterTab.DIPLOMATIC to (if (isRussian) "🌍 Дипломаты" else "🌍 Diplomatiya"),
                CatalogFilterTab.GOVERNMENT to (if (isRussian) "🏛️ Госорганы" else "🏛️ Davlat organlari"),
                CatalogFilterTab.MILITARY to (if (isRussian) "🛡️ Военные & Спецслужбы" else "🛡️ Harbiy & Maxsus"),
                CatalogFilterTab.FOREIGN to (if (isRussian) "🏢 Инофирмы & Иностранцы (M, H)" else "🏢 Xorijiy kompaniyalar (M, H)"),
                CatalogFilterTab.ELECTRIC to (if (isRussian) "⚡ Электромобили" else "⚡ Elektromobillar")
            )

            tabs.forEach { (tab, label) ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                        .border(
                            1.dp,
                            if (isSelected) PlateEvGreenBadge else AutoBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) PlateEvGreenBadge else AutoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rules List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredRules, key = { it.id }) { rule ->
                CatalogRuleItem(
                    rule = rule,
                    isRussian = isRussian,
                    onClick = { onRuleSelected(rule) }
                )
            }
        }
    }
}

@Composable
fun CatalogRuleItem(
    rule: PlateRule,
    isRussian: Boolean,
    onClick: () -> Unit
) {
    val codeDisplay = when {
        rule.series != null -> rule.series
        rule.countryCode != null -> "Код ${rule.countryCode}"
        rule.regionCode != null -> "Код ${rule.regionCode}"
        else -> "РУз"
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge code
                    val isBlack = rule.plateTheme == PlateTheme.BLACK
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBlack) Color(0xFF1E293B) else PlateEvGreenBadge.copy(alpha = 0.15f))
                            .border(if (isBlack) 1.dp else 0.dp, if (isBlack) Color(0xFF94A3B8) else Color.Transparent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = codeDisplay,
                            color = if (isBlack) Color.White else PlateEvGreenBadge,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val country = if (isRussian) rule.countryNameRu else rule.countryNameUz
                    if (country != null) {
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Text(
                            text = country,
                            color = AutoTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isRussian) rule.orgNameRu else rule.orgNameUz,
                    color = AutoTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (isRussian) rule.descriptionRu else rule.descriptionUz,
                    color = AutoTextMuted,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "›",
                color = AutoTextMuted,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
