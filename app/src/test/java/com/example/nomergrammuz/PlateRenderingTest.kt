package com.example.nomergrammuz

import com.example.nomergrammuz.data.repository.PlateRuleDto
import com.example.nomergrammuz.domain.model.PlateFormatType
import com.example.nomergrammuz.domain.parser.PlateParserEngine
import com.example.nomergrammuz.ui.components.resolveMilitaryPlateParts
import com.example.nomergrammuz.ui.components.resolveRegionalPlateParts
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.InputStreamReader

class PlateRenderingTest {

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
    fun testEmptyInputShowsPlaceholderWithoutMirroring() {
        val result = parserEngine.parse("", isRussian = true)
        val parts = resolveRegionalPlateParts(result, "A 001 AA")

        assertEquals("01", parts.region)
        assertEquals("A 001 AA", parts.number)
        assertTrue(parts.isRegionPlaceholder)
        assertTrue(parts.isNumberPlaceholder)
    }

    @Test
    fun testSingleDigitInputDoesNotMirrorIntoNumberBox() {
        // User inputs "0"
        val res0 = parserEngine.parse("0", isRussian = true)
        val parts0 = resolveRegionalPlateParts(res0, "A 001 AA")
        assertEquals("0", parts0.region)
        assertEquals("A 001 AA", parts0.number)
        assertFalse(parts0.isRegionPlaceholder)
        assertTrue(parts0.isNumberPlaceholder)
        assertNotEquals("Number part must not mirror region digit", parts0.region, parts0.number)

        // User inputs "7"
        val res7 = parserEngine.parse("7", isRussian = true)
        val parts7 = resolveRegionalPlateParts(res7, "A 001 AA")
        assertEquals("7", parts7.region)
        assertEquals("A 001 AA", parts7.number)
        assertFalse(parts7.isRegionPlaceholder)
        assertTrue(parts7.isNumberPlaceholder)
        assertNotEquals("Number part must not mirror region digit", parts7.region, parts7.number)
    }

    @Test
    fun testValidTwoDigitRegionDoesNotMirrorIntoNumberBox() {
        // User inputs "01" (Tashkent)
        val res01 = parserEngine.parse("01", isRussian = true)
        val parts01 = resolveRegionalPlateParts(res01, "A 001 AA")
        assertEquals("01", parts01.region)
        assertEquals("A 001 AA", parts01.number)
        assertFalse(parts01.isRegionPlaceholder)
        assertTrue(parts01.isNumberPlaceholder)
        assertNotEquals("Number part must not mirror 01", "01", parts01.number)

        // User inputs "10" (Tashkent Region)
        val res10 = parserEngine.parse("10", isRussian = true)
        val parts10 = resolveRegionalPlateParts(res10, "A 001 AA")
        assertEquals("10", parts10.region)
        assertEquals("A 001 AA", parts10.number)
        assertFalse(parts10.isRegionPlaceholder)
        assertTrue(parts10.isNumberPlaceholder)
        assertNotEquals("Number part must not mirror 10", "10", parts10.number)
    }

    @Test
    fun testNonRegionDigitsDoNotCorruptRegionBox() {
        // "77" is not a region code in Uzbekistan (regions are 01, 10, 20, 25, 30, 40, 50, 60, 70, 75, 80, 85, 90, 95)
        val res77 = parserEngine.parse("77", isRussian = true)
        val parts77 = resolveRegionalPlateParts(res77, "A 001 AA")
        assertEquals("01", parts77.region)
        assertTrue(parts77.isRegionPlaceholder)
        assertEquals("77", parts77.number)
        assertFalse(parts77.isNumberPlaceholder)

        // "777" must not be split into 77 | 7
        val res777 = parserEngine.parse("777", isRussian = true)
        val parts777 = resolveRegionalPlateParts(res777, "A 001 AA")
        assertEquals("01", parts777.region)
        assertTrue(parts777.isRegionPlaceholder)
        assertEquals("777", parts777.number)
        assertFalse(parts777.isNumberPlaceholder)
    }

