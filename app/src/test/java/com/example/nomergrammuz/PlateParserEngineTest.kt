package com.example.nomergrammuz

import com.example.nomergrammuz.data.repository.PlateRuleDto
import com.example.nomergrammuz.domain.model.PlateCategory
import com.example.nomergrammuz.domain.model.PlateFormatType
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.domain.normalizer.PlateNormalizer
import com.example.nomergrammuz.domain.parser.PlateParserEngine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStreamReader

class PlateParserEngineTest {

    private lateinit var parserEngine: PlateParserEngine

    @Before
    fun setUp() {
        val file = File("src/main/assets/seed_plates.json")
        val jsonStream = if (file.exists()) {
            file.inputStream()
        } else {
            File("app/src/main/assets/seed_plates.json").inputStream()
        }

        val dtoList: List<PlateRuleDto> = InputStreamReader(jsonStream, Charsets.UTF_8).use { reader ->
            val type = object : TypeToken<List<PlateRuleDto>>() {}.type
            Gson().fromJson(reader, type)
        }

        val rules = dtoList.map { it.toDomain() }
        parserEngine = PlateParserEngine(rules)
    }

    @Test
    fun testNormalizerTranslatesCyrillicLookalikes() {
        // Cyrillic letters: Р, А, В, С, Е, Н, К, М, О, Т, Х
        val cyrillicPaa = "РАА 001"
        val normalized = PlateNormalizer.normalize(cyrillicPaa)
        assertEquals("PAA 001", normalized)

        val cyrillicCmd = "СМД 0701"
        assertEquals("CMD 0701", PlateNormalizer.normalize(cyrillicCmd))

        val cyrillicVsb = "ВСВ 123"
        assertEquals("VSB 123", PlateNormalizer.normalize(cyrillicVsb))

        val civilianCyrillic = "01 777 ХАА"
        assertEquals("01 777 XAA", PlateNormalizer.normalize(civilianCyrillic))
    }

    @Test
    fun testDiplomaticHeadCmdRussia() {
        // Code 07 = Russia
        val result = parserEngine.parse("CMD 0701", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.DIPLOMATIC_HEAD, result.formatType)
        assertEquals(PlateTheme.GREEN, result.theme)
        assertEquals("Российская Федерация", result.countryName)
        assertTrue(result.organizationName.contains("Российская Федерация"))
        assertTrue(result.description.contains("Посол"))
    }

    @Test
    fun testDiplomaticHeadCmdUSA() {
        // Code 10 = USA
        val result = parserEngine.parse("CMD 1001", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.DIPLOMATIC_HEAD, result.formatType)
        assertEquals(PlateTheme.GREEN, result.theme)
        assertEquals("США", result.countryName)
        assertTrue(result.description.contains("Посол"))
    }

    @Test
    fun testDiplomatDStaff() {
        // Code 007 = Russia
        val result = parserEngine.parse("007 D 012", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.DIPLOMAT, result.formatType)
        assertEquals(PlateTheme.GREEN, result.theme)
        assertEquals("Российская Федерация", result.countryName)
    }

    @Test
    fun testUnitedNationsPlate() {
        val result = parserEngine.parse("UN 001", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.UN_AGENCY, result.formatType)
        assertEquals(PlateTheme.BLUE, result.theme)
        assertTrue(result.organizationName.contains("ООН"))
    }

    @Test
    fun testPresidentialAdministrationPaa() {
        val result = parserEngine.parse("PAA 001", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, result.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, result.theme)
        assertEquals(PlateCategory.STATE_AUTHORITY, result.primaryRule?.category)
        assertTrue(result.organizationName.contains("Администрация Президента"))
    }

    @Test
    fun testParliamentPpp() {
        val result = parserEngine.parse("PPP 777", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, result.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, result.theme)
        assertTrue(result.organizationName.contains("Олий Мажлис"))
    }

    @Test
    fun testMinistriesLeadershipApc() {
        val result = parserEngine.parse("APC 100", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, result.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, result.theme)
        assertTrue(result.organizationName.contains("Руководство министерств"))
    }

