package com.example.nomergrammuz.ui.search

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
import com.example.nomergrammuz.domain.model.DiplomaticFlags
import com.example.nomergrammuz.domain.model.DiplomaticCountryMission
import com.example.nomergrammuz.domain.model.buildDiplomaticCountryMissions
import com.example.nomergrammuz.ui.components.DiplomaticCountryCard
import com.example.nomergrammuz.ui.theme.AutoBackground
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextMuted
import com.example.nomergrammuz.ui.theme.AutoTextPrimary
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge

// ============================================================================
// 1. ПОИСК ПО РЕГИОНАМ (01 - 95)
// ============================================================================

@Composable
fun RegionSearchScreen(
    rules: List<PlateRule>,
    isRussian: Boolean = true,
    onRegionSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val regionRules = remember(rules, searchQuery, isRussian) {
        val allRegions = rules.filter { it.category == PlateCategory.REGIONAL && it.regionCode != null }
            .sortedBy { it.regionCode }

        if (searchQuery.isBlank()) {
            allRegions
        } else {
            val q = searchQuery.trim().lowercase()
            allRegions.filter { rule ->
                (rule.regionCode?.contains(q) == true) ||
                        (rule.orgNameRu.lowercase().contains(q)) ||
                        (rule.orgNameUz.lowercase().contains(q)) ||
                        (rule.descriptionRu.lowercase().contains(q)) ||
                        (rule.descriptionUz.lowercase().contains(q))
            }
        }
    }

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
                    text = if (isRussian) "Поиск по регионам" else "Hududlar bo'yicha qidiruv",
                    color = AutoTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRussian) "Коды областей 01–95 по ПКМ №131" else "PKM №131 bo'yicha 01–95 hudud kodlari",
                    color = AutoTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = if (isRussian) "Введите код или область (01, Самарканд...)" else "Kod yoki viloyatni kiriting (01, Samarqand...)",
                    color = AutoTextMuted,
                    fontSize = 13.sp
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

        Spacer(modifier = Modifier.height(14.dp))

        // Region items list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(regionRules, key = { it.id }) { rule ->
                val code = rule.regionCode ?: "00"
                val name = if (isRussian) rule.orgNameRu else rule.orgNameUz
                val desc = if (isRussian) rule.descriptionRu else rule.descriptionUz

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AutoSurface)
                        .border(1.dp, AutoBorder, RoundedCornerShape(12.dp))
                        .clickable { onRegionSelected("$code A 777 AA") }
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
                            // Large Region Code Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PlateEvGreenBadge.copy(alpha = 0.18f))
                                    .border(1.dp, PlateEvGreenBadge.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = code,
                                    color = PlateEvGreenBadge,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = name,
                                    color = AutoTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = AutoTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Sample plate preview button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$code 001 AAA",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. ПОИСК ДИПЛОМАТОВ (CMD, D, UN, T, X)
// ============================================================================

enum class DiplomatFilterSubTab {
    ALL,
    HEADS_CMD,
    DIPLOMATS_D,
    UN_MISSION,
    TECH_STAFF_T,
    CONSULAR_X
}

@Composable
fun DiplomatSearchScreen(
    rules: List<PlateRule>,
    isRussian: Boolean = true,
    onDiplomatSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var subTab by remember { mutableStateOf(DiplomatFilterSubTab.ALL) }

    val diplomaticMissions = remember(rules) {
        buildDiplomaticCountryMissions(rules)
    }

    val filteredDiplomaticMissions = remember(diplomaticMissions, searchQuery, subTab, isRussian) {
        val base = when (subTab) {
            DiplomatFilterSubTab.ALL -> diplomaticMissions
            DiplomatFilterSubTab.HEADS_CMD -> diplomaticMissions.filter { it.cmdPlate != null }
            DiplomatFilterSubTab.DIPLOMATS_D -> diplomaticMissions.filter { it.dPlate != null }
            DiplomatFilterSubTab.UN_MISSION -> diplomaticMissions.filter { it.codeDisplay == "UN" }
            DiplomatFilterSubTab.TECH_STAFF_T, DiplomatFilterSubTab.CONSULAR_X -> emptyList()
        }

        if (searchQuery.isBlank()) {
            base
        } else {
            val q = searchQuery.trim().lowercase()
            base.filter { mission ->
                mission.countryNameRu.lowercase().contains(q) ||
                        mission.countryNameUz.lowercase().contains(q) ||
                        mission.orgNameRu.lowercase().contains(q) ||
                        mission.orgNameUz.lowercase().contains(q) ||
                        mission.codeDisplay.lowercase().contains(q) ||
                        (mission.countryCode?.toString() == q) ||
                        (mission.countryCode != null && String.format("%02d", mission.countryCode).contains(q)) ||
                        q in listOf("cmd", "d", "un")
            }
        }
    }

    val filteredOtherDiplomats = remember(rules, searchQuery, subTab, isRussian) {
        if (subTab != DiplomatFilterSubTab.TECH_STAFF_T && subTab != DiplomatFilterSubTab.CONSULAR_X) {
            emptyList()
        } else {
            val dipRules = rules.filter { it.category == PlateCategory.DIPLOMATIC && it.series !in listOf("M", "H") }
            dipRules.filter { rule ->
                val matchesSubTab = when (subTab) {
                    DiplomatFilterSubTab.TECH_STAFF_T -> rule.series == "T"
                    DiplomatFilterSubTab.CONSULAR_X -> rule.series == "X"
                    else -> false
                }

                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    val q = searchQuery.trim().lowercase()
                    (rule.countryNameRu?.lowercase()?.contains(q) == true) ||
                            (rule.countryNameUz?.lowercase()?.contains(q) == true) ||
                            (rule.orgNameRu.lowercase().contains(q)) ||
                            (rule.orgNameUz.lowercase().contains(q)) ||
                            (rule.series?.lowercase()?.contains(q) == true) ||
                            (rule.countryCode?.toString() == q) ||
                            (rule.countryCode != null && String.format("%02d", rule.countryCode).contains(q))
                }

                matchesSubTab && matchesSearch
            }
        }
    }

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
                    text = if (isRussian) "Поиск дипломатов" else "Diplomatik qidiruv",
                    color = AutoTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRussian) "Страны, посольства, флаги и серии CMD, D, UN" else "Davlatlar, elchixonalar, bayroqlar va CMD, D, UN seriyalari",
                    color = AutoTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = if (isRussian) "Поиск страны или кода (07, США, Россия, UN...)" else "Mamlakat yoki kod qidirish (07, AQSh, Rossiya...)",
                    color = AutoTextMuted,
                    fontSize = 13.sp
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

        Spacer(modifier = Modifier.height(10.dp))

        // Sub Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                DiplomatFilterSubTab.ALL to (if (isRussian) "Все страны" else "Barchasi"),
                DiplomatFilterSubTab.HEADS_CMD to (if (isRussian) "CMD Послы" else "CMD Elchilar"),
                DiplomatFilterSubTab.DIPLOMATS_D to (if (isRussian) "D Дипломаты" else "D Diplomatlar"),
                DiplomatFilterSubTab.UN_MISSION to (if (isRussian) "🇺🇳 ООН" else "🇺🇳 BMT"),
                DiplomatFilterSubTab.TECH_STAFF_T to (if (isRussian) "T Техперсонал" else "T Texxodimlar"),
                DiplomatFilterSubTab.CONSULAR_X to (if (isRussian) "X Консульства" else "X Konsulliklar")
            )

            tabs.forEach { (tab, label) ->
                val isSelected = subTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                        .border(1.dp, if (isSelected) PlateEvGreenBadge else AutoBorder, RoundedCornerShape(14.dp))
                        .clickable { subTab = tab }
                        .padding(horizontal = 11.dp, vertical = 6.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Diplomatic missions list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (subTab in listOf(
                    DiplomatFilterSubTab.ALL,
                    DiplomatFilterSubTab.HEADS_CMD,
                    DiplomatFilterSubTab.DIPLOMATS_D,
                    DiplomatFilterSubTab.UN_MISSION
                )
            ) {
                items(filteredDiplomaticMissions, key = { it.codeDisplay }) { mission ->
                    DiplomaticCountryCard(
                        mission = mission,
                        isRussian = isRussian,
                        onSelectPlate = { samplePlate, _ ->
                            val targetPlate = when (subTab) {
                                DiplomatFilterSubTab.HEADS_CMD -> mission.cmdPlate ?: samplePlate
                                DiplomatFilterSubTab.DIPLOMATS_D -> mission.dPlate ?: samplePlate
                                else -> samplePlate
                            }
                            onDiplomatSelected(targetPlate)
                        }
                    )
                }
            } else {
                items(filteredOtherDiplomats, key = { it.id }) { rule ->
                    val samplePlate = when {
                        rule.series == "T" && rule.countryCode != null -> {
                            val c = if (rule.countryCode < 10) "00${rule.countryCode}" else if (rule.countryCode < 100) "0${rule.countryCode}" else "${rule.countryCode}"
                            "T $c 012"
                        }
                        rule.series == "X" && rule.countryCode != null -> {
                            val c = if (rule.countryCode < 10) "00${rule.countryCode}" else if (rule.countryCode < 100) "0${rule.countryCode}" else "${rule.countryCode}"
                            "X $c 001"
                        }
                        rule.series == "T" -> "T 007 012"
                        rule.series == "X" -> "X 007 001"
                        else -> "CMD 0101"
                    }

                    val country = if (isRussian) rule.countryNameRu else rule.countryNameUz
                    val orgTitle = if (isRussian) rule.orgNameRu else rule.orgNameUz
                    val desc = if (isRussian) rule.descriptionRu else rule.descriptionUz
                    val flag = DiplomaticFlags.getFlag(rule.countryCode)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AutoSurface)
                            .border(1.dp, AutoBorder, RoundedCornerShape(12.dp))
                            .clickable { onDiplomatSelected(samplePlate) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PlateEvGreenBadge.copy(alpha = 0.18f))
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (rule.countryCode != null) "Код ${rule.countryCode}" else (rule.series ?: "DIP"),
                                            color = PlateEvGreenBadge,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (country != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$flag $country",
                                            color = AutoTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = orgTitle,
                                    color = AutoTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = desc,
                                    color = AutoTextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF064E3B))
                                    .border(1.dp, PlateEvGreenBadge, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = samplePlate,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. ПОИСК ВЕДОМСТВ (PAA, PPP, APC, DAV, STY, ZDF, TSS, MV...)
// ============================================================================

enum class GovAgencySubTab {
    ALL,
    HIGHEST_STATE,
    KHOKIMIYATS_DAV,
    SECURITY_SERVICES,
    EMERGENCY
}

@Composable
fun GovernmentSearchScreen(
    rules: List<PlateRule>,
    isRussian: Boolean = true,
    onAgencySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var subTab by remember { mutableStateOf(GovAgencySubTab.ALL) }

    val filteredAgencies = remember(rules, searchQuery, subTab, isRussian) {
        val govRules = rules.filter {
            it.category in listOf(
                PlateCategory.STATE_AUTHORITY,
                PlateCategory.LAW_ENFORCEMENT,
                PlateCategory.MILITARY,
                PlateCategory.EMERGENCY
            ) || it.series in listOf("DAV", "STY")
        }

        govRules.filter { rule ->
            val matchesSubTab = when (subTab) {
                GovAgencySubTab.ALL -> true
                GovAgencySubTab.HIGHEST_STATE -> rule.series in listOf("PAA", "PSS", "PPP", "APC", "UZB")
                GovAgencySubTab.KHOKIMIYATS_DAV -> rule.series == "DAV"
                GovAgencySubTab.SECURITY_SERVICES -> rule.series in listOf("ZDF", "TSS", "TST", "VSF", "VSB", "VSC", "MV", "DXA")
                GovAgencySubTab.EMERGENCY -> rule.series in listOf("STY", "FVV")
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                (rule.series?.lowercase()?.contains(q) == true) ||
                        (rule.orgNameRu.lowercase().contains(q)) ||
                        (rule.orgNameUz.lowercase().contains(q)) ||
                        (rule.descriptionRu.lowercase().contains(q)) ||
                        (rule.descriptionUz.lowercase().contains(q))
            }

            matchesSubTab && matchesSearch
        }
    }

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
                    text = if (isRussian) "Поиск ведомств" else "Davlat idoralari qidiruv",
                    color = AutoTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRussian) "Госорганы, министерства, хокимияты и службы" else "Davlat organlari, vazirliklar, hokimliklar va xizmatlar",
                    color = AutoTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = if (isRussian) "Поиск ведомства (Сенат, Президент, DAV...)" else "Idora qidirish (Senat, Prezident, DAV...)",
                    color = AutoTextMuted,
                    fontSize = 13.sp
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

        Spacer(modifier = Modifier.height(10.dp))

        // Sub Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                GovAgencySubTab.ALL to (if (isRussian) "Все" else "Barchasi"),
                GovAgencySubTab.HIGHEST_STATE to (if (isRussian) "Высшие органы (PAA/PPP)" else "Oliy organlar"),
                GovAgencySubTab.KHOKIMIYATS_DAV to (if (isRussian) "Хокимияты (DAV)" else "Hokimliklar (DAV)"),
                GovAgencySubTab.SECURITY_SERVICES to (if (isRussian) "Силовые и Суды" else "Xavfsizlik va Sudlar"),
                GovAgencySubTab.EMERGENCY to (if (isRussian) "Экстренные (103 STY)" else "Tezkor (103 STY)")
            )

            tabs.forEach { (tab, label) ->
                val isSelected = subTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                        .border(1.dp, if (isSelected) PlateEvGreenBadge else AutoBorder, RoundedCornerShape(14.dp))
                        .clickable { subTab = tab }
                        .padding(horizontal = 11.dp, vertical = 6.dp)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Agency list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredAgencies, key = { it.id }) { rule ->
                val samplePlate = when (rule.series) {
                    "DAV" -> "01 123 DAV"
                    "STY" -> "01 103 STY"
                    else -> "${rule.series?.split(",")?.firstOrNull()?.trim() ?: "PAA"} 001"
                }

                val seriesLabel = rule.series ?: "ГОС"
                val orgTitle = if (isRussian) rule.orgNameRu else rule.orgNameUz
                val desc = if (isRussian) rule.descriptionRu else rule.descriptionUz
                val isTypeII = rule.series !in listOf("DAV", "STY")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AutoSurface)
                        .border(1.dp, AutoBorder, RoundedCornerShape(12.dp))
                        .clickable { onAgencySelected(samplePlate) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Series code badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PlateEvGreenBadge.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = seriesLabel,
                                        color = PlateEvGreenBadge,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Type tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isTypeII) "Тип II (Флаг слева)" else "Тип I (Служебный)",
                                        color = AutoTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = orgTitle,
                                color = AutoTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = desc,
                                color = AutoTextMuted,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Preview button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = samplePlate,
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. ПОИСК ПО ВОЕННЫМ И СПЕЦСЛУЖБАМ (Минобороны, СГБ, Погранвойска, Нацгвардия, МЧС, МВД)
// ============================================================================

enum class MilitarySubTab {
    ALL,
    DEFENSE,        // MV (Черные войсковые и белые аппарата МО)
    SECURITY,       // TSS, TST, USB, Погранвойска СГБ
    GUARD,          // PSF, PSP, PSV (Нацгвардия)
    EMERGENCY,      // FVV (МЧС)
    INTERNAL_AFFAIRS// VSB..VSV (МВД)
}

@Composable
fun MilitarySearchScreen(
    rules: List<PlateRule>,
    isRussian: Boolean = true,
    onAgencySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MilitarySubTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val militaryRules = remember(rules, selectedTab, searchQuery, isRussian) {
        val baseRules = rules.filter { rule ->
            rule.category in listOf(
                PlateCategory.MILITARY,
                PlateCategory.LAW_ENFORCEMENT,
                PlateCategory.EMERGENCY
            ) || rule.series in listOf("MV", "TSS", "TST", "USB", "PSF", "PSP", "PSV", "FVV")
        }.sortedWith(
            compareBy<PlateRule> {
                when {
                    it.category == PlateCategory.MILITARY || it.plateTheme == PlateTheme.BLACK -> 0
                    it.series == "MV" -> 1
                    it.series in listOf("TST", "USB", "TSS") || it.id.contains("border") -> 2
                    it.series in listOf("PSF", "PSP", "PSV") -> 3
                    it.series == "FVV" -> 4
                    it.series?.startsWith("VS") == true -> 5
                    it.series == "PSS" -> 6
                    it.series == "ZDF" -> 7
                    else -> 8
                }
            }
        )

        val filteredByTab = when (selectedTab) {
            MilitarySubTab.ALL -> baseRules
            MilitarySubTab.DEFENSE -> baseRules.filter { it.series == "MV" || it.id.contains("defense") }
            MilitarySubTab.SECURITY -> baseRules.filter {
                it.series in listOf("TSS", "TST", "USB") || it.id.contains("border") || it.id.contains("sgb") || it.id.contains("security")
            }
            MilitarySubTab.GUARD -> baseRules.filter {
                it.series in listOf("PSF", "PSP", "PSV") || it.id.contains("natguard")
            }
            MilitarySubTab.EMERGENCY -> baseRules.filter {
                it.series == "FVV" || it.id.contains("emergency")
            }
            MilitarySubTab.INTERNAL_AFFAIRS -> baseRules.filter {
                it.series?.contains("VS") == true || it.id.contains("mvd")
            }
        }

        if (searchQuery.isBlank()) {
            filteredByTab
        } else {
            val q = searchQuery.trim().lowercase()
            filteredByTab.filter { rule ->
                rule.orgNameRu.lowercase().contains(q) ||
                        rule.orgNameUz.lowercase().contains(q) ||
                        rule.descriptionRu.lowercase().contains(q) ||
                        rule.descriptionUz.lowercase().contains(q) ||
                        (rule.series?.lowercase()?.contains(q) == true)
            }
        }
    }

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
                    text = if (isRussian) "Военные и спецслужбы" else "Harbiy va maxsus xizmatlar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRussian) "Минобороны, СГБ, Погранвойска, Нацгвардия, МЧС" else "Mudofaa, DXX, Chegara, Milliy gvardiya, FVV",
                    color = AutoTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = if (isRussian) "Поиск службы (MV, СГБ, МЧС, 1234...)" else "Xizmat qidiruvi (MV, DXX, FVV...)",
                    color = AutoTextMuted,
                    fontSize = 13.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AutoSurface,
                unfocusedContainerColor = AutoSurface,
                focusedBorderColor = PlateEvGreenBadge,
                unfocusedBorderColor = AutoBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subcategory Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                MilitarySubTab.ALL to (if (isRussian) "Все" else "Barchasi"),
                MilitarySubTab.DEFENSE to (if (isRussian) "🎖️ Минобороны (MV)" else "🎖️ Mudofaa (MV)"),
                MilitarySubTab.SECURITY to (if (isRussian) "🛡️ СГБ и Погранвойска" else "🛡️ DXX va Chegara"),
                MilitarySubTab.GUARD to (if (isRussian) "⚔️ Нацгвардия" else "⚔️ Milliy gvardiya"),
                MilitarySubTab.EMERGENCY to (if (isRussian) "🚨 МЧС (FVV)" else "🚨 FVV"),
                MilitarySubTab.INTERNAL_AFFAIRS to (if (isRussian) "🚔 МВД" else "🚔 IIV")
            )

            tabs.forEach { (tab, label) ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PlateEvGreenBadge else AutoSurface)
                        .border(1.dp, if (isSelected) PlateEvGreenBadge else AutoBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else AutoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Agencies List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(militaryRules, key = { it.id }) { rule ->
                val orgTitle = if (isRussian) rule.orgNameRu else rule.orgNameUz
                val desc = if (isRussian) rule.descriptionRu else rule.descriptionUz
                val isBlackTheme = rule.plateTheme == PlateTheme.BLACK
                val seriesLabel = rule.series ?: "MV"
                val samplePlate = when {
                    isBlackTheme -> "01 1234 MV"
                    rule.series?.contains(",") == true -> "${rule.series.split(",").first().trim()} 001"
                    else -> "${rule.series ?: "MV"} 001"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AutoSurface)
                        .border(1.dp, AutoBorder, RoundedCornerShape(12.dp))
                        .clickable { onAgencySelected(samplePlate) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Series code badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isBlackTheme) Color(0xFF1E293B) else PlateEvGreenBadge.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = seriesLabel,
                                        color = if (isBlackTheme) Color.White else PlateEvGreenBadge,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Type tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isBlackTheme) "Войсковой (Черный фон)" else "Тип II (Флаг слева)",
                                        color = if (isBlackTheme) Color(0xFFFACC15) else AutoTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = orgTitle,
                                color = AutoTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = desc,
                                color = AutoTextMuted,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Preview button / plate miniature
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isBlackTheme) Color(0xFF111418) else Color.White)
                                .border(1.dp, if (isBlackTheme) Color(0xFFE2E8F0) else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = samplePlate,
                                color = if (isBlackTheme) Color.White else Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
