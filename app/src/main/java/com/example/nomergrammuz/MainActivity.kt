package com.example.nomergrammuz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import com.example.nomergrammuz.data.repository.PlateRepository
import com.example.nomergrammuz.data.repository.UserPreferencesRepository
import com.example.nomergrammuz.domain.model.ParsedPlateResult
import com.example.nomergrammuz.domain.model.SavedPlateItem
import com.example.nomergrammuz.ui.camera.CameraScannerOverlay
import com.example.nomergrammuz.ui.catalog.CatalogFilterTab
import com.example.nomergrammuz.ui.catalog.CatalogScreen
import androidx.activity.compose.BackHandler
import android.widget.Toast
import com.example.nomergrammuz.ui.components.AutoSizedActionButton
import com.example.nomergrammuz.ui.components.AutoShrinkText
import com.example.nomergrammuz.ui.components.CompactPlateCard
import com.example.nomergrammuz.ui.components.UzbekistanLicensePlate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Color
import com.example.nomergrammuz.domain.model.PlateCategory
import com.example.nomergrammuz.domain.model.PlateRule
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.ui.saved.SavedPlatesScreen
import com.example.nomergrammuz.ui.search.DiplomatSearchScreen
import com.example.nomergrammuz.ui.search.GovernmentSearchScreen
import com.example.nomergrammuz.ui.search.MilitarySearchScreen
import com.example.nomergrammuz.ui.search.RegionSearchScreen
import com.example.nomergrammuz.ui.theme.AutoBackground
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextMuted
import com.example.nomergrammuz.ui.theme.AutoTextPrimary
import com.example.nomergrammuz.ui.theme.NomerGrammUZTheme
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge

enum class MainSearchCategory {
    ALL,
    REGIONS,
    DIPLOMATS,
    GOVERNMENT,
    MILITARY
}

enum class AppScreenMode {
    MAIN,
    SCANNER,
    CATALOG,
    SEARCH_REGIONS,
    SEARCH_DIPLOMATS,
    SEARCH_GOVERNMENT,
    SEARCH_MILITARY,
    SAVED
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NomerGrammUZTheme {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val repository = remember { PlateRepository(context) }
    val userPrefsRepo = remember { UserPreferencesRepository(context) }
    val parserEngine = remember(repository) { repository.getParserEngine() }
    val allRules = remember(repository) { repository.getAllRules() }

    var currentScreen by remember { mutableStateOf(AppScreenMode.MAIN) }
    var activeCategory by remember { mutableStateOf(MainSearchCategory.ALL) }
    var isRussian by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("CMD 0701") }
    var parseResult by remember { mutableStateOf<ParsedPlateResult?>(null) }
    var favoritesList by remember { mutableStateOf(userPrefsRepo.getFavorites()) }
    var historyList by remember { mutableStateOf(userPrefsRepo.getHistory()) }

    LaunchedEffect(inputText, isRussian) {
        val res = parserEngine.parse(inputText, isRussian = isRussian)
        parseResult = res
        if (res.isIdentified && res.primaryRule != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            userPrefsRepo.addToHistory(
                SavedPlateItem(
                    plate = res.formattedPlate,
                    title = res.organizationName,
                    subtitle = res.description,
                    formatType = res.formatType,
                    theme = res.theme
                )
            )
            historyList = userPrefsRepo.getHistory()
        }
    }

    var lastBackPressTime by remember { mutableStateOf(0L) }

    // System Back button handling (Decision A2):
    // When inside any sub-screen (Catalog, Scanner, Search, Saved), navigate back to MAIN
    BackHandler(enabled = currentScreen != AppScreenMode.MAIN) {
        currentScreen = AppScreenMode.MAIN
    }