    @Test
    fun testStateSecurityServiceTss() {
        val result = parserEngine.parse("TSS 001", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, result.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, result.theme)
        assertTrue(result.organizationName.contains("Служба государственной безопасности"))
    }

    @Test
    fun testPoliceMvdVsf() {
        val result = parserEngine.parse("VSF 100", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, result.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, result.theme)
        assertTrue(result.organizationName.contains("внутренних дел"))
    }

    @Test
    fun testGeneralProsecutorZdf() {
        val result = parserEngine.parse("ZDF 050", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, result.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, result.theme)
        assertTrue(result.organizationName.contains("прокуратур"))
    }

    @Test
    fun testRegionalGovernmentDav() {
        val result = parserEngine.parse("01 123 DAV", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_DUTY_REGIONAL, result.formatType)
        assertTrue(result.organizationName.contains("Хокимият"))
        assertEquals("город Ташкент", result.regionTitle)
    }

    @Test
    fun testEmergencyAmbulanceSty() {
        val result = parserEngine.parse("01 123 STY", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.EMERGENCY_SERVICE, result.formatType)
        assertTrue(result.organizationName.contains("Скорая медицинская помощь"))
    }

    @Test
    fun testCivilianIndividualFormat() {
        val result = parserEngine.parse("01 A 123 AA", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.CIVILIAN_INDIVIDUAL, result.formatType)
        assertEquals("город Ташкент", result.regionTitle)
    }

    @Test
    fun testCivilianLegalEntityFormat() {
        val result = parserEngine.parse("30 123 AAA", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.CIVILIAN_LEGAL, result.formatType)
        assertEquals("Самаркандская область", result.regionTitle)
    }

    @Test
    fun testCyrillicInputSeamlessParsing() {
        // User inputs on Russian keyboard layout: "РАА 001"
        val result = parserEngine.parse("РАА 001", isRussian = true)
        assertTrue(result.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, result.formatType)
        assertTrue(result.organizationName.contains("Администрация Президента"))

        // User inputs "СМД 0701" on Russian layout
        val cmdResult = parserEngine.parse("СМД 0701", isRussian = true)
        assertTrue(cmdResult.isIdentified)
        assertEquals(PlateFormatType.DIPLOMATIC_HEAD, cmdResult.formatType)
        assertEquals("Российская Федерация", cmdResult.countryName)
    }

