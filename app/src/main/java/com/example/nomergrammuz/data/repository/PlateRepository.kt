package com.example.nomergrammuz.data.repository

import android.content.Context
import com.example.nomergrammuz.domain.model.PlateCategory
import com.example.nomergrammuz.domain.model.PlateRule
import com.example.nomergrammuz.domain.model.PlateTheme
import com.example.nomergrammuz.domain.parser.PlateParserEngine
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

data class PlateRuleDto(
    @SerializedName("id") val id: String,
    @SerializedName("category") val category: String,
    @SerializedName("sub_type") val subType: String,
    @SerializedName("series") val series: String?,
    @SerializedName("region_code") val regionCode: String? = null,
    @SerializedName("country_code") val countryCode: Int? = null,
    @SerializedName("country_name_ru") val countryNameRu: String? = null,
    @SerializedName("country_name_uz") val countryNameUz: String? = null,
    @SerializedName("org_name_ru") val orgNameRu: String,
    @SerializedName("org_name_uz") val orgNameUz: String,
    @SerializedName("description_ru") val descriptionRu: String,
    @SerializedName("description_uz") val descriptionUz: String,
    @SerializedName("plate_theme") val plateTheme: String,
    @SerializedName("regex_pattern") val regexPattern: String? = null,
    @SerializedName("legal_basis") val legalBasis: String? = null
) {
    fun toDomain(): PlateRule {
        return PlateRule(
            id = id,
            category = runCatching { PlateCategory.valueOf(category) }.getOrDefault(PlateCategory.UNKNOWN),
            subType = subType,
            series = series,
            regionCode = regionCode,
            countryCode = countryCode,
            countryNameRu = countryNameRu,
            countryNameUz = countryNameUz,
            orgNameRu = orgNameRu,
            orgNameUz = orgNameUz,
            descriptionRu = descriptionRu,
            descriptionUz = descriptionUz,
            plateTheme = runCatching { PlateTheme.valueOf(plateTheme) }.getOrDefault(PlateTheme.WHITE),
            regexPattern = regexPattern,
            legalBasis = legalBasis ?: "ПКМ РУз №131 от 27.06.2007"
        )
    }
}

class PlateRepository(
    private val context: Context? = null,
    seedRules: List<PlateRule>? = null
) {
    private val gson = Gson()
    private val rulesLock = Any()
    private var cachedRules: List<PlateRule> = seedRules ?: emptyList()
    private var cachedEngine: PlateParserEngine = PlateParserEngine(cachedRules)

    init {
        if (cachedRules.isEmpty() && context != null) {
            loadFromAssets()
        }
    }

    private fun loadFromAssets() {
        try {
            context?.assets?.open("seed_plates.json")?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val type = object : TypeToken<List<PlateRuleDto>>() {}.type
                    val dtoList: List<PlateRuleDto> = gson.fromJson(reader, type)
                    synchronized(rulesLock) {
                        cachedRules = dtoList.map { it.toDomain() }
                        cachedEngine = PlateParserEngine(cachedRules)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAllRules(): List<PlateRule> = synchronized(rulesLock) { cachedRules }

    fun getParserEngine(): PlateParserEngine = synchronized(rulesLock) { cachedEngine }

    fun updateRules(newRules: List<PlateRule>) {
        synchronized(rulesLock) {
            cachedRules = newRules
            cachedEngine = PlateParserEngine(cachedRules)
        }
    }

    fun parseRemoteJson(jsonString: String): List<PlateRule> {
        val type = object : TypeToken<List<PlateRuleDto>>() {}.type
        val dtoList: List<PlateRuleDto> = gson.fromJson(jsonString, type)
        return dtoList.map { it.toDomain() }
    }
}