    // On MAIN screen: require double back press within 2 seconds to exit gracefully
    BackHandler(enabled = currentScreen == AppScreenMode.MAIN) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? ComponentActivity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(
                context,
                if (isRussian) "Нажмите ещё раз для выхода" else "Chiqish uchun yana bir bor bosing",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreenMode.SCANNER -> {
                CameraScannerOverlay(
                    parserEngine = parserEngine,
                    isRussian = isRussian,
                    onPlateScanned = { scannedPlate ->
                        inputText = scannedPlate
                        currentScreen = AppScreenMode.MAIN
                    },
                    onClose = { currentScreen = AppScreenMode.MAIN }
                )
            }

            AppScreenMode.SEARCH_REGIONS -> {
                RegionSearchScreen(
                    rules = allRules,
                    isRussian = isRussian,
                    onRegionSelected = { selectedPlate ->
                        inputText = selectedPlate
                        currentScreen = AppScreenMode.MAIN
                    },
                    onBack = { currentScreen = AppScreenMode.MAIN }
                )
            }

            AppScreenMode.SEARCH_DIPLOMATS -> {
                DiplomatSearchScreen(
                    rules = allRules,
                    isRussian = isRussian,
                    onDiplomatSelected = { selectedPlate ->
                        inputText = selectedPlate
                        currentScreen = AppScreenMode.MAIN
                    },
                    onBack = { currentScreen = AppScreenMode.MAIN }
                )
            }

            AppScreenMode.SEARCH_GOVERNMENT -> {
                GovernmentSearchScreen(
                    rules = allRules,
                    isRussian = isRussian,
                    onAgencySelected = { selectedPlate ->
                        inputText = selectedPlate
                        currentScreen = AppScreenMode.MAIN
                    },
                    onBack = { currentScreen = AppScreenMode.MAIN }
                )
            }

            AppScreenMode.SEARCH_MILITARY -> {
                MilitarySearchScreen(
                    rules = allRules,
                    isRussian = isRussian,
                    onAgencySelected = { selectedPlate ->
                        inputText = selectedPlate
                        currentScreen = AppScreenMode.MAIN
                    },
                    onBack = { currentScreen = AppScreenMode.MAIN }
                )
            }

        AppScreenMode.CATALOG -> {
            CatalogScreen(
                rules = allRules,
                initialTab = CatalogFilterTab.ALL,
                isRussian = isRussian,
                onRuleSelected = { selectedRule ->
                    val samplePlate = when {
                        selectedRule.series == "CMD" && selectedRule.countryCode != null -> {
                            val codeStr = if (selectedRule.countryCode < 10) "0${selectedRule.countryCode}01" else "${selectedRule.countryCode}01"
                            "CMD $codeStr"
                        }
                        selectedRule.series == "D" && selectedRule.countryCode != null -> {
                            val codeStr = if (selectedRule.countryCode < 10) "00${selectedRule.countryCode}" else if (selectedRule.countryCode < 100) "0${selectedRule.countryCode}" else "${selectedRule.countryCode}"
                            "$codeStr D 001"
                        }
                        selectedRule.plateTheme == PlateTheme.BLACK || selectedRule.id == "gov_defense_troops" -> "01 1234 MV"
                        selectedRule.series == "DAV" -> "01 123 DAV"
                        selectedRule.series == "STY" -> "01 103 STY"
                        selectedRule.series == "M" -> "01 M 123456"
                        selectedRule.series == "H" -> "01 H 123456"
                        selectedRule.series == "UN" -> "UN 0001"
                        selectedRule.series == "T" -> "007 T 012"
                        selectedRule.series == "X" -> "007 X 001"
                        selectedRule.series?.contains("EV") == true -> "01 A 777 AA"
                        selectedRule.series != null -> "${selectedRule.series.split(',').first().trim()} 001"
                        selectedRule.regionCode != null -> "${selectedRule.regionCode} A 777 AA"
                        else -> "01 A 001 AA"
                    }
                    inputText = samplePlate
                    currentScreen = AppScreenMode.MAIN
                },
                onRefreshDatabase = {
                    val result = repository.refreshFromRemote()
                    if (result.isSuccess) {
                        val count = result.getOrDefault(allRules.size)
                        if (isRussian) "База актуальна ($count правил)" else "Baza yangilandi ($count qoida)"
                    } else {
                        if (isRussian) "Офлайн-режим (используется локальная база)" else "Oflayn rejim (lokal baza faol)"
                    }
                },
                onBack = { currentScreen = AppScreenMode.MAIN }
            )
        }

        AppScreenMode.SAVED -> {
            SavedPlatesScreen(
                favorites = favoritesList,
                history = historyList,
                isRussian = isRussian,
                onPlateSelected = { plate ->
                    inputText = plate
                    currentScreen = AppScreenMode.MAIN
                },
                onRemoveFavorite = { plate ->
                    userPrefsRepo.removeFavorite(plate)
                    favoritesList = userPrefsRepo.getFavorites()
                },
                onRemoveHistory = { plate ->
                    userPrefsRepo.removeHistory(plate)
                    historyList = userPrefsRepo.getHistory()
                },
                onClearHistory = {
                    userPrefsRepo.clearHistory()
                    historyList = emptyList()
                },
                onBack = { currentScreen = AppScreenMode.MAIN }
            )
        }

        AppScreenMode.MAIN -> {
            val categoryChips = remember(activeCategory, isRussian) {
                when (activeCategory) {
                    MainSearchCategory.ALL -> listOf(
                        "01 A 777 AA" to (if (isRussian) "01 Частный" else "01 Jismoniy"),
                        "PAA 001" to (if (isRussian) "PAA Президент" else "PAA Prezident"),
                        "01 1234 MV" to (if (isRussian) "01 1234 MV Войска" else "01 1234 MV Qo'shinlar"),
                        "CMD 0701" to (if (isRussian) "CMD Посол РФ" else "CMD Rossiya Elchisi"),
                        "D" to (if (isRussian) "Серия D" else "D seriyasi"),
                        "UN" to (if (isRussian) "Серия UN (ООН)" else "UN seriyasi (BMT)"),
                        "01 123 DAV" to (if (isRussian) "01 DAV Хокимият" else "01 DAV Hokimlik"),
                        "01 103 STY" to (if (isRussian) "103 Скорая" else "103 Tez yordam"),
                        "01 M 123456" to (if (isRussian) "Серия M (Инофирмы)" else "M seriyasi"),
                        "01 H 123456" to (if (isRussian) "Серия H (Иностранцы)" else "H seriyasi"),
                        "ZDF 001" to (if (isRussian) "ZDF Прокуратура" else "ZDF Prokuratura"),
                        "TSS 001" to (if (isRussian) "TSS СГБ" else "TSS DXX")
                    )
                    MainSearchCategory.REGIONS -> listOf(
                        "01 001 AAA" to (if (isRussian) "01 г. Ташкент" else "01 Toshkent sh."),
                        "10 001 AAA" to (if (isRussian) "10 Ташкентская обл." else "10 Toshkent vil."),
                        "20 001 AAA" to (if (isRussian) "20 Сырдарья" else "20 Sirdaryo"),
                        "25 001 AAA" to (if (isRussian) "25 Джизак" else "25 Jizzax"),
                        "30 001 AAA" to (if (isRussian) "30 Самарканд" else "30 Samarqand"),
                        "40 001 AAA" to (if (isRussian) "40 Фергана" else "40 Farg'ona"),
                        "50 001 AAA" to (if (isRussian) "50 Наманган" else "50 Namangan"),
                        "60 001 AAA" to (if (isRussian) "60 Андижан" else "60 Andijon"),
                        "70 001 AAA" to (if (isRussian) "70 Кашкадарья" else "70 Qashqadaryo"),
                        "75 001 AAA" to (if (isRussian) "75 Сурхандарья" else "75 Surxondaryo"),
                        "80 001 AAA" to (if (isRussian) "80 Бухара" else "80 Buxoro"),
                        "85 001 AAA" to (if (isRussian) "85 Навои" else "85 Navoiy"),
                        "90 001 AAA" to (if (isRussian) "90 Хорезм" else "90 Xorazm"),
                        "95 001 AAA" to (if (isRussian) "95 Каракалпакстан" else "95 Qoraqalpog'iston")
                    )
                    MainSearchCategory.DIPLOMATS -> listOf(
                        "CMD 0701" to (if (isRussian) "CMD 07 Посол РФ" else "CMD Rossiya Elchisi"),
                        "CMD 1001" to (if (isRussian) "CMD 10 Посол США" else "CMD AQSh Elchisi"),
                        "CMD 2401" to (if (isRussian) "CMD 24 Посол Германии" else "CMD Germaniya Elchisi"),
                        "CMD 2501" to (if (isRussian) "CMD 25 Посол КНР" else "CMD Xitoy Elchisi"),
                        "CMD 2001" to (if (isRussian) "CMD 20 Посол Турции" else "CMD Turkiya Elchisi"),
                        "CMD 0101" to (if (isRussian) "CMD 01 Посол Франции" else "CMD Fransiya Elchisi"),
                        "CMD 3001" to (if (isRussian) "CMD 30 Посол Казахстана" else "CMD Qozog'iston"),
                        "CMD 4101" to (if (isRussian) "CMD 41 Делегация ЕС" else "CMD YeI Delegatsiyasi"),
                        "007 D 001" to (if (isRussian) "007 D Дипломат РФ" else "007 D Rossiya"),
                        "010 D 001" to (if (isRussian) "010 D Дипломат США" else "010 D AQSh"),
                        "UN 0001" to (if (isRussian) "UN Представительство ООН" else "UN BMT vakolatxonasi"),
                        "007 T 012" to (if (isRussian) "Серия T (Техперсонал)" else "T seriyasi"),
                        "007 X 001" to (if (isRussian) "Серия X (Консулы)" else "X seriyasi")
                    )
                    MainSearchCategory.GOVERNMENT -> listOf(
                        "PAA 001" to (if (isRussian) "PAA Президент / Кабмин" else "PAA Prezident"),
                        "PPP 001" to (if (isRussian) "PPP Сенат / Депутаты" else "PPP Senat"),
                        "APC 100" to (if (isRussian) "APC Министерства" else "APC Vazirliklar"),
                        "UZB 001" to (if (isRussian) "UZB Руководители ведомств" else "UZB Idora rahbarlari"),
                        "01 123 DAV" to (if (isRussian) "01 DAV Служебный хокимият" else "01 DAV Hokimlik"),
                        "01 103 STY" to (if (isRussian) "103 Скорая помощь" else "103 Tez yordam"),
                        "ZDF 001" to (if (isRussian) "ZDF Генеральная прокуратура" else "ZDF Bosh prokuratura"),
                        "DXA 001" to (if (isRussian) "DXA Госуслуги / Минюст" else "DXA Davlat xizmatlari")
                    )
                    MainSearchCategory.MILITARY -> listOf(
                        "01 1234 MV" to (if (isRussian) "01 1234 MV Войска МО (ВАИ)" else "01 1234 MV Qo'shinlar"),
                        "MV 001" to (if (isRussian) "MV 001 Минобороны (Аппарат)" else "MV 001 Mudofaa apparati"),
                        "TST 001" to (if (isRussian) "TST Погранвойска СГБ" else "TST Chegara qo'shinlari"),
                        "TSS 001" to (if (isRussian) "TSS Служба госбезопасности" else "TSS Davlat xavfsizligi"),
                        "USB 001" to (if (isRussian) "USB Спецназ СГБ" else "USB DXX maxsus"),
                        "PSF 001" to (if (isRussian) "PSF Национальная гвардия" else "PSF Milliy gvardiya"),
                        "FVV 001" to (if (isRussian) "FVV 001 МЧС" else "FVV 001 FVV"),
                        "VSF 100" to (if (isRussian) "VSF МВД / ГУВД" else "VSF IIV / IIBB"),
                        "VSB 001" to (if (isRussian) "VSB Управление МВД" else "VSB IIV Boshqarmasi")
                    )
                }
            }

            val searchPlaceholder = when (activeCategory) {
                MainSearchCategory.ALL -> if (isRussian) "Поиск по номеру, серии, региону или ведомству..." else "Raqam, seriya, hudud yoki idorani qidirish..."
                MainSearchCategory.REGIONS -> if (isRussian) "Поиск региона (01, Самарканд, Бухара, 95...)" else "Hudud qidiruvi (01, Samarqand, Buxoro, 95...)"
                MainSearchCategory.DIPLOMATS -> if (isRussian) "Поиск дипломатов (CMD, D, UN, Россия, США, 007...)" else "Diplomatlar qidiruvi (CMD, D, UN, AQSh, 007...)"
                MainSearchCategory.GOVERNMENT -> if (isRussian) "Поиск госорганов (PAA, PPP, DAV, Прокуратура, 103...)" else "Davlat organlari (PAA, PPP, DAV, Prokuratura...)"
                MainSearchCategory.MILITARY -> if (isRussian) "Поиск военных (1234 MV, СГБ, Нацгвардия, МЧС...)" else "Harbiy transport (1234 MV, DXX, FVV, VAI...)"
            }

            val categorySuggestions = remember(activeCategory, inputText, allRules) {
                val q = inputText.trim().lowercase()
                when (activeCategory) {
                    MainSearchCategory.ALL -> {
                        if (q.isBlank()) emptyList()
                        else allRules.filter { rule ->
                            (rule.series?.lowercase()?.contains(q) == true) ||
                                    (rule.regionCode?.contains(q) == true) ||
                                    (rule.orgNameRu.lowercase().contains(q)) ||
                                    (rule.countryNameRu?.lowercase()?.contains(q) == true)
                        }.take(8)
                    }
                    MainSearchCategory.REGIONS -> {
                        allRules.filter { it.category == PlateCategory.REGIONAL && it.regionCode != null }
                            .filter { rule ->
                                q.isBlank() || (rule.regionCode?.contains(q) == true) || (rule.orgNameRu.lowercase().contains(q))
                            }
                    }
                    MainSearchCategory.DIPLOMATS -> {
                        allRules.filter { it.category == PlateCategory.DIPLOMATIC }
                            .filter { rule ->
                                q.isBlank() || (rule.series?.lowercase()?.contains(q) == true) ||
                                        (rule.countryNameRu?.lowercase()?.contains(q) == true) ||
                                        (rule.countryCode?.toString()?.contains(q) == true)
                            }.take(12)
                    }
                    MainSearchCategory.GOVERNMENT -> {
                        allRules.filter {
                            it.category in listOf(PlateCategory.STATE_AUTHORITY, PlateCategory.LAW_ENFORCEMENT) &&
                                    it.plateTheme != PlateTheme.BLACK &&
                                    it.series != null
                        }.filter { rule ->
                            q.isBlank() || (rule.series?.lowercase()?.contains(q) == true) ||
                                    (rule.orgNameRu.lowercase().contains(q))
                        }.take(12)
                    }
                    MainSearchCategory.MILITARY -> {
                        allRules.filter {
                            it.category in listOf(PlateCategory.MILITARY, PlateCategory.LAW_ENFORCEMENT, PlateCategory.EMERGENCY) ||
                                    it.plateTheme == PlateTheme.BLACK ||
                                    it.series in listOf("MV", "TST", "TSS", "USB", "PSF", "FVV")
                        }.filter { rule ->
                            q.isBlank() || (rule.series?.lowercase()?.contains(q) == true) ||
                                    (rule.orgNameRu.lowercase().contains(q)) ||
                                    (rule.descriptionRu.lowercase().contains(q))
                        }.take(12)
                    }
                }
            }

            fun getSamplePlate(rule: PlateRule): String {
                return when {
                    rule.plateTheme == PlateTheme.BLACK || rule.id == "gov_defense_troops" -> "01 1234 MV"
                    rule.series == "CMD" && rule.countryCode != null -> {
                        val codeStr = if (rule.countryCode < 10) "0${rule.countryCode}01" else "${rule.countryCode}01"
                        "CMD $codeStr"
                    }
                    rule.series == "D" && rule.countryCode != null -> {
                        val codeStr = if (rule.countryCode < 10) "00${rule.countryCode}" else if (rule.countryCode < 100) "0${rule.countryCode}" else "${rule.countryCode}"
                        "$codeStr D 001"
                    }
                    rule.series == "DAV" -> "01 123 DAV"
                    rule.series == "STY" -> "01 103 STY"
                    rule.series == "M" -> "01 M 123456"
                    rule.series == "H" -> "01 H 123456"
                    rule.series == "UN" -> "UN 0001"
                    rule.series == "T" -> "007 T 012"
                    rule.series == "X" -> "007 X 001"
                    rule.series != null -> "${rule.series.split(',').first().trim()} 001"
                    rule.regionCode != null -> "${rule.regionCode} 001 AAA"
                    else -> "01 A 001 AA"
                }
            }

            val mainScrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()
            val bringIntoViewRequester = remember { BringIntoViewRequester() }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AutoBackground),
                containerColor = AutoBackground
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                        .verticalScroll(mainScrollState)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Header Row: App Title & Language Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NomerGramm UZ",
                                color = AutoTextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isRussian) "Определитель автономеров РУз" else "O'zbekiston davlat raqamlari",
                                color = AutoTextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Saved & History Button (Decision 1A)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AutoSurface)
                                    .border(1.dp, AutoBorder, RoundedCornerShape(20.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        currentScreen = AppScreenMode.SAVED
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⭐", fontSize = 12.sp)
                                    if (favoritesList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${favoritesList.size}",
                                            color = PlateEvGreenBadge,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Quick Language Switcher Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AutoSurface)
                                    .clickable { isRussian = !isRussian }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isRussian) "RU | O'Z" else "O'Z | RU",
                                    color = PlateEvGreenBadge,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Photorealistic Plate Preview
                    parseResult?.let { result ->
                        UzbekistanLicensePlate(
                            result = result,
                            placeholderText = "01 A 001 AA"
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dedicated Category Search Grid (2x2) - In-place Category Search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategorySearchCard(
                            modifier = Modifier.weight(1f),
                            icon = "📍",
                            title = if (isRussian) "Регионы" else "Hududlar",
                            subtitle = if (isRussian) "01–95" else "01–95",
                            isSelected = activeCategory == MainSearchCategory.REGIONS,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (activeCategory == MainSearchCategory.REGIONS) {
                                    activeCategory = MainSearchCategory.ALL
                                } else {
                                    activeCategory = MainSearchCategory.REGIONS
                                    inputText = "01 001 AAA"
                                }
                            }
                        )

                        CategorySearchCard(
                            modifier = Modifier.weight(1f),
                            icon = "🌍",
                            title = if (isRussian) "Дипломаты" else "Diplomatlar",
                            subtitle = if (isRussian) "CMD, D, UN, T" else "CMD, D, UN",
                            isSelected = activeCategory == MainSearchCategory.DIPLOMATS,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (activeCategory == MainSearchCategory.DIPLOMATS) {
                                    activeCategory = MainSearchCategory.ALL
                                } else {
                                    activeCategory = MainSearchCategory.DIPLOMATS
                                    inputText = "CMD 0701"
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategorySearchCard(
                            modifier = Modifier.weight(1f),
                            icon = "🏛️",
                            title = if (isRussian) "Госорганы" else "Davlat organlari",
                            subtitle = if (isRussian) "PAA, DAV, ZDF" else "PAA, DAV, ZDF",
                            isSelected = activeCategory == MainSearchCategory.GOVERNMENT,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (activeCategory == MainSearchCategory.GOVERNMENT) {
                                    activeCategory = MainSearchCategory.ALL
                                } else {
                                    activeCategory = MainSearchCategory.GOVERNMENT
                                    inputText = "PAA 001"
                                }
                            }
                        )

                        CategorySearchCard(
                            modifier = Modifier.weight(1f),
                            icon = "🛡️",
                            title = if (isRussian) "Военные" else "Harbiy & Maxsus",
                            subtitle = if (isRussian) "MV, СГБ, МЧС" else "MV, DXX, FVV",
                            isSelected = activeCategory == MainSearchCategory.MILITARY,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (activeCategory == MainSearchCategory.MILITARY) {
                                    activeCategory = MainSearchCategory.ALL
                                } else {
                                    activeCategory = MainSearchCategory.MILITARY
                                    inputText = "01 1234 MV"
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Main Action Buttons Row (Camera Scanner + Catalog Directory)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Camera Scanner Button (Live OCR + Gallery Picker)
                        AutoSizedActionButton(
                            modifier = Modifier.weight(1f),
                            icon = "📷",
                            text = if (isRussian) "Сканер (камера)" else "Kamera orqali",
                            height = 48.dp,
                            backgroundColor = PlateEvGreenBadge.copy(alpha = 0.15f),
                            borderColor = PlateEvGreenBadge.copy(alpha = 0.5f),
                            textColor = PlateEvGreenBadge,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentScreen = AppScreenMode.SCANNER
                            }
                        )

                        // 2. Directory / Catalog Button
                        AutoSizedActionButton(
                            modifier = Modifier.weight(1f),
                            icon = "📚",
                            text = if (isRussian) "Справочник" else "Katalog",
                            height = 48.dp,
                            backgroundColor = AutoSurface,
                            borderColor = AutoBorder,
                            textColor = AutoTextPrimary,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentScreen = AppScreenMode.CATALOG
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active Category Banner
                    if (activeCategory != MainSearchCategory.ALL) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PlateEvGreenBadge)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (activeCategory) {
                                        MainSearchCategory.REGIONS -> if (isRussian) "Режим: Регионы (01–95)" else "Rejim: Hududlar (01–95)"
                                        MainSearchCategory.DIPLOMATS -> if (isRussian) "Режим: Дипломаты (CMD, D, UN, T, X)" else "Rejim: Diplomatlar"
                                        MainSearchCategory.GOVERNMENT -> if (isRussian) "Режим: Госорганы и ведомства" else "Rejim: Davlat organlari"
                                        MainSearchCategory.MILITARY -> if (isRussian) "Режим: Военные и спецслужбы" else "Rejim: Harbiy & Maxsus"
                                        else -> ""
                                    },
                                    color = PlateEvGreenBadge,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AutoSurface)
                                    .border(1.dp, AutoBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        activeCategory = MainSearchCategory.ALL
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isRussian) "✕ Сбросить" else "✕ Bekor qilish",
                                    color = AutoTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Search Input Field
                    val inputInteractionSource = remember { MutableInteractionSource() }
                    LaunchedEffect(inputInteractionSource) {
                        inputInteractionSource.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Release) {
                                keyboardController?.show()
                                coroutineScope.launch {
                                    delay(150)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        interactionSource = inputInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        delay(200)
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        placeholder = {
                            Text(
                                text = searchPlaceholder,
                                color = AutoTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        trailingIcon = {
                            if (inputText.isNotEmpty()) {
                                Text(
                                    text = "✕",
                                    color = AutoTextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            inputText = ""
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                        .padding(8.dp)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlateEvGreenBadge,
                            unfocusedBorderColor = if (activeCategory != MainSearchCategory.ALL) PlateEvGreenBadge.copy(alpha = 0.5f) else AutoBorder,
                            focusedContainerColor = AutoSurface,
                            unfocusedContainerColor = AutoSurface,
                            focusedTextColor = AutoTextPrimary,
                            unfocusedTextColor = AutoTextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Category Samples Chips (Horizontal Scroll)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryChips.forEach { (code, label) ->
                            val isSelected = inputText.equals(code, ignoreCase = true) ||
                                    (code.length <= 4 && inputText.startsWith(code, ignoreCase = true))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.5.dp,
                                        color = if (isSelected) PlateEvGreenBadge else AutoBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        inputText = code
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
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

                    // Matching category rule quick-select pills
                    if (categorySuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categorySuggestions.forEach { rule ->
                                val samplePlate = getSamplePlate(rule)
                                val isCurrent = inputText.equals(samplePlate, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isCurrent) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                                        .border(
                                            width = 1.dp,
                                            color = if (isCurrent) PlateEvGreenBadge else AutoBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            inputText = samplePlate
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val tag = rule.regionCode ?: rule.series?.split(",")?.first()?.trim() ?: "UZ"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (rule.plateTheme == PlateTheme.BLACK) Color(0xFF101214)
                                                    else if (rule.plateTheme == PlateTheme.GREEN) Color(0xFF14532D)
                                                    else if (rule.plateTheme == PlateTheme.BLUE) Color(0xFF1E3A8A)
                                                    else PlateEvGreenBadge.copy(alpha = 0.2f)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isRussian) rule.orgNameRu else rule.orgNameUz,
                                            color = AutoTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Compact Result Card (Decision 2B: Clean, concise; 1A: Favorite & Share)
                    parseResult?.let { result ->
                        CompactPlateCard(
                            result = result,
                            isRussian = isRussian,
                            isFavorite = userPrefsRepo.isFavorite(result.formattedPlate),
                            onToggleFavorite = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userPrefsRepo.toggleFavorite(
                                    SavedPlateItem(
                                        plate = result.formattedPlate,
                                        title = result.organizationName,
                                        subtitle = result.description,
                                        formatType = result.formatType,
                                        theme = result.theme
                                    )
                                )
                                favoritesList = userPrefsRepo.getFavorites()
                            },
                            onShare = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "NomerGramm UZ: ${result.formattedPlate}")
                                    val shareText = buildString {
                                        append("🇺🇿 NomerGramm UZ\n")
                                        append("Номер: ${result.formattedPlate}\n")
                                        append("Организация: ${result.organizationName}\n")
                                        append("Статус: ${result.description}\n")
                                        if (!result.countryName.isNullOrBlank()) append("Страна: ${result.countryName}\n")
                                        if (!result.regionTitle.isNullOrBlank()) append("Регион: ${result.regionTitle}\n")
                                        append("Основание: ПКМ РУз №131 от 27.06.2007")
                                    }
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, if (isRussian) "Поделиться номером" else "Raqamni ulashish"))
                            },
                            onCopy = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Plate", "${result.formattedPlate}: ${result.organizationName}")
                                clipboard.setPrimaryClip(clip)
                            }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun CategorySearchCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    subtitle: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PlateEvGreenBadge.copy(alpha = 0.16f) else AutoSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) PlateEvGreenBadge else AutoBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PlateEvGreenBadge.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                AutoShrinkText(
                    text = title,
                    color = if (isSelected) PlateEvGreenBadge else AutoTextPrimary,
                    initialFontSize = 12.sp,
                    minFontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                AutoShrinkText(
                    text = if (isSelected) "● Активно" else subtitle,
                    color = if (isSelected) PlateEvGreenBadge else AutoTextMuted,
                    initialFontSize = 10.sp,
                    minFontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}