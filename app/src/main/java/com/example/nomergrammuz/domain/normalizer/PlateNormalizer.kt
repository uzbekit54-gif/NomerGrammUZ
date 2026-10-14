package com.example.nomergrammuz.domain.normalizer

object PlateNormalizer {

    private val cyrillicAcronymMap = mapOf(
        "СМД" to "CMD",
        "ООН" to "UN",
        "ПСС" to "PSS",
        "ППП" to "PPP",
        "УЗБ" to "UZB",
        "АПС" to "APC",
        "ЗДФ" to "ZDF",
        "ДАВ" to "DAV",
        "ТСС" to "TSS",
        "ТСТ" to "TST",
        "ДХА" to "DXA",
        "ФВВ" to "FVV",
        "ВСФ" to "VSF",
        "ВСВ" to "VSB",
        "ВСС" to "VSC",
        "ВSС" to "VSC",
        "СТЙ" to "STY",
        "СТИ" to "STY"
    )

    private val cyrillicToLatinMap = mapOf(
        // Visual homoglyphs (lookalikes)
        'А' to 'A', 'а' to 'A',
        'В' to 'B', 'в' to 'B',
        'С' to 'C', 'с' to 'C',
        'Е' to 'E', 'е' to 'E',
        'Н' to 'H', 'н' to 'H',
        'К' to 'K', 'к' to 'K',
        'М' to 'M', 'м' to 'M',
        'О' to 'O', 'о' to 'O',
        'Р' to 'P', 'р' to 'P',
        'Т' to 'T', 'т' to 'T',
        'Х' to 'X', 'х' to 'X',
        'У' to 'Y', 'у' to 'Y',

        // Phonetic bridges for Uzbek plates when typed on Russian keyboard
        'Д' to 'D', 'д' to 'D',
        'З' to 'Z', 'з' to 'Z',
        'Ф' to 'F', 'ф' to 'F',
        'П' to 'P', 'п' to 'P',
        'Б' to 'B', 'б' to 'B'
    )

    /**
     * Normalizes an input string:
     * 1. Replaces known Russian acronym tokens (e.g. СМД -> CMD, ВСВ -> VSB).
     * 2. Translates individual Cyrillic letters to Latin uppercase.
     * 3. Trims and collapses multiple spaces.
     */
    fun normalize(input: String): String {
        if (input.isBlank()) return ""

        // 1. Token-level acronym replacement
        val tokens = input.trim().split(Regex("\\s+")).map { token ->
            cyrillicAcronymMap[token.uppercase()] ?: token
        }
        val tokenized = tokens.joinToString(" ")

        // 2. Character-by-character mapping
        val sb = StringBuilder()
        for (char in tokenized) {
            val converted = cyrillicToLatinMap[char] ?: char
            if (converted.isLetterOrDigit() || converted == ' ') {
                sb.append(converted.uppercaseChar())
            }
        }

        // 3. Collapse multiple spaces
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Compact string with all whitespace removed.
     */
    fun toCompact(input: String): String {
        return normalize(input).replace(" ", "")
    }
}
