package com.example.nomergrammuz.domain.model

data class PlateRule(
    val id: String,
    val category: PlateCategory,
    val subType: String,
    val series: String?,
    val regionCode: String? = null,
    val countryCode: Int? = null,
    val countryNameRu: String? = null,
    val countryNameUz: String? = null,
    val orgNameRu: String,
    val orgNameUz: String,
    val descriptionRu: String,
    val descriptionUz: String,
    val plateTheme: PlateTheme,
    val regexPattern: String? = null,
    val legalBasis: String = "ПКМ РУз №131 от 27.06.2007"
)

data class ParsedPlateResult(
    val rawInput: String,
    val normalizedInput: String,
    val formattedPlate: String,
    val formatType: PlateFormatType,
    val theme: PlateTheme,
    val primaryRule: PlateRule?,
    val secondaryRegionRule: PlateRule? = null,
    val countryName: String? = null,
    val organizationName: String,
    val description: String,
    val regionTitle: String? = null,
    val isElectricVehicle: Boolean = false,
    val isIdentified: Boolean = true
)
