package com.example.nomergrammuz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nomergrammuz.data.repository.PlateRepository
import com.example.nomergrammuz.domain.model.ParsedPlateResult
import com.example.nomergrammuz.ui.components.CompactPlateCard
import com.example.nomergrammuz.ui.components.UzbekistanLicensePlate
import com.example.nomergrammuz.ui.theme.AutoBackground
import com.example.nomergrammuz.ui.theme.AutoBorder
import com.example.nomergrammuz.ui.theme.AutoSurface
import com.example.nomergrammuz.ui.theme.AutoTextMuted
import com.example.nomergrammuz.ui.theme.AutoTextPrimary
import com.example.nomergrammuz.ui.theme.NomerGrammUZTheme
import com.example.nomergrammuz.ui.theme.PlateEvGreenBadge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NomerGrammUZTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Initialize Repository & Parser Engine
    val repository = remember { PlateRepository(context) }
    val parserEngine = remember(repository) { repository.getParserEngine() }

    var isRussian by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("CMD 0701") }
    var parseResult by remember { mutableStateOf<ParsedPlateResult?>(null) }

    // Re-parse when text or language changes
    LaunchedEffect(inputText, isRussian) {
        val res = parserEngine.parse(inputText, isRussian = isRussian)
        parseResult = res
        if (res.isIdentified && res.primaryRule != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val quickSampleChips = listOf(
        "CMD 0701" to (if (isRussian) "CMD Посол РФ" else "CMD Rossiya Elchisi"),
        "CMD 1001" to (if (isRussian) "CMD Посол США" else "CMD AQSh Elchisi"),
        "PAA 001" to (if (isRussian) "PAA Президент" else "PAA Prezident"),
        "PPP 001" to (if (isRussian) "PPP Сенат / Депутаты" else "PPP Senat"),
        "APC 100" to (if (isRussian) "APC Министерства" else "APC Vazirliklar"),
        "ZDF 001" to (if (isRussian) "ZDF Прокуратура" else "ZDF Prokuratura"),
        "TSS 001" to (if (isRussian) "TSS СГБ" else "TSS DXX"),
        "01 123 DAV" to (if (isRussian) "01 DAV Хокимият" else "01 DAV Hokimlik"),
        "01 123 STY" to (if (isRussian) "103 Скорая" else "103 Tez yordam"),
        "01 777 AAA" to (if (isRussian) "01 Юрлицо" else "01 Yuridik shaxs")
    )

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
                .verticalScroll(rememberScrollState())
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

            Spacer(modifier = Modifier.height(20.dp))

            // Photorealistic Plate Preview
            parseResult?.let { result ->
                UzbekistanLicensePlate(
                    result = result,
                    placeholderText = "01 A 001 AA"
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Search Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isRussian) "Введите номер (PAA 001, CMD 0701...)" else "Raqamni kiriting (PAA 001, CMD 0701...)",
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
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Samples Chips (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickSampleChips.forEach { (code, label) ->
                    val isSelected = inputText.equals(code, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) PlateEvGreenBadge.copy(alpha = 0.2f) else AutoSurface)
                            .clickable {
                                inputText = code
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

            Spacer(modifier = Modifier.height(20.dp))

            // Compact Result Card (Decision 2B: Clean, concise and un-cluttered)
            parseResult?.let { result ->
                CompactPlateCard(
                    result = result,
                    isRussian = isRussian
                )
            }
        }
    }
}