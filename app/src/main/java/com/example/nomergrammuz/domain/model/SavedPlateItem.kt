package com.example.nomergrammuz.domain.model

data class SavedPlateItem(
    val plate: String,
    val title: String,
    val subtitle: String,
    val formatType: PlateFormatType = PlateFormatType.UNKNOWN,
    val theme: PlateTheme = PlateTheme.WHITE,
    val timestamp: Long = System.currentTimeMillis()
)
