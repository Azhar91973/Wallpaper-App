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
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE)

    fun saveInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun saveWallpaperSetType(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getWallpaperSetType(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    private fun saveListToPreferences(key: String, list: List<String>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    fun getListFromPreferences(key: String): MutableList<String> {
        val gson = Gson()
        val json = sharedPreferences.getString(key, null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun addItemToPreferences(key: String, item: String) {
        val list = getListFromPreferences(key)
        val trimmedItem = item.trim()
        if (!list.contains(trimmedItem)) list.add(trimmedItem)
        saveListToPreferences(key, list)
    }

    fun removeItemFromPreferences(key: String, item: String) {
        val list = getListFromPreferences(key)
        list.remove(item)
        saveListToPreferences(key, list)
    }

    fun clearListFromPreferences(key: String) {
        val list = getListFromPreferences(key)
        list.clear()
        saveListToPreferences(key, list)
    }
}