    @Test
    fun testStandaloneSeriesQueriesDecree131() {
        // 1. Standalone "D" (Type V)
        val dResult = parserEngine.parse("D", isRussian = true)
        assertTrue(dResult.isIdentified)
        assertEquals(PlateFormatType.DIPLOMAT, dResult.formatType)
        assertEquals(PlateTheme.GREEN, dResult.theme)
        assertTrue(dResult.organizationName.contains("Дипломатический персонал") || dResult.organizationName.contains("Серия D"))

        // 2. Standalone "T" (Type VII)
        val tResult = parserEngine.parse("T", isRussian = true)
        assertTrue(tResult.isIdentified)
        assertEquals(PlateFormatType.TECHNICAL_STAFF, tResult.formatType)
        assertEquals(PlateTheme.GREEN, tResult.theme)
        assertTrue(tResult.organizationName.contains("Административно-технический") || tResult.organizationName.contains("Серия T"))

        // 3. Standalone "UN" (Type VI)
        val unResult = parserEngine.parse("UN", isRussian = true)
        assertTrue(unResult.isIdentified)
        assertEquals(PlateFormatType.UN_AGENCY, unResult.formatType)
        assertEquals(PlateTheme.BLUE, unResult.theme)
        assertTrue(unResult.organizationName.contains("ООН"))

        // 4. Standalone "CMD" (Type IV)
        val cmdResult = parserEngine.parse("CMD", isRussian = true)
        assertTrue(cmdResult.isIdentified)
        assertEquals(PlateFormatType.DIPLOMATIC_HEAD, cmdResult.formatType)
        assertEquals(PlateTheme.GREEN, cmdResult.theme)
        assertTrue(cmdResult.description.contains("Посол"))

        // 5. Standalone "X" (Type VIII)
        val xResult = parserEngine.parse("X", isRussian = true)
        assertTrue(xResult.isIdentified)
        assertEquals(PlateFormatType.CONSULAR, xResult.formatType)
        assertEquals(PlateTheme.GREEN, xResult.theme)
        assertTrue(xResult.organizationName.contains("Консульские учреждения") || xResult.organizationName.contains("Серия X"))

        // 6. Standalone "M" (Type IX)
        val mResult = parserEngine.parse("M", isRussian = true)
        assertTrue(mResult.isIdentified)
        assertEquals(PlateFormatType.FOREIGN_FIRM, mResult.formatType)
        assertEquals(PlateTheme.GREEN, mResult.theme)
        assertTrue(mResult.organizationName.contains("Иностранные компании") || mResult.organizationName.contains("Серия M"))

        // 7. Standalone "H" (Type X)
        val hResult = parserEngine.parse("H", isRussian = true)
        assertTrue(hResult.isIdentified)
        assertEquals(PlateFormatType.FOREIGN_CITIZEN, hResult.formatType)
        assertEquals(PlateTheme.YELLOW, hResult.theme)
        assertTrue(hResult.organizationName.contains("Иностранные граждане") || hResult.organizationName.contains("Серия H"))

        // 8. Standalone "DAV" (Type I-1)
        val davResult = parserEngine.parse("DAV", isRussian = true)
        assertTrue(davResult.isIdentified)
        assertEquals(PlateFormatType.GOV_DUTY_REGIONAL, davResult.formatType)
        assertTrue(davResult.organizationName.contains("Хокимият") || davResult.organizationName.contains("Серия DAV"))

        // 9. Standalone "STY" (Type I-2)
        val styResult = parserEngine.parse("STY", isRussian = true)
        assertTrue(styResult.isIdentified)
        assertEquals(PlateFormatType.EMERGENCY_SERVICE, styResult.formatType)
        assertTrue(styResult.organizationName.contains("Скорая медицинская помощь"))

        // 10. Standalone "PAA" (Type II)
        val paaResult = parserEngine.parse("PAA", isRussian = true)
        assertTrue(paaResult.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, paaResult.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, paaResult.theme)
        assertTrue(paaResult.organizationName.contains("Администрация Президента"))
    }

    @Test
    fun testPrefixQueries() {
        // User inputs "серия D"
        val dPref = parserEngine.parse("серия D", isRussian = true)
        assertTrue(dPref.isIdentified)
        assertEquals(PlateFormatType.DIPLOMAT, dPref.formatType)

        // User inputs "серия UN"
        val unPref = parserEngine.parse("серия UN", isRussian = true)
        assertTrue(unPref.isIdentified)
        assertEquals(PlateFormatType.UN_AGENCY, unPref.formatType)

        // User inputs "тип II"
        val paaPref = parserEngine.parse("серия PAA", isRussian = true)
        assertTrue(paaPref.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, paaPref.formatType)
    }

    @Test
    fun testDecreeFormatSixDigits() {
        // Decree Annex 2 format: D 007012
        val dResult = parserEngine.parse("D 007012", isRussian = true)
        assertTrue(dResult.isIdentified)
        assertEquals(PlateFormatType.DIPLOMAT, dResult.formatType)
        assertEquals("Российская Федерация", dResult.countryName)

        // Decree Annex 2 format: T 007012
        val tResult = parserEngine.parse("T 007012", isRussian = true)
        assertTrue(tResult.isIdentified)
        assertEquals(PlateFormatType.TECHNICAL_STAFF, tResult.formatType)
        assertEquals("Российская Федерация", tResult.countryName)

        // Decree Annex 2 format: 01 M 123456
        val mResult = parserEngine.parse("01 M 123456", isRussian = true)
        assertTrue(mResult.isIdentified)
        assertEquals(PlateFormatType.FOREIGN_FIRM, mResult.formatType)
        assertEquals(PlateTheme.GREEN, mResult.theme)
        assertEquals("город Ташкент", mResult.regionTitle)

        // Decree Annex 2 format: 01 H 123456
        val hResult = parserEngine.parse("01 H 123456", isRussian = true)
        assertTrue(hResult.isIdentified)
        assertEquals(PlateFormatType.FOREIGN_CITIZEN, hResult.formatType)
        assertEquals(PlateTheme.YELLOW, hResult.theme)
        assertEquals("город Ташкент", hResult.regionTitle)
    }

