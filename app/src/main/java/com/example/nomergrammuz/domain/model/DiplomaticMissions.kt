package com.example.nomergrammuz.domain.model

data class DiplomaticCountryMission(
    val countryCode: Int?,
    val codeDisplay: String,
    val flagEmoji: String,
    val countryNameRu: String,
    val countryNameUz: String,
    val orgNameRu: String,
    val orgNameUz: String,
    val cmdPlate: String?,
    val dPlate: String?,
    val tPlate: String? = null,
    val xPlate: String? = null,
    val unPlate: String? = null,
    val cmdRule: PlateRule?,
    val dRule: PlateRule?,
    val primaryRule: PlateRule
)

object DiplomaticFlags {
    // Official country codes under Decree No. 131 with authentic Unicode country flags
    private val codeToFlag = mapOf(
        1 to "🇹🇷",  // Турция / Turkiya
        2 to "🇨🇳",  // Китай (КНР) / Xitoy (XXR)
        3 to "🇵🇰",  // Пакистан / Pokiston
        4 to "🇩🇪",  // Германия (ФРГ) / Germaniya
        5 to "🇮🇳",  // Индия / Hindiston
        6 to "🇫🇷",  // Франция / Fransiya
        7 to "🇷🇺",  // Россия / Rossiya Federatsiyasi
        8 to "🇮🇷",  // Иран / Eron
        9 to "🇮🇹",  // Италия / Italiya
        10 to "🇺🇸", // США / AQSh
        11 to "🇮🇱", // Израиль / Isroil
        12 to "🇯🇴", // Иордания / Iordaniya
        13 to "🇪🇬", // Египет / Misr
        14 to "🇦🇫", // Афганистан / Afg'oniston
        15 to "🇯🇵", // Япония / Yaponiya
        16 to "🇬🇧", // Великобритания / Buyuk Britaniya
        17 to "🇰🇷", // Республика Корея / Janubiy Koreya
        18 to "🇨🇭", // Швейцария / Shveytsariya
        19 to "🇮🇩", // Индонезия / Indoneziya
        20 to "🇵🇱", // Польша / Polsha
        21 to "🇲🇾", // Малайзия / Malayziya
        22 to "🇧🇩", // Бангладеш / Bangladesh
        23 to "🇰🇼", // Кувейт / Quvayt
        24 to "🇴🇲", // Оман / Ummon
        25 to "🇸🇦", // Саудовская Аравия / Saudiya Arabistoni
        26 to "🇻🇳", // Вьетнам / Vyetnam
        27 to "🇬🇪", // Грузия / Gruziya
        28 to "🇨🇿", // Чехия / Chexiya
        29 to "🇧🇬", // Болгария / Bolgariya
        30 to "🇰🇿", // Казахстан / Qozog'iston
        31 to "🇺🇦", // Украина / Ukraina
        32 to "🇰🇬", // Кыргызстан / Qirg'iziston
        33 to "🇦🇿", // Азербайджан / Ozarbayjon
        34 to "🇲🇩", // Молдова / Moldova
        35 to "🇷🇴", // Румыния / Ruminiya
        36 to "🇧🇾", // Беларусь / Belarus
        37 to "🇹🇯", // Таджикистан / Tojikiston
        38 to "🇹🇲", // Туркменистан / Turkmaniston
        39 to "🇱🇻", // Латвия / Latviya
        40 to "🇸🇰", // Словакия / Slovakiya
        41 to "🇪🇺", // Делегация ЕС / Yevropa Ittifoqi
        42 to "🇻🇦", // Ватикан / Vatikan
        43 to "🇭🇺", // Венгрия / Vengriya
        44 to "🇪🇸", // Испания / Ispaniya
        45 to "🇶🇦", // Катар / Qatar
        46 to "🇲🇳", // Монголия / Mongoliya
        47 to "🇷🇸", // Сербия / Serbiya
        60 to "🇹🇲", // Туркменистан / Turkmaniston
        64 to "🇦🇪"  // ОАЭ / BAA
    )

    fun getFlag(countryCode: Int?): String {
        return if (countryCode == null) "🇺🇳" else codeToFlag[countryCode] ?: "🌐"
    }
}

fun buildDiplomaticCountryMissions(rules: List<PlateRule>): List<DiplomaticCountryMission> {
    val dipRules = rules.filter { it.category == PlateCategory.DIPLOMATIC && it.series !in listOf("M", "H") }
    val groupedByCountry = dipRules.filter { it.countryCode != null }.groupBy { it.countryCode!! }

    val countryMissions = groupedByCountry.map { (code, countryRules) ->
        val cmdRule = countryRules.firstOrNull { it.series == "CMD" || it.subType == "EMBASSY_HEAD" }
        val dRule = countryRules.firstOrNull { it.series == "D" || it.subType == "DIPLOMAT" }
        val refRule = cmdRule ?: dRule ?: countryRules.first()

        val countryNameRu = refRule.countryNameRu ?: "Страна $code"
        val countryNameUz = refRule.countryNameUz ?: refRule.countryNameRu ?: "Davlat $code"
        
        // Clean organization title from suffix
        val orgRu = refRule.orgNameRu
            .replace(" - Автомобиль.*".toRegex(), "")
            .replace(" - Посол.*".toRegex(), "")
            .replace(" (Посол)".toRegex(), "")
            .replace(" (Дипломат)".toRegex(), "")
        val orgUz = refRule.orgNameUz
            .replace(" - .*".toRegex(), "")
            .replace(" (Elchi)".toRegex(), "")
            .replace(" (Diplomat)".toRegex(), "")

        val codeStr2 = if (code < 10) "0$code" else "$code"
        val codeStr3 = if (code < 10) "00$code" else if (code < 100) "0$code" else "$code"

        DiplomaticCountryMission(
            countryCode = code,
            codeDisplay = codeStr2,
            flagEmoji = DiplomaticFlags.getFlag(code),
            countryNameRu = countryNameRu,
            countryNameUz = countryNameUz,
            orgNameRu = orgRu,
            orgNameUz = orgUz,
            cmdPlate = "CMD ${codeStr2}01",
            dPlate = "D $codeStr3 001",
            cmdRule = cmdRule,
            dRule = dRule,
            primaryRule = refRule
        )
    }.sortedBy { it.countryCode ?: 999 }

    // Add UN representation if available
    val unRule = dipRules.firstOrNull { it.series == "UN" }
    val list = countryMissions.toMutableList()
    if (unRule != null) {
        list.add(
            DiplomaticCountryMission(
                countryCode = null,
                codeDisplay = "UN",
                flagEmoji = "🇺🇳",
                countryNameRu = unRule.countryNameRu ?: "Организация Объединённых Наций (ООН)",
                countryNameUz = unRule.countryNameUz ?: "Birlashgan Millatlar Tashkiloti (BMT)",
                orgNameRu = unRule.orgNameRu,
                orgNameUz = unRule.orgNameUz,
                cmdPlate = null,
                dPlate = null,
                unPlate = "UN 0001",
                cmdRule = null,
                dRule = null,
                primaryRule = unRule
            )
        )
    }

    return list
}
