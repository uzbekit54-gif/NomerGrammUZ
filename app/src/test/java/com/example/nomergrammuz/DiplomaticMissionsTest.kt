package com.example.nomergrammuz

import com.example.nomergrammuz.domain.model.DiplomaticFlags
import com.example.nomergrammuz.domain.model.PlateCategory
import com.example.nomergrammuz.domain.model.PlateRule
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.domain.model.buildDiplomaticCountryMissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiplomaticMissionsTest {

    @Test
    fun testCountryFlagsMapping() {
        assertEquals("🇹🇷", DiplomaticFlags.getFlag(1))   // Turkey
        assertEquals("🇨🇳", DiplomaticFlags.getFlag(2))   // China
        assertEquals("🇩🇪", DiplomaticFlags.getFlag(4))   // Germany
        assertEquals("🇫🇷", DiplomaticFlags.getFlag(6))   // France
        assertEquals("🇷🇺", DiplomaticFlags.getFlag(7))   // Russia
        assertEquals("🇺🇸", DiplomaticFlags.getFlag(10))  // USA
        assertEquals("🇬🇧", DiplomaticFlags.getFlag(16))  // UK
        assertEquals("🇰🇿", DiplomaticFlags.getFlag(30))  // Kazakhstan
        assertEquals("🇪🇺", DiplomaticFlags.getFlag(41))  // EU
        assertEquals("🇻🇦", DiplomaticFlags.getFlag(42))  // Vatican
        assertEquals("🇺🇳", DiplomaticFlags.getFlag(null)) // UN
        assertEquals("🌐", DiplomaticFlags.getFlag(999))  // Fallback
    }

    @Test
    fun testBuildDiplomaticCountryMissionsGrouping() {
        val rules = listOf(
            PlateRule(
                id = "dip_cmd_01",
                category = PlateCategory.DIPLOMATIC,
                subType = "EMBASSY_HEAD",
                series = "CMD",
                countryCode = 1,
                countryNameRu = "Турция",
                countryNameUz = "Turkiya",
                orgNameRu = "Посольство: Турция - Автомобиль Посла (CMD)",
                orgNameUz = "Turkiya Elchixonasi - Elchi avtomobili (CMD)",
                descriptionRu = "Глава дипмиссии",
                descriptionUz = "Diplomatik missiya rahbari",
                plateTheme = PlateTheme.GREEN
            ),
            PlateRule(
                id = "dip_d_01",
                category = PlateCategory.DIPLOMATIC,
                subType = "DIPLOMAT",
                series = "D",
                countryCode = 1,
                countryNameRu = "Турция",
                countryNameUz = "Turkiya",
                orgNameRu = "Посольство: Турция - Автомобиль дипломата (D)",
                orgNameUz = "Turkiya Elchixonasi - Diplomat avtomobili (D)",
                descriptionRu = "Дипломатический сотрудник",
                descriptionUz = "Diplomatik xodim",
                plateTheme = PlateTheme.GREEN
            ),
            PlateRule(
                id = "dip_cmd_07",
                category = PlateCategory.DIPLOMATIC,
                subType = "EMBASSY_HEAD",
                series = "CMD",
                countryCode = 7,
                countryNameRu = "Россия",
                countryNameUz = "Rossiya",
                orgNameRu = "Посольство: Россия - Автомобиль Посла (CMD)",
                orgNameUz = "Rossiya Elchixonasi - Elchi avtomobili (CMD)",
                descriptionRu = "Глава дипмиссии",
                descriptionUz = "Diplomatik missiya rahbari",
                plateTheme = PlateTheme.GREEN
            ),
            PlateRule(
                id = "dip_d_07",
                category = PlateCategory.DIPLOMATIC,
                subType = "DIPLOMAT",
                series = "D",
                countryCode = 7,
                countryNameRu = "Россия",
                countryNameUz = "Rossiya",
                orgNameRu = "Посольство: Россия - Автомобиль дипломата (D)",
                orgNameUz = "Rossiya Elchixonasi - Diplomat avtomobili (D)",
                descriptionRu = "Дипломатический сотрудник",
                descriptionUz = "Diplomatik xodim",
                plateTheme = PlateTheme.GREEN
            ),
            PlateRule(
                id = "dip_un",
                category = PlateCategory.DIPLOMATIC,
                subType = "UN",
                series = "UN",
                countryCode = null,
                countryNameRu = "Организация Объединённых Наций (ООН)",
                countryNameUz = "BMT",
                orgNameRu = "Представительство ООН в Узбекистане",
                orgNameUz = "BMT vakolatxonasi",
                descriptionRu = "Представительство ООН",
                descriptionUz = "BMT vakolatxonasi",
                plateTheme = PlateTheme.BLUE
            )
        )

        val missions = buildDiplomaticCountryMissions(rules)
        // Expect 2 countries (Turkey, Russia) + 1 UN = 3 missions total
        assertEquals(3, missions.size)

        // Verify Turkey
        val turkey = missions.find { it.countryCode == 1 }
        assertNotNull(turkey)
        assertEquals("🇹🇷", turkey!!.flagEmoji)
        assertEquals("01", turkey.codeDisplay)
        assertEquals("CMD 0101", turkey.cmdPlate)
        assertEquals("D 001 001", turkey.dPlate)
        assertEquals("Посольство: Турция", turkey.orgNameRu)

        // Verify Russia
        val russia = missions.find { it.countryCode == 7 }
        assertNotNull(russia)
        assertEquals("🇷🇺", russia!!.flagEmoji)
        assertEquals("07", russia.codeDisplay)
        assertEquals("CMD 0701", russia.cmdPlate)
        assertEquals("D 007 001", russia.dPlate)

        // Verify UN
        val un = missions.find { it.codeDisplay == "UN" }
        assertNotNull(un)
        assertEquals("🇺🇳", un!!.flagEmoji)
        assertEquals("UN 0001", un.unPlate)
    }
}