    @Test
    fun testStandaloneRegionCode() {
        val reg01 = parserEngine.parse("01", isRussian = true)
        assertTrue(reg01.isIdentified)
        assertEquals("город Ташкент", reg01.regionTitle)

        val reg95 = parserEngine.parse("95", isRussian = true)
        assertTrue(reg95.isIdentified)
        assertEquals("Республика Каракалпакстан", reg95.regionTitle)

        // Single digit region "1" padded to "01"
        val reg1 = parserEngine.parse("1", isRussian = true)
        assertTrue(reg1.isIdentified)
        assertEquals("город Ташкент", reg1.regionTitle)
    }

    @Test
    fun testSemanticSearchByRegionCountryAgency() {
        // Region name
        val samarkand = parserEngine.parse("Самарканд", isRussian = true)
        assertTrue(samarkand.isIdentified)
        assertEquals("Самаркандская область", samarkand.regionTitle)

        // Country name
        val germany = parserEngine.parse("Германия", isRussian = true)
        assertTrue(germany.isIdentified)
        assertTrue(germany.countryName?.contains("Германия") == true)
        assertEquals(PlateFormatType.DIPLOMATIC_HEAD, germany.formatType)

        // Agency name
        val president = parserEngine.parse("Президент", isRussian = true)
        assertTrue(president.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, president.formatType)
        assertTrue(president.organizationName.contains("Администрация Президента"))

        val senate = parserEngine.parse("Сенат", isRussian = true)
        assertTrue(senate.isIdentified)
        assertTrue(senate.organizationName.contains("Олий Мажлис"))
    }

    @Test
    fun testMilitaryDefensePlates() {
        // Operational military vehicle plate (black background, white symbols)
        val milResult = parserEngine.parse("01 1234 MV", isRussian = true)
        assertTrue(milResult.isIdentified)
        assertEquals(PlateFormatType.MILITARY, milResult.formatType)
        assertEquals(PlateTheme.BLACK, milResult.theme)
        assertEquals("01 1234 MV", milResult.formattedPlate)
        assertTrue(milResult.organizationName.contains("Министерство обороны"))

        // Military plate without region prefix: "1234 MV"
        val milWithoutReg = parserEngine.parse("1234 MV", isRussian = true)
        assertTrue(milWithoutReg.isIdentified)
        assertEquals(PlateFormatType.MILITARY, milWithoutReg.formatType)
        assertEquals(PlateTheme.BLACK, milWithoutReg.theme)

        // Ministry central staff (white plate with flag, Type II): "MV 001"
        val mvGov = parserEngine.parse("MV 001", isRussian = true)
        assertTrue(mvGov.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, mvGov.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, mvGov.theme)

        // SGB Special unit: "USB 001"
        val usbGov = parserEngine.parse("USB 001", isRussian = true)
        assertTrue(usbGov.isIdentified)
        assertEquals(PlateFormatType.GOV_SPECIAL, usbGov.formatType)
        assertEquals(PlateTheme.WHITE_GOV_FLAG, usbGov.theme)

        // Semantic search for border troops: "погран"
        val borderResult = parserEngine.parse("погран", isRussian = true)
        assertTrue(borderResult.isIdentified)
        assertTrue(borderResult.organizationName.contains("Пограничные войска"))
    }
}

