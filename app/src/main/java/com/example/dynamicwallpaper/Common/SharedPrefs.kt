package com.example.dynamicwallpaper.Common

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
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

    fun saveInt(value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(INDEX, value)
        editor.apply()
    }

    fun getInt(defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(INDEX, defaultValue)
    }

    fun saveWallpaperSetType(value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(WALLPAPER_SET_TYPE, value)
        editor.apply()
    }

    fun getWallpaperSetType(defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(WALLPAPER_SET_TYPE, defaultValue)
    }

    private fun saveListToPreferences(list: List<String>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(RECENT_SEARCH_LIST, json)
        editor.apply()
    }

    fun getListFromPreferences(): MutableList<String> {
        val gson = Gson()
        val json = sharedPreferences.getString(RECENT_SEARCH_LIST, null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun addItemToPreferences(item: String) {
        val list = getListFromPreferences()
        val trimmedItem = item.trim()
        if (list.contains(trimmedItem)) list.remove(item)
        list.add(0, trimmedItem)
        saveListToPreferences(list)
    }

    fun removeItemFromPreferences(item: String) {
        val list = getListFromPreferences()
        list.remove(item)
        saveListToPreferences(list)
    }

    fun clearListFromPreferences() {
        val list = getListFromPreferences()
        list.clear()
        saveListToPreferences(list)
    }

    // Save the selected theme mode
    fun saveThemePreference(themeMode: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, themeMode).apply()
    }

    fun getThemePreference(): Int {
        return sharedPreferences.getInt(KEY_THEME_MODE, THEME_SYSTEM_DEFAULT)
    }
}
