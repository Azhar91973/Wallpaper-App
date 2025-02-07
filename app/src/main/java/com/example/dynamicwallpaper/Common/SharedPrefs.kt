package com.example.dynamicwallpaper.Common

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class SharedPrefs(context: Context) {

    companion object {
        const val RECENT_SEARCH_LIST = "RecentSearch"
        const val INDEX = "IndexValue"
        const val WALLPAPER_SET_TYPE = "SetType"
        private const val KEY_THEME_MODE = "theme_mode" // Key for theme preference
        const val THEME_SYSTEM_DEFAULT = -1
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        private const val SHARED_PREFERENCE = "MySharedPrefs"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<String>>() {}.type

    // Saves an integer value under the INDEX key.
    fun saveInt(value: Int) {
        sharedPreferences.edit().putInt(INDEX, value).apply()
    }

    // Retrieves the integer stored at INDEX, defaulting to 0.
    fun getInt(defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(INDEX, defaultValue)
    }

    // Saves the wallpaper set type.
    fun saveWallpaperSetType(value: Int) {
        sharedPreferences.edit().putInt(WALLPAPER_SET_TYPE, value).apply()
    }

    // Retrieves the wallpaper set type.
    fun getWallpaperSetType(defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(WALLPAPER_SET_TYPE, defaultValue)
    }

    // Serializes and saves a list of strings.
    private fun saveListToPreferences(list: List<String>) {
        val json = gson.toJson(list)
        sharedPreferences.edit().putString(RECENT_SEARCH_LIST, json).apply()
    }

    // Retrieves and deserializes the list of strings.
    fun getListFromPreferences(): MutableList<String> {
        val json = sharedPreferences.getString(RECENT_SEARCH_LIST, null)
        return try {
            if (json.isNullOrEmpty()) mutableListOf() else gson.fromJson(json, listType)
        } catch (e: JsonSyntaxException) {
            mutableListOf()
        }
    }

    // Synchronized method to add a trimmed item to the list.
    @Synchronized
    fun addItemToPreferences(item: String) {
        val list = getListFromPreferences()
        val trimmedItem = item.trim()
        if (list.contains(trimmedItem)) {
            list.remove(trimmedItem)
        }
        list.add(0, trimmedItem)
        saveListToPreferences(list)
    }

    // Synchronized method to remove an item from the list.
    @Synchronized
    fun removeItemFromPreferences(item: String) {
        val list = getListFromPreferences()
        list.remove(item)
        saveListToPreferences(list)
    }

    // Synchronized method to clear the list.
    @Synchronized
    fun clearListFromPreferences() {
        saveListToPreferences(emptyList())
    }

    // Saves the theme mode preference.
    fun saveThemePreference(themeMode: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, themeMode).apply()
    }

    // Retrieves the theme mode, defaulting to THEME_SYSTEM_DEFAULT.
    fun getThemePreference(): Int {
        return sharedPreferences.getInt(KEY_THEME_MODE, THEME_SYSTEM_DEFAULT)
    }
}