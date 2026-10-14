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
}