    @Test
    fun testLetterInputDoesNotCorruptRegionBox() {
        val resA = parserEngine.parse("A", isRussian = true)
        val partsA = resolveRegionalPlateParts(resA, "A 001 AA")
        assertEquals("01", partsA.region)
        assertTrue(partsA.isRegionPlaceholder)
        assertEquals("A", partsA.number)
        assertFalse(partsA.isNumberPlaceholder)
    }

    @Test
    fun testFullPlatesCorrectlySplitIntoRegionAndNumber() {
        // Individual standard: 01 777 AAA
        val resIndiv = parserEngine.parse("01 777 AAA", isRussian = true)
        val partsIndiv = resolveRegionalPlateParts(resIndiv, "A 001 AA")
        assertEquals("01", partsIndiv.region)
        assertEquals("777 AAA", partsIndiv.number)
        assertFalse(partsIndiv.isRegionPlaceholder)
        assertFalse(partsIndiv.isNumberPlaceholder)

        // Individual type III: 01 A 777 AA
        val resType3 = parserEngine.parse("01 A 777 AA", isRussian = true)
        val partsType3 = resolveRegionalPlateParts(resType3, "A 001 AA")
        assertEquals("01", partsType3.region)
        assertEquals("A 777 AA", partsType3.number)
        assertFalse(partsType3.isRegionPlaceholder)
        assertFalse(partsType3.isNumberPlaceholder)

        // Samarkand Legal Entity: 30 123 AAA
        val resSam = parserEngine.parse("30 123 AAA", isRussian = true)
        val partsSam = resolveRegionalPlateParts(resSam, "A 001 AA")
        assertEquals("30", partsSam.region)
        assertEquals("123 AAA", partsSam.number)
        assertFalse(partsSam.isRegionPlaceholder)
        assertFalse(partsSam.isNumberPlaceholder)

        // Regional duty: 10 001 DAV
        val resDav = parserEngine.parse("10 001 DAV", isRussian = true)
        val partsDav = resolveRegionalPlateParts(resDav, "A 001 AA")
        assertEquals("10", partsDav.region)
        assertEquals("001 DAV", partsDav.number)
        assertFalse(partsDav.isRegionPlaceholder)
        assertFalse(partsDav.isNumberPlaceholder)
    }

    @Test
    fun testMilitaryPlateParts() {
        // With region
        val milWithReg = parserEngine.parse("01 1234 MV", isRussian = true)
        val partsWithReg = resolveMilitaryPlateParts(milWithReg)
        assertEquals("01", partsWithReg.region)
        assertEquals("1234 MV", partsWithReg.body)

        // Without region
        val milWithoutReg = parserEngine.parse("1234 MV", isRussian = true)
        val partsWithoutReg = resolveMilitaryPlateParts(milWithoutReg)
        assertEquals("01", partsWithoutReg.region)
        assertEquals("1234 MV", partsWithoutReg.body)
    }

    @Test
    fun testDiplomaticPlatesDoNotMirrorD007() {
        // User types "D 007" - must display "D 007" on plate, NEVER "007 D"
        val resD007 = parserEngine.parse("D 007", isRussian = true)
        assertEquals("D 007", resD007.formattedPlate)
        assertNotEquals("007 D", resD007.formattedPlate)
        assertEquals("Российская Федерация", resD007.countryName)

        // User types "D 007 012" - must format as "D 007 012"
        val resD007012 = parserEngine.parse("D 007 012", isRussian = true)
        assertEquals("D 007 012", resD007012.formattedPlate)
        assertNotEquals("007 D 012", resD007012.formattedPlate)

        // User types "007 D 012" - formatted with D prefix per Decree 131
        val res007D = parserEngine.parse("007 D 012", isRussian = true)
        assertEquals("D 007 012", res007D.formattedPlate)

        // Technical staff "T 007"
        val resT007 = parserEngine.parse("T 007", isRussian = true)
        assertEquals("T 007", resT007.formattedPlate)
        assertNotEquals("007 T", resT007.formattedPlate)

        // Consular "X 007"
        val resX007 = parserEngine.parse("X 007", isRussian = true)
        assertEquals("X 007", resX007.formattedPlate)
        assertNotEquals("007 X", resX007.formattedPlate)
    }
}
