package com.gerardbradshaw.fairday

import android.content.Context

object SharedPrefManager {
  fun putString(context: Context, key: String, value: String) {
    context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0).edit().apply {
      this.putString(key, value)
      apply()
    }
  }

  fun putFloat(context: Context, key: String, value: Float) {
    context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0).edit().apply {
      this.putFloat(key, value)
      apply()
    }
  }

  fun putLong(context: Context, key: String, value: Long) {
    context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0).edit().apply {
      this.putLong(key, value)
      apply()
    }
  }
  fun putBoolean(context: Context, key: String, value: Boolean) {
    context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0).edit().apply {
      this.putBoolean(key, value)
      apply()
    }
  }

  fun getString(context: Context, key: String, defaultValue: String? = null): String? {
    val prefs = context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    return prefs.getString(key, null)
  }

  fun getFloat(context: Context, key: String, defaultValue: Float): Float {
    val prefs = context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    return prefs.getFloat(key, defaultValue)
  }

  fun getLong(context: Context, key: String, defaultValue: Long): Long {
    val prefs = context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    return prefs.getLong(key, defaultValue)
  }

  fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
    val prefs = context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    return prefs.getBoolean(key, defaultValue)
  }

  fun remove(context: Context, key: String) {
    context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0).edit().apply {
      this.remove(key)
      apply()
    }
  }

  const val PREFS_FILE_KEY = "com.gerardbradshaw.fairday.PREFS_FILE_KEY"
}