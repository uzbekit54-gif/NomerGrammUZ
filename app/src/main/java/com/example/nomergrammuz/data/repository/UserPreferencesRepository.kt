package com.example.nomergrammuz.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.nomergrammuz.domain.model.SavedPlateItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val itemType = object : TypeToken<List<SavedPlateItem>>() {}.type

    fun getFavorites(): List<SavedPlateItem> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<SavedPlateItem>>(json, itemType) }.getOrDefault(emptyList())
    }

    fun isFavorite(plate: String): Boolean {
        val clean = plate.trim().uppercase()
        return getFavorites().any { it.plate.trim().uppercase() == clean }
    }

    fun toggleFavorite(item: SavedPlateItem): Boolean {
        val list = getFavorites().toMutableList()
        val clean = item.plate.trim().uppercase()
        val index = list.indexOfFirst { it.plate.trim().uppercase() == clean }

        val isNowFavorite = if (index >= 0) {
            list.removeAt(index)
            false
        } else {
            list.add(0, item.copy(timestamp = System.currentTimeMillis()))
            true
        }

        prefs.edit().putString(KEY_FAVORITES, gson.toJson(list)).apply()
        return isNowFavorite
    }

    fun removeFavorite(plate: String) {
        val list = getFavorites().toMutableList()
        val clean = plate.trim().uppercase()
        list.removeAll { it.plate.trim().uppercase() == clean }
        prefs.edit().putString(KEY_FAVORITES, gson.toJson(list)).apply()
    }

    fun getHistory(): List<SavedPlateItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<SavedPlateItem>>(json, itemType) }.getOrDefault(emptyList())
    }

    fun addToHistory(item: SavedPlateItem) {
        if (item.plate.isBlank()) return
        val list = getHistory().toMutableList()
        val clean = item.plate.trim().uppercase()

        // Remove existing duplicate
        list.removeAll { it.plate.trim().uppercase() == clean }

        // Insert at beginning
        list.add(0, item.copy(timestamp = System.currentTimeMillis()))

        // Cap history at 30 items
        if (list.size > MAX_HISTORY_ITEMS) {
            list.subList(MAX_HISTORY_ITEMS, list.size).clear()
        }

        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    fun removeHistory(plate: String) {
        val list = getHistory().toMutableList()
        val clean = plate.trim().uppercase()
        list.removeAll { it.plate.trim().uppercase() == clean }
        prefs.edit().putString(KEY_HISTORY, gson.toJson(list)).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    companion object {
        private const val PREFS_NAME = "nomergramm_user_prefs"
        private const val KEY_FAVORITES = "pref_favorites_json"
        private const val KEY_HISTORY = "pref_history_json"
        private const val MAX_HISTORY_ITEMS = 30
    }
}
