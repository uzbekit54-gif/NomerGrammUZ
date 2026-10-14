package com.example.nomergrammuz.domain.parser

import com.example.nomergrammuz.domain.model.ParsedPlateResult
import com.example.nomergrammuz.domain.model.PlateCategory
import com.example.nomergrammuz.domain.model.PlateFormatType
import com.example.nomergrammuz.domain.model.PlateRule
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.domain.normalizer.PlateNormalizer

class PlateParserEngine(
    private val rules: List<PlateRule>
) {

    // Cache rules by key properties for ultra-fast lookup
    private val diplomaticRulesByCountry = rules
        .filter { it.category == PlateCategory.DIPLOMATIC && it.countryCode != null }
        .groupBy { it.countryCode!! }

    private val govSpecialRulesBySeries = rules
        .filter {
            it.category in listOf(PlateCategory.STATE_AUTHORITY, PlateCategory.LAW_ENFORCEMENT, PlateCategory.MILITARY, PlateCategory.EMERGENCY) &&
                    it.series != null &&
                    it.plateTheme != PlateTheme.BLACK
        }
        .flatMap { rule ->
            rule.series!!.split(",").map { it.trim() to rule }
        }
        .toMap()

    private val regionRulesByCode = rules
        .filter { it.category == PlateCategory.REGIONAL && it.regionCode != null }
        .associateBy { it.regionCode!! }

    // Precompiled Regular Expressions for Decree № 131 Formats
    private val cmdRegex = Regex("^CMD\\s?(\\d{1,4})?$")
    private val dRegexCompact = Regex("^D(\\d{4,6})$")
    private val dRegex1 = Regex("^(\\d{1,3})\\s?D\\s?(\\d{1,4})?$")
    private val dRegex2 = Regex("^D\\s?(\\d{1,3})\\s?(\\d{1,4})?$")
    private val unRegex = Regex("^UN\\s?(\\d{1,4})?$")
    private val tRegexCompact = Regex("^T(\\d{4,6})$")
    private val tRegex1 = Regex("^(\\d{1,3})\\s?T\\s?(\\d{1,4})?$")
    private val tRegex2 = Regex("^T\\s?(\\d{1,3})\\s?(\\d{1,4})?$")
    private val xRegexCompact = Regex("^X(\\d{4,6})$")
    private val xRegex1 = Regex("^(\\d{1,3})\\s?X\\s?(\\d{1,4})?$")
    private val xRegex2 = Regex("^X\\s?(\\d{1,3})\\s?(\\d{1,4})?$")
    private val mRegex = Regex("^(\\d{2})?\\s?M\\s?(\\d{1,6})?$")
    private val hRegex = Regex("^(\\d{2})?\\s?H\\s?(\\d{1,6})?$")
    private val davRegex = Regex("^(\\d{2})?\\s?(\\d{1,3})?\\s?DAV$")
    private val styRegex = Regex("^(\\d{2})?\\s?(\\d{1,3})?\\s?STY$")

    // Military vehicles (Mudofaa vazirligi - black background): e.g. "01 1234 MV", "1234 MV", "01 MV 1234", "MV 1234"
    private val militaryPlateFullRegex = Regex("^(\\d{2})(\\d{3,4})MV$")
    private val militaryPlateShortRegex = Regex("^(\\d{3,4})MV$")
    private val militaryPlateRevFullRegex = Regex("^(\\d{2})MV(\\d{3,4})$")
    private val militaryPlateRevShortRegex = Regex("^MV(\\d{4})$")

    // Government Type II (Series + Numbers without region): e.g. "PAA 001", "VSF 777", or standalone "PAA"
    private val govTypeIIRegex = Regex("^([A-Z]{2,3})\\s?(\\d{1,4})?$")

    // Regional plates: "01 123 DAV", "01 A 123 AA", "01 123 AAA", "01 123 TAA", "01 123 STY"
    private val regionalPlateRegex = Regex("^(\\d{2})\\s?([A-Z])?\\s?(\\d{3})\\s?([A-Z]{2,3})$")

    fun parse(rawInput: String, isRussian: Boolean = true): ParsedPlateResult {
        val normalized = PlateNormalizer.normalize(rawInput)
        val compact = PlateNormalizer.toCompact(rawInput)

        if (compact.isBlank()) {
            return emptyResult(rawInput, isRussian)
        }

        // -------------------------------------------------------------
        // 1. Direct / Standalone Series Queries (Decree № 131 Types)
        // -------------------------------------------------------------

        // 1a. CMD (Type IV)
        val cmdMatch = cmdRegex.find(compact)
        if (cmdMatch != null) {
            val digits = cmdMatch.groupValues[1]
            val countryCode = when {
                digits.length >= 3 -> digits.substring(0, 2).toIntOrNull()
                digits.isNotEmpty() -> digits.toIntOrNull()
                else -> null
            }
            return resolveDiplomaticHead(rawInput, normalized, digits, countryCode, isRussian)
        }

        // 1b. D (Type V - Diplomatic Staff & Consuls)
        if (compact == "D") {
            return resolveDiplomat(rawInput, normalized, "", null, isRussian)
        }
        val dCompactMatch = dRegexCompact.find(compact)
        if (dCompactMatch != null) {
            val fullDigits = dCompactMatch.groupValues[1]
            val countryCode = fullDigits.take(3).toIntOrNull()
            val numPart = fullDigits.drop(3)
            return resolveDiplomat(rawInput, normalized, "${countryCode ?: 0} D $numPart", countryCode, isRussian)
        }
        val dMatch = dRegex1.find(compact) ?: dRegex2.find(compact)
        if (dMatch != null) {
            val countryCode = dMatch.groupValues[1].toIntOrNull()
            val numPart = dMatch.groupValues[2]
            val formatted = if (numPart.isNotEmpty()) "${dMatch.groupValues[1]} D $numPart" else "${dMatch.groupValues[1]} D"
            return resolveDiplomat(rawInput, normalized, formatted, countryCode, isRussian)
        }

        // 1c. UN (Type VI - United Nations Representation)
        val unMatch = unRegex.find(compact)
        if (unMatch != null) {
            val num = unMatch.groupValues[1]
            val formatted = if (num.isNotEmpty()) "UN $num" else "UN 0000"
            val rule = rules.firstOrNull { it.series == "UN" }
            return ParsedPlateResult(
                rawInput = rawInput,
                normalizedInput = normalized,
                formattedPlate = formatted,
                formatType = PlateFormatType.UN_AGENCY,
                theme = PlateTheme.BLUE,
                primaryRule = rule,
                organizationName = if (isRussian) "Представительство ООН в Республике Узбекистан (Серия UN)" else "BMTning O'zbekiston Respublikasidagi vakolatxonasi (UN seriyasi)",
                description = if (isRussian) "Тип VI по ПКМ РУз №131: Голубой фон, белые символы. Официальный транспорт миссий и специализированных учреждений ООН" else "PKM №131 bo'yicha VI tip: Moviy fon, oq belgilar. BMT tizimidagi tashkilotlar rasmiy avtotransporti"
            )
        }

        // 1d. T (Type VII - Administrative & Technical Staff)
        if (compact == "T") {
            return resolveTechnicalStaff(rawInput, normalized, "T 000000", null, isRussian)
        }
        val tCompactMatch = tRegexCompact.find(compact)
        if (tCompactMatch != null) {
            val fullDigits = tCompactMatch.groupValues[1]
            val countryCode = fullDigits.take(3).toIntOrNull()
            val numPart = fullDigits.drop(3)
            return resolveTechnicalStaff(rawInput, normalized, "${countryCode ?: 0} T $numPart", countryCode, isRussian)
        }
        val tMatch = tRegex1.find(compact) ?: tRegex2.find(compact)
        if (tMatch != null) {
            val countryCode = tMatch.groupValues[1].toIntOrNull()
            val numPart = tMatch.groupValues[2]
            val formatted = if (numPart.isNotEmpty()) "${tMatch.groupValues[1]} T $numPart" else "${tMatch.groupValues[1]} T"
            return resolveTechnicalStaff(rawInput, normalized, formatted, countryCode, isRussian)
        }

        // 1e. X (Type VIII - Consular & Trade Missions)
        if (compact == "X") {
            return resolveConsular(rawInput, normalized, "X 000000", null, isRussian)
        }
        val xCompactMatch = xRegexCompact.find(compact)
        if (xCompactMatch != null) {
            val fullDigits = xCompactMatch.groupValues[1]
            val countryCode = fullDigits.take(3).toIntOrNull()
            val numPart = fullDigits.drop(3)
            return resolveConsular(rawInput, normalized, "${countryCode ?: 0} X $numPart", countryCode, isRussian)
        }
        val xMatch = xRegex1.find(compact) ?: xRegex2.find(compact)
        if (xMatch != null) {
            val countryCode = xMatch.groupValues[1].toIntOrNull()
            val numPart = xMatch.groupValues[2]
            val formatted = if (numPart.isNotEmpty()) "${xMatch.groupValues[1]} X $numPart" else "${xMatch.groupValues[1]} X"
            return resolveConsular(rawInput, normalized, formatted, countryCode, isRussian)
        }

        // 1f. M (Type IX - Foreign Firms, Banks & Foreign Correspondents)
        if (compact == "M") {
            return resolveForeignFirm(rawInput, normalized, "01 M 000000", "01", isRussian)
        }
        val mMatch = mRegex.find(compact)
        if (mMatch != null) {
            val regionPart = mMatch.groupValues[1].ifEmpty { "01" }
            val numPart = mMatch.groupValues[2].ifEmpty { "000000" }
            return resolveForeignFirm(rawInput, normalized, "$regionPart M $numPart", regionPart, isRussian)
        }

        // 1g. H (Type X - Foreign Citizens & Stateless persons)
        if (compact == "H") {
            return resolveForeignCitizen(rawInput, normalized, "01 H 000000", "01", isRussian)
        }
        val hMatch = hRegex.find(compact)
        if (hMatch != null) {
            val regionPart = hMatch.groupValues[1].ifEmpty { "01" }
            val numPart = hMatch.groupValues[2].ifEmpty { "000000" }
            return resolveForeignCitizen(rawInput, normalized, "$regionPart H $numPart", regionPart, isRussian)
        }

        // 1h. DAV (Type I-1 - State Administration & Regional Khokimiyats)
        val davMatch = davRegex.find(compact)
        if (davMatch != null) {
            val region = davMatch.groupValues[1].ifEmpty { "01" }
            val num = davMatch.groupValues[2].ifEmpty { "000" }
            return resolveDav(rawInput, normalized, region, num, isRussian)
        }

        // 1i. STY (Type I-2 - Ambulance Service 103)
        val styMatch = styRegex.find(compact)
        if (styMatch != null) {
            val region = styMatch.groupValues[1].ifEmpty { "01" }
            val num = styMatch.groupValues[2].ifEmpty { "103" }
            return resolveSty(rawInput, normalized, region, num, isRussian)
        }

        // 1j. Standalone Region code (01..95 or 1..9)
        val paddedRegion = if (compact.length == 1 && compact[0].isDigit()) "0$compact" else compact
        if (paddedRegion.matches(Regex("^\\d{2}$")) && regionRulesByCode.containsKey(paddedRegion)) {
            val regRule = regionRulesByCode[paddedRegion]!!
            val regName = if (isRussian) regRule.orgNameRu else regRule.orgNameUz
            return ParsedPlateResult(
                rawInput = rawInput,
                normalizedInput = normalized,
                formattedPlate = "$paddedRegion 000 AAA",
                formatType = PlateFormatType.CIVILIAN_LEGAL,
                theme = PlateTheme.WHITE,
                primaryRule = regRule,
                secondaryRegionRule = regRule,
                regionTitle = regName,
                organizationName = if (isRussian) "Код региона $paddedRegion: $regName" else "$paddedRegion hudud kodi: $regName",
                description = if (isRussian) regRule.descriptionRu else regRule.descriptionUz
            )
        }

        // 1k. Military Vehicles (Mudofaa vazirligi - black background): "01 1234 MV", "1234 MV", "01 MV 1234", "MV 1234"
        val milFullMatch = militaryPlateFullRegex.find(compact)
        if (milFullMatch != null) {
            val reg = milFullMatch.groupValues[1]
            val num = milFullMatch.groupValues[2]
            return resolveMilitaryTroops(rawInput, normalized, reg, num, isRussian)
        }
        val milShortMatch = militaryPlateShortRegex.find(compact)
        if (milShortMatch != null) {
            val num = milShortMatch.groupValues[1]
            return resolveMilitaryTroops(rawInput, normalized, "01", num, isRussian)
        }
        val milRevFullMatch = militaryPlateRevFullRegex.find(compact)
        if (milRevFullMatch != null) {
            val reg = milRevFullMatch.groupValues[1]
            val num = milRevFullMatch.groupValues[2]
            return resolveMilitaryTroops(rawInput, normalized, reg, num, isRussian)
        }
        val milRevShortMatch = militaryPlateRevShortRegex.find(compact)
        if (milRevShortMatch != null) {
            val num = milRevShortMatch.groupValues[1]
            return resolveMilitaryTroops(rawInput, normalized, "01", num, isRussian)
        }

        // -------------------------------------------------------------
        // 2. Government Type II (PAA, PSS, PPP, APC, UZB, ZDF, TSS, VSF, MV, FVV, DXA)
        // -------------------------------------------------------------
        val govMatch = govTypeIIRegex.find(compact)
        if (govMatch != null) {
            val series = govMatch.groupValues[1]
            val number = govMatch.groupValues[2].ifEmpty { "000" }
            val matchedRule = govSpecialRulesBySeries[series]
            if (matchedRule != null) {
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "$series $number",
                    formatType = PlateFormatType.GOV_SPECIAL,
                    theme = PlateTheme.WHITE_GOV_FLAG,
                    primaryRule = matchedRule,
                    organizationName = if (isRussian) matchedRule.orgNameRu else matchedRule.orgNameUz,
                    description = if (isRussian) matchedRule.descriptionRu else matchedRule.descriptionUz
                )
            }
        }

        // -------------------------------------------------------------
        // 3. Regional Vehicle Formats: "01 123 DAV", "01 123 STY", "01 123 TAA", "01 A 123 AA", "01 123 AAA"
        // -------------------------------------------------------------
        val regMatch = regionalPlateRegex.find(compact)
        if (regMatch != null) {
            val region = regMatch.groupValues[1]
            val singleLetter = regMatch.groupValues[2]
            val numbers = regMatch.groupValues[3]
            val lettersSuffix = regMatch.groupValues[4]
            val regRule = regionRulesByCode[region]
            val regionTitle = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz

            // 3a. DAV
            if (lettersSuffix == "DAV") {
                return resolveDav(rawInput, normalized, region, numbers, isRussian)
            }

            // 3b. STY
            if (lettersSuffix == "STY") {
                return resolveSty(rawInput, normalized, region, numbers, isRussian)
            }

            // 3c. TAXI (TTA, TTB, TTC...)
            if (lettersSuffix.startsWith("TT") || lettersSuffix.startsWith("TA")) {
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "$region $numbers $lettersSuffix",
                    formatType = PlateFormatType.PUBLIC_TAXI,
                    theme = PlateTheme.YELLOW,
                    primaryRule = null,
                    secondaryRegionRule = regRule,
                    regionTitle = regionTitle,
                    organizationName = if (isRussian) "Линейное такси ($regionTitle)" else "Yo'nalishli taksi ($regionTitle)",
                    description = if (isRussian) "Автотранспорт лицензированных пассажирских перевозок (такси)" else "Litsenziyalangan yo'lovchi tashuvchi taksi avtomobili"
                )
            }

            // 3d. Civilian Individual: "01 A 123 AA" (Type III)
            if (singleLetter.isNotEmpty() && lettersSuffix.length == 2) {
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "$region $singleLetter $numbers $lettersSuffix",
                    formatType = PlateFormatType.CIVILIAN_INDIVIDUAL,
                    theme = PlateTheme.WHITE,
                    primaryRule = null,
                    secondaryRegionRule = regRule,
                    regionTitle = regionTitle,
                    organizationName = if (isRussian) "Физическое лицо ($regionTitle)" else "Jismoniy shaxs ($regionTitle)",
                    description = if (isRussian) "Тип III по ПКМ №131: Частный автотранспорт граждан Республики Узбекистан" else "PKM №131 bo'yicha III tip: O'zbekiston fuqarolarining shaxsiy avtotransporti"
                )
            }

            // 3e. Civilian Legal Entity: "01 123 AAA" (Type I)
            if (singleLetter.isEmpty() && lettersSuffix.length == 3) {
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "$region $numbers $lettersSuffix",
                    formatType = PlateFormatType.CIVILIAN_LEGAL,
                    theme = PlateTheme.WHITE,
                    primaryRule = null,
                    secondaryRegionRule = regRule,
                    regionTitle = regionTitle,
                    organizationName = if (isRussian) "Юридическое лицо ($regionTitle)" else "Yuridik shaxs ($regionTitle)",
                    description = if (isRussian) "Тип I по ПКМ №131: Автотранспорт предприятий, учреждений и организаций" else "PKM №131 bo'yicha I tip: Korxona va tashkilotlar xizmat transporti"
                )
            }
        }

        // Fallback: Check if input contains known series keyword
        for ((seriesKey, rule) in govSpecialRulesBySeries) {
            if (compact.contains(seriesKey)) {
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = normalized,
                    formatType = PlateFormatType.GOV_SPECIAL,
                    theme = PlateTheme.WHITE_GOV_FLAG,
                    primaryRule = rule,
                    organizationName = if (isRussian) rule.orgNameRu else rule.orgNameUz,
                    description = if (isRussian) rule.descriptionRu else rule.descriptionUz
                )
            }
        }

        // -------------------------------------------------------------
        // 4. Semantic / Text Keyword Search (Country, Region, Agency names)
        // -------------------------------------------------------------
        val queryClean = rawInput.trim().lowercase()
        if (queryClean.length >= 2) {
            // 4a. Region by name: e.g. "Самарканд", "Бухара", "Ташкент", "Toshkent", "Farg'ona"
            val matchedRegion = rules.firstOrNull { rule ->
                rule.category == PlateCategory.REGIONAL && (
                    rule.orgNameRu.lowercase().contains(queryClean) ||
                    rule.orgNameUz.lowercase().contains(queryClean) ||
                    rule.descriptionRu.lowercase().contains(queryClean) ||
                    rule.descriptionUz.lowercase().contains(queryClean)
                )
            }
            if (matchedRegion != null) {
                val regCode = matchedRegion.regionCode ?: "01"
                val regName = if (isRussian) matchedRegion.orgNameRu else matchedRegion.orgNameUz
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "$regCode 001 AAA",
                    formatType = PlateFormatType.CIVILIAN_LEGAL,
                    theme = PlateTheme.WHITE,
                    primaryRule = matchedRegion,
                    secondaryRegionRule = matchedRegion,
                    regionTitle = regName,
                    organizationName = if (isRussian) "Код региона $regCode: $regName" else "$regCode hudud kodi: $regName",
                    description = if (isRussian) matchedRegion.descriptionRu else matchedRegion.descriptionUz
                )
            }

            // 4b. Diplomatic Country by name: e.g. "Германия", "США", "Россия", "Казахстан", "AQSh", "Rossiya"
            val matchedDipCountry = rules.firstOrNull { rule ->
                rule.category == PlateCategory.DIPLOMATIC && rule.countryCode != null && (
                    rule.countryNameRu?.lowercase()?.contains(queryClean) == true ||
                    rule.countryNameUz?.lowercase()?.contains(queryClean) == true
                )
            }
            if (matchedDipCountry != null) {
                val cCode = matchedDipCountry.countryCode!!
                val cCodeStr = if (cCode < 10) "0$cCode" else "$cCode"
                val cName = if (isRussian) matchedDipCountry.countryNameRu else matchedDipCountry.countryNameUz
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "CMD ${cCodeStr}01",
                    formatType = PlateFormatType.DIPLOMATIC_HEAD,
                    theme = PlateTheme.GREEN,
                    primaryRule = matchedDipCountry,
                    countryName = cName,
                    organizationName = if (isRussian) "Посольство: $cName (Код $cCode)" else "$cName Elchixonasi ($cCode-kod)",
                    description = if (isRussian) "Дипломатическое представительство страны в Республике Узбекистан. Код страны на дипломатических номерах (CMD, D, T, X): $cCodeStr" else "O'zbekistondagi diplomatik vakolatxonasi. Diplomatik raqamlardagi mamlakat kodi (CMD, D, T, X): $cCodeStr"
                )
            }

            // 4c. Government / Security Agency by name: e.g. "Президент", "Сенат", "Хокимият", "Скорая", "Прокуратура", "МВД", "СГБ"
            val matchedGov = rules.firstOrNull { rule ->
                rule.category in listOf(PlateCategory.STATE_AUTHORITY, PlateCategory.LAW_ENFORCEMENT, PlateCategory.MILITARY, PlateCategory.EMERGENCY) && (
                    rule.orgNameRu.lowercase().contains(queryClean) ||
                    rule.orgNameUz.lowercase().contains(queryClean) ||
                    rule.descriptionRu.lowercase().contains(queryClean) ||
                    rule.descriptionUz.lowercase().contains(queryClean)
                )
            }
            if (matchedGov != null) {
                val isMilitary = matchedGov.plateTheme == PlateTheme.BLACK
                val samplePlate = when {
                    isMilitary -> "01 1234 MV"
                    matchedGov.series == "DAV" -> "01 123 DAV"
                    matchedGov.series == "STY" -> "01 103 STY"
                    else -> "${matchedGov.series?.split(",")?.firstOrNull()?.trim() ?: "PAA"} 001"
                }
                val isGovTypeII = matchedGov.series !in listOf("DAV", "STY") && !isMilitary
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = samplePlate,
                    formatType = when {
                        isMilitary -> PlateFormatType.MILITARY
                        isGovTypeII -> PlateFormatType.GOV_SPECIAL
                        else -> PlateFormatType.GOV_DUTY_REGIONAL
                    },
                    theme = when {
                        isMilitary -> PlateTheme.BLACK
                        isGovTypeII -> PlateTheme.WHITE_GOV_FLAG
                        else -> PlateTheme.WHITE
                    },
                    primaryRule = matchedGov,
                    organizationName = if (isRussian) matchedGov.orgNameRu else matchedGov.orgNameUz,
                    description = if (isRussian) matchedGov.descriptionRu else matchedGov.descriptionUz
                )
            }
        }

        return emptyResult(rawInput, isRussian)
    }

    private fun resolveMilitaryTroops(
        rawInput: String,
        normalized: String,
        regionCode: String?,
        number: String,
        isRussian: Boolean
    ): ParsedPlateResult {
        val reg = if (regionCode.isNullOrEmpty()) "01" else regionCode
        val regRule = regionRulesByCode[reg]
        val regName = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz
        val formatted = "$reg $number MV"
        val militaryRule = rules.firstOrNull { it.id == "gov_defense_troops" }
            ?: rules.firstOrNull { it.id == "gov_defense" }

        return ParsedPlateResult(
            rawInput = rawInput,
            normalizedInput = normalized,
            formattedPlate = formatted,
            formatType = PlateFormatType.MILITARY,
            theme = PlateTheme.BLACK,
            primaryRule = militaryRule,
            secondaryRegionRule = regRule,
            regionTitle = regName,
            organizationName = if (isRussian) {
                "Министерство обороны: Войсковой автотранспорт" + (if (regName != null) " ($regName)" else "")
            } else {
                "Mudofaa vazirligi: Harbiy qismlar transporti" + (if (regName != null) " ($regName)" else "")
            },
            description = if (isRussian) {
                "Военный автотранспорт Вооруженных Сил Республики Узбекистан (черный фон, белые символы). Служебный, боевой и специальный автотранспорт войсковых частей, полигонов и оперативных подразделений МО РУз. Учет ведет Военная автомобильная инспекция (ВАИ)."
            } else {
                "O'zbekiston Respublikasi Qurolli Kuchlari harbiy avtotransporti (qora fon, oq belgilar). Harbiy qismlar va jangovar texnikalarning xizmat transporti. Mudofaa vazirligi Harbiy avtonazoratida (VAI) ro'yxatga olingan."
            }
        )
    }

    private fun resolveDiplomaticHead(
        raw: String,
        normalized: String,
        digits: String,
        countryCode: Int?,
        isRussian: Boolean
    ): ParsedPlateResult {
        val rule = if (countryCode != null) {
            diplomaticRulesByCountry[countryCode]?.firstOrNull { it.subType == "EMBASSY_HEAD" }
        } else {
            rules.firstOrNull { it.id == "dip_series_cmd" } ?: rules.firstOrNull { it.series == "CMD" && it.countryCode == null }
        }
        val countryName = if (countryCode != null) {
            if (isRussian) rule?.countryNameRu else rule?.countryNameUz
        } else null

        val displayPlate = if (digits.isNotEmpty()) "CMD $digits" else "CMD 0000"

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = displayPlate,
            formatType = PlateFormatType.DIPLOMATIC_HEAD,
            theme = PlateTheme.GREEN,
            primaryRule = rule,
            countryName = countryName,
            organizationName = if (countryName != null) {
                if (isRussian) "Посольство: $countryName" else "$countryName Elchixonasi"
            } else {
                if (isRussian) "Руководители иностранных дипломатических миссий (Послы)" else "Xorijiy diplomatik vakolatxonalar rahbarlari (Elchilar)"
            },
            description = if (isRussian) {
                "Тип IV по ПКМ РУз №131: Зеленый фон, белые символы. Служебный автотранспорт руководителей иностранных дипломатических миссий (Чрезвычайный и Полномочный Посол)"
            } else {
                "PKM №131 bo'yicha IV tip: Yashil fon, oq belgilar. Diplomatik vakolatxona rahbari xizmat avtomobili (Favqulodda va Muxtor Elchi)"
            }
        )
    }

    private fun resolveDiplomat(
        raw: String,
        normalized: String,
        formatted: String,
        countryCode: Int?,
        isRussian: Boolean
    ): ParsedPlateResult {
        val rule = if (countryCode != null) {
            diplomaticRulesByCountry[countryCode]?.firstOrNull { it.subType == "DIPLOMAT" }
                ?: diplomaticRulesByCountry[countryCode]?.firstOrNull()
        } else {
            rules.firstOrNull { it.id == "dip_series_d" } ?: rules.firstOrNull { it.series == "D" && it.countryCode == null }
        }
        val countryName = if (countryCode != null) {
            if (isRussian) rule?.countryNameRu else rule?.countryNameUz
        } else null

        val displayPlate = formatted.ifEmpty { "D 000000" }

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = displayPlate,
            formatType = PlateFormatType.DIPLOMAT,
            theme = PlateTheme.GREEN,
            primaryRule = rule,
            countryName = countryName,
            organizationName = if (countryName != null) {
                if (isRussian) "Посольство / Консульство: $countryName" else "$countryName Elchixonasi / Konsulligi"
            } else {
                if (isRussian) "Дипломатический персонал и консулы (Серия D)" else "Diplomatik xodimlar va konsullar (D seriyasi)"
            },
            description = if (isRussian) {
                "Тип V по ПКМ РУз №131: Зеленый фон, белые символы. Служебный и личный автотранспорт дипломатических агентов, консулов и членов их семей"
            } else {
                "PKM №131 bo'yicha V tip: Yashil fon, oq belgilar. Diplomatlar, konsullar va ularning oila a'zolari transport vositasi"
            }
        )
    }

    private fun resolveTechnicalStaff(
        raw: String,
        normalized: String,
        formatted: String,
        countryCode: Int?,
        isRussian: Boolean
    ): ParsedPlateResult {
        val countryRule = if (countryCode != null) {
            diplomaticRulesByCountry[countryCode]?.firstOrNull()
        } else {
            rules.firstOrNull { it.id == "dip_series_t" } ?: rules.firstOrNull { it.series == "T" && it.countryCode == null }
        }
        val cName = if (countryCode != null) {
            if (isRussian) countryRule?.countryNameRu else countryRule?.countryNameUz
        } else null

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = formatted,
            formatType = PlateFormatType.TECHNICAL_STAFF,
            theme = PlateTheme.GREEN,
            primaryRule = countryRule,
            countryName = cName,
            organizationName = if (cName != null) {
                if (isRussian) "Посольство ($cName) — Техперсонал" else "Elchixona ($cName) — Texnik xodimlar"
            } else {
                if (isRussian) "Административно-технический персонал дипмиссий (Серия T)" else "Diplomatik vakolatxona ma'muriy-texnik xodimlari (T seriyasi)"
            },
            description = if (isRussian) {
                "Тип VII по ПКМ РУз №131: Зеленый фон, белые символы. Автотранспорт административно-технического персонала посольств, консульств и программ техсодействия"
            } else {
                "PKM №131 bo'yicha VII tip: Yashil fon, oq belgilar. Elchixona, konsullik va texnik yordam dasturlarining ma'muriy-texnik xodimlari transporti"
            }
        )
    }

    private fun resolveConsular(
        raw: String,
        normalized: String,
        formatted: String,
        countryCode: Int?,
        isRussian: Boolean
    ): ParsedPlateResult {
        val countryRule = if (countryCode != null) {
            diplomaticRulesByCountry[countryCode]?.firstOrNull()
        } else {
            rules.firstOrNull { it.id == "dip_series_x" } ?: rules.firstOrNull { it.series == "X" && it.countryCode == null }
        }
        val cName = if (countryCode != null) {
            if (isRussian) countryRule?.countryNameRu else countryRule?.countryNameUz
        } else null

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = formatted,
            formatType = PlateFormatType.CONSULAR,
            theme = PlateTheme.GREEN,
            primaryRule = countryRule,
            countryName = cName,
            organizationName = if (cName != null) {
                if (isRussian) "Консульство ($cName) — Серия X" else "Konsullik ($cName) — X seriyasi"
            } else {
                if (isRussian) "Консульские учреждения и торгпредства (Серия X)" else "Konsullik muassasalari va savdo vakolatxonalari (X seriyasi)"
            },
            description = if (isRussian) {
                "Тип VIII по ПКМ РУз №131: Зеленый фон, белые символы. Служебный автотранспорт глав консульств, почетных консулов, торговых представительств"
            } else {
                "PKM №131 bo'yicha VIII tip: Yashil fon, oq belgilar. Konsullik rahbarlari, faxriy konsullar va savdo vakolatxonalari xizmat avtotransporti"
            }
        )
    }

    private fun resolveForeignFirm(
        raw: String,
        normalized: String,
        formatted: String,
        regionCode: String,
        isRussian: Boolean
    ): ParsedPlateResult {
        val regRule = regionRulesByCode[regionCode]
        val regTitle = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz
        val mRule = rules.firstOrNull { it.series == "M" }

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = formatted,
            formatType = PlateFormatType.FOREIGN_FIRM,
            theme = PlateTheme.GREEN,
            primaryRule = mRule,
            secondaryRegionRule = regRule,
            regionTitle = regTitle,
            organizationName = if (isRussian) "Иностранные компании, банки и инвесторы ($regTitle)" else "Xorijiy kompaniyalar, banklar va investorlar ($regTitle)",
            description = if (isRussian) {
                "Тип IX по ПКМ РУз №131: Зеленый фон, белые символы, код региона слева. Автотранспорт иностранных фирм, банков, СП, инвесторов и иностранных СМИ"
            } else {
                "PKM №131 bo'yicha IX tip: Yashil fon, oq belgilar, chapda viloyat kodi. Xorijiy tijorat tashkilotlari, banklar, xorijiy investitsiyali korxonalar va xorijiy muxbirlar transporti"
            }
        )
    }

    private fun resolveForeignCitizen(
        raw: String,
        normalized: String,
        formatted: String,
        regionCode: String,
        isRussian: Boolean
    ): ParsedPlateResult {
        val regRule = regionRulesByCode[regionCode]
        val regTitle = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz
        val hRule = rules.firstOrNull { it.series == "H" }

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = formatted,
            formatType = PlateFormatType.FOREIGN_CITIZEN,
            theme = PlateTheme.YELLOW,
            primaryRule = hRule,
            secondaryRegionRule = regRule,
            regionTitle = regTitle,
            organizationName = if (isRussian) "Иностранные граждане и лица без гражданства ($regTitle)" else "Xorijiy fuqarolar va fuqaroligi bo'lmagan shaxslar ($regTitle)",
            description = if (isRussian) {
                "Тип X по ПКМ РУз №131: Желтый фон, черные символы, код региона слева. Автотранспорт иностранных граждан, постоянно проживающих или временно пребывающих в Узбекистане"
            } else {
                "PKM №131 bo'yicha X tip: Sariq fon, qora belgilar, chapda viloyat kodi. O'zbekistonda doimiy yoki vaqtincha yashovchi xorijiy fuqarolar transporti"
            }
        )
    }

    private fun resolveDav(
        raw: String,
        normalized: String,
        region: String,
        number: String,
        isRussian: Boolean
    ): ParsedPlateResult {
        val davRule = rules.firstOrNull { it.series == "DAV" }
        val regRule = regionRulesByCode[region]
        val regionTitle = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = "$region $number DAV",
            formatType = PlateFormatType.GOV_DUTY_REGIONAL,
            theme = PlateTheme.WHITE,
            primaryRule = davRule,
            secondaryRegionRule = regRule,
            regionTitle = regionTitle,
            organizationName = if (isRussian) "Хокимият и органы госуправления ($regionTitle)" else "Hokimlik va davlat boshqaruvi organlari ($regionTitle)",
            description = if (isRussian) {
                "Тип I-1 по ПКМ №131 (введён ПКМ №258): Белый фон, код региона слева, флаг и UZ справа. Персонально закрепленный и дежурный служебный автотранспорт"
            } else {
                "PKM №131 bo'yicha I-1 tip (PKM №258 asosida): Oq fon, chapda hudud kodi, o'ngda bayroq va UZ. Biriktirilgan va navbatchi xizmat avtotransporti"
            }
        )
    }

    private fun resolveSty(
        raw: String,
        normalized: String,
        region: String,
        number: String,
        isRussian: Boolean
    ): ParsedPlateResult {
        val styRule = rules.firstOrNull { it.series == "STY" }
        val regRule = regionRulesByCode[region]
        val regionTitle = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = "$region $number STY",
            formatType = PlateFormatType.EMERGENCY_SERVICE,
            theme = PlateTheme.WHITE,
            primaryRule = styRule,
            secondaryRegionRule = regRule,
            regionTitle = regionTitle,
            organizationName = if (isRussian) "Скорая медицинская помощь 103 ($regionTitle)" else "Tez tibbiy yordam 103 ($regionTitle)",
            description = if (isRussian) {
                "Тип I-2 по ПКМ №131 (введён ПКМ №429 от 17.07.2024): Белый фон, код региона слева, флаг и UZ справа. Автомобили скорой помощи (включая электромобили Тип IЕ-2)"
            } else {
                "PKM №131 bo'yicha I-2 tip (PKM №429 asosida): Oq fon, chapda hudud kodi, o'ngda bayroq va UZ. Tez tibbiy yordam avtotransporti (IЕ-2 elektromobillar ham)"
            }
        )
    }

    private fun emptyResult(raw: String, isRussian: Boolean): ParsedPlateResult {
        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = PlateNormalizer.normalize(raw),
            formattedPlate = PlateNormalizer.normalize(raw),
            formatType = PlateFormatType.UNKNOWN,
            theme = PlateTheme.WHITE,
            primaryRule = null,
            organizationName = if (isRussian) "Номер не определен" else "Raqam aniqlanmadi",
            description = if (isRussian) "Введите номер в формате 01 A123AA, PAA 001, CMD 0701, серию (D, UN, T, M, H) или выберите из каталога" else "Raqamni 01 A123AA, PAA 001, CMD 0701, seriyani (D, UN, T, M, H) shaklida kiriting",
            isIdentified = false
        )
    }
}
