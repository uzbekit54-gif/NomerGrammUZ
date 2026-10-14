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
        .filter { it.category in listOf(PlateCategory.STATE_AUTHORITY, PlateCategory.LAW_ENFORCEMENT, PlateCategory.MILITARY, PlateCategory.EMERGENCY) && it.series != null }
        .flatMap { rule ->
            rule.series!!.split(",").map { it.trim() to rule }
        }
        .toMap()

    private val regionRulesByCode = rules
        .filter { it.category == PlateCategory.REGIONAL && it.regionCode != null }
        .associateBy { it.regionCode!! }

    // Precompiled Regular Expressions
    private val cmdRegex = Regex("^CMD\\s?(\\d{2,4})$")
    private val dRegex1 = Regex("^(\\d{3})\\s?D\\s?(\\d{2,3})$")
    private val dRegex2 = Regex("^D\\s?(\\d{3})\\s?(\\d{2,3})$")
    private val unRegex = Regex("^UN\\s?(\\d{2,4})$")
    private val tRegex = Regex("^(\\d{3})\\s?T\\s?(\\d{2,3})$")
    private val xRegex = Regex("^(\\d{3})\\s?X\\s?(\\d{2,3})$")
    private val mRegex = Regex("^(\\d{2})?\\s?M\\s?(\\d{4,6})$")
    private val hRegex = Regex("^(\\d{2})?\\s?H\\s?(\\d{4,6})$")

    // Government Type II (Series + Numbers without region): e.g. "PAA 001", "VSF 777"
    private val govTypeIIRegex = Regex("^([A-Z]{2,3})\\s?(\\d{3})$")

    // Regional plates: "01 123 DAV", "01 A 123 AA", "01 123 AAA", "01 123 TAA", "01 123 STY"
    private val regionalPlateRegex = Regex("^(\\d{2})\\s?([A-Z])?\\s?(\\d{3})\\s?([A-Z]{2,3})$")

    fun parse(rawInput: String, isRussian: Boolean = true): ParsedPlateResult {
        val normalized = PlateNormalizer.normalize(rawInput)
        val compact = PlateNormalizer.toCompact(rawInput)

        if (compact.isBlank()) {
            return emptyResult(rawInput, isRussian)
        }

        // 1. Try CMD (Head of Foreign Diplomatic Representation)
        val cmdMatch = cmdRegex.find(compact)
        if (cmdMatch != null) {
            val digits = cmdMatch.groupValues[1]
            val countryCode = if (digits.length >= 4) {
                digits.substring(0, 2).toIntOrNull()
            } else {
                digits.toIntOrNull()
            }
            return resolveDiplomaticHead(rawInput, normalized, compact, countryCode, isRussian)
        }

        // 2. Try D (Diplomatic Staff)
        val dMatch = dRegex1.find(compact) ?: dRegex2.find(compact)
        if (dMatch != null) {
            val countryCode = dMatch.groupValues[1].toIntOrNull()
            return resolveDiplomat(rawInput, normalized, compact, countryCode, isRussian)
        }

        // 3. Try UN (United Nations Agencies)
        val unMatch = unRegex.find(compact)
        if (unMatch != null) {
            val rule = rules.firstOrNull { it.series == "UN" }
            return ParsedPlateResult(
                rawInput = rawInput,
                normalizedInput = normalized,
                formattedPlate = "UN ${unMatch.groupValues[1]}",
                formatType = PlateFormatType.UN_AGENCY,
                theme = PlateTheme.BLUE,
                primaryRule = rule,
                organizationName = if (isRussian) "Представительство ООН в Узбекистане" else "BMTning O'zbekistondagi vakolatxonasi",
                description = if (isRussian) "Официальный транспорт миссий системы ООН" else "BMT tizimidagi tashkilotlar rasmiy avtotransporti"
            )
        }

        // 4. Try T (Technical Staff)
        val tMatch = tRegex.find(compact)
        if (tMatch != null) {
            val countryCode = tMatch.groupValues[1].toIntOrNull()
            val countryRule = diplomaticRulesByCountry[countryCode]?.firstOrNull()
            val cName = if (isRussian) countryRule?.countryNameRu else countryRule?.countryNameUz
            return ParsedPlateResult(
                rawInput = rawInput,
                normalizedInput = normalized,
                formattedPlate = "${tMatch.groupValues[1]} T ${tMatch.groupValues[2]}",
                formatType = PlateFormatType.TECHNICAL_STAFF,
                theme = PlateTheme.GREEN,
                primaryRule = countryRule,
                countryName = cName,
                organizationName = if (isRussian) "Посольство: ${cName ?: "Код $countryCode"}" else "Elchixona: ${cName ?: "Kod $countryCode"}",
                description = if (isRussian) "Административно-технический персонал диппредставительства" else "Diplomatik vakolatxona ma'muriy-texnik xodimlari"
            )
        }

        // 5. Try X (Consular)
        val xMatch = xRegex.find(compact)
        if (xMatch != null) {
            val countryCode = xMatch.groupValues[1].toIntOrNull()
            val countryRule = diplomaticRulesByCountry[countryCode]?.firstOrNull()
            val cName = if (isRussian) countryRule?.countryNameRu else countryRule?.countryNameUz
            return ParsedPlateResult(
                rawInput = rawInput,
                normalizedInput = normalized,
                formattedPlate = "${xMatch.groupValues[1]} X ${xMatch.groupValues[2]}",
                formatType = PlateFormatType.CONSULAR,
                theme = PlateTheme.GREEN,
                primaryRule = countryRule,
                countryName = cName,
                organizationName = if (isRussian) "Консульство: ${cName ?: "Код $countryCode"}" else "Konsullik: ${cName ?: "Kod $countryCode"}",
                description = if (isRussian) "Консульские учреждения и торговые представительства" else "Konsullik muassasalari va savdo vakolatxonalari"
            )
        }

        // 6. Try H (Foreign Citizens / Stateless persons)
        val hMatch = hRegex.find(compact)
        if (hMatch != null) {
            val regionPart = hMatch.groupValues[1]
            val regRule = regionRulesByCode[regionPart]
            return ParsedPlateResult(
                rawInput = rawInput,
                normalizedInput = normalized,
                formattedPlate = if (regionPart.isNotEmpty()) "$regionPart H ${hMatch.groupValues[2]}" else "H ${hMatch.groupValues[2]}",
                formatType = PlateFormatType.FOREIGN_CITIZEN,
                theme = PlateTheme.YELLOW,
                primaryRule = null,
                secondaryRegionRule = regRule,
                regionTitle = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz,
                organizationName = if (isRussian) "Иностранные граждане и лица без гражданства" else "Xorijiy fuqarolar va fuqaroligi bo'lmagan shaxslar",
                description = if (isRussian) "Транспорт иностранных граждан с постоянным видом на жительство" else "O'zbekistonda doimiy yashovchi chet el fuqarolari transporti"
            )
        }

        // 7. Try Government Type II (PAA, PSS, PPP, APC, UZB, ZDF, TSS, VSF, MV, FVV, etc.)
        val govMatch = govTypeIIRegex.find(compact)
        if (govMatch != null) {
            val series = govMatch.groupValues[1]
            val number = govMatch.groupValues[2]
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

        // 8. Try Regional Vehicle Formats: "01 123 DAV", "01 123 STY", "01 123 TAA", "01 A 123 AA", "01 123 AAA"
        val regMatch = regionalPlateRegex.find(compact)
        if (regMatch != null) {
            val region = regMatch.groupValues[1]
            val singleLetter = regMatch.groupValues[2]
            val numbers = regMatch.groupValues[3]
            val lettersSuffix = regMatch.groupValues[4]
            val regRule = regionRulesByCode[region]
            val regionTitle = if (isRussian) regRule?.orgNameRu else regRule?.orgNameUz

            // 8a. DAV (Regional Government Administration & Khokimiyats)
            if (lettersSuffix == "DAV") {
                val davRule = rules.firstOrNull { it.series == "DAV" }
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "$region $numbers DAV",
                    formatType = PlateFormatType.GOV_DUTY_REGIONAL,
                    theme = PlateTheme.WHITE,
                    primaryRule = davRule,
                    secondaryRegionRule = regRule,
                    regionTitle = regionTitle,
                    organizationName = if (isRussian) "Хокимият и органы госуправления ($regionTitle)" else "Hokimlik va davlat boshqaruvi organlari ($regionTitle)",
                    description = if (isRussian) "Персонально закрепленный и дежурный служебный автотранспорт" else "Biriktirilgan va navbatchi xizmat avtotransporti"
                )
            }

            // 8b. STY (Ambulance - 103)
            if (lettersSuffix == "STY") {
                val styRule = rules.firstOrNull { it.series == "STY" }
                return ParsedPlateResult(
                    rawInput = rawInput,
                    normalizedInput = normalized,
                    formattedPlate = "$region $numbers STY",
                    formatType = PlateFormatType.EMERGENCY_SERVICE,
                    theme = PlateTheme.WHITE,
                    primaryRule = styRule,
                    secondaryRegionRule = regRule,
                    regionTitle = regionTitle,
                    organizationName = if (isRussian) "Скорая медицинская помощь (103)" else "Tez tibbiy yordam (103)",
                    description = if (isRussian) "Специализированный медицинский автотранспорт" else "Tez tibbiy yordam maxsus transport vositasi"
                )
            }

            // 8c. TAXI (TTA, TTB, TTC...)
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
                    description = if (isRussian) "Автотранспорт пассажирских перевозок (такси)" else "Yo'lovchi tashuvchi taksi avtomobili"
                )
            }

            // 8d. Civilian Individual: "01 A 123 AA"
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
                    description = if (isRussian) "Частный легковой автотранспорт гражданина" else "Fuqaroning shaxsiy avtotransporti"
                )
            }

            // 8e. Civilian Legal Entity: "01 123 AAA"
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
                    description = if (isRussian) "Автотранспорт предприятий и коммерческих организаций" else "Korxona va tashkilotlar xizmat transporti"
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

        return emptyResult(rawInput, isRussian)
    }

    private fun resolveDiplomaticHead(
        raw: String,
        normalized: String,
        compact: String,
        countryCode: Int?,
        isRussian: Boolean
    ): ParsedPlateResult {
        val rule = diplomaticRulesByCountry[countryCode]?.firstOrNull { it.subType == "EMBASSY_HEAD" }
        val countryName = if (isRussian) rule?.countryNameRu else rule?.countryNameUz

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = "CMD ${compact.removePrefix("CMD")}",
            formatType = PlateFormatType.DIPLOMATIC_HEAD,
            theme = PlateTheme.GREEN,
            primaryRule = rule,
            countryName = countryName,
            organizationName = if (countryName != null) {
                if (isRussian) "Посольство: $countryName" else "$countryName Elchixonasi"
            } else {
                if (isRussian) "Иностранное дипломатическое представительство" else "Xorijiy diplomatik vakolatxona"
            },
            description = if (isRussian) {
                "Автомобиль руководителя дипломатического представительства (Чрезвычайный и Полномочный Посол)"
            } else {
                "Diplomatik vakolatxona rahbari xizmat avtomobili (Favqulodda va Muxtor Elchi)"
            }
        )
    }

    private fun resolveDiplomat(
        raw: String,
        normalized: String,
        compact: String,
        countryCode: Int?,
        isRussian: Boolean
    ): ParsedPlateResult {
        val rule = diplomaticRulesByCountry[countryCode]?.firstOrNull { it.subType == "DIPLOMAT" }
            ?: diplomaticRulesByCountry[countryCode]?.firstOrNull()
        val countryName = if (isRussian) rule?.countryNameRu else rule?.countryNameUz

        return ParsedPlateResult(
            rawInput = raw,
            normalizedInput = normalized,
            formattedPlate = normalized,
            formatType = PlateFormatType.DIPLOMAT,
            theme = PlateTheme.GREEN,
            primaryRule = rule,
            countryName = countryName,
            organizationName = if (countryName != null) {
                if (isRussian) "Посольство: $countryName" else "$countryName Elchixonasi"
            } else {
                if (isRussian) "Дипломатическое представительство" else "Diplomatik vakolatxona"
            },
            description = if (isRussian) {
                "Служебный транспорт дипломатических сотрудников посольства"
            } else {
                "Elchixona diplomatik xodimlari transport vositasi"
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
            description = if (isRussian) "Введите номер в формате 01 A123AA, PAA 001, CMD 0701 или выберите из каталога" else "Raqamni 01 A123AA, PAA 001, CMD 0701 shaklida kiriting",
            isIdentified = false
        )
    }
}
