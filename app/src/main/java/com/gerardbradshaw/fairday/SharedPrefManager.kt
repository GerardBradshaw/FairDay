package com.gerardbradshaw.fairday

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object SharedPrefManager {
  fun getDefaultSharedPrefs(context: Context): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(context)
  }

  fun putString(context: Context, key: String, value: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
      this.putString(key, value)
      apply()
    }
  }

  fun putFloat(context: Context, key: String, value: Float) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
      this.putFloat(key, value)
      apply()
    }
  }

  fun putLong(context: Context, key: String, value: Long) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
      this.putLong(key, value)
      apply()
    }
  }
  fun putBoolean(context: Context, key: String, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
      this.putBoolean(key, value)
      apply()
    }
  }

  fun getString(context: Context, key: String, defaultValue: String? = null): String? {
    return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
  }

  fun getFloat(context: Context, key: String, defaultValue: Float): Float {
    return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defaultValue)
  }

  fun getLong(context: Context, key: String, defaultValue: Long): Long {
    return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultValue)
  }

  fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue)
  }

  fun remove(context: Context, key: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
      this.remove(key)
      apply()
    }
  }

  /**
   * Clears SharedPreferences. WARNING: All of it!
   */
  fun purge(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context)
      .edit()
      .clear()
      .apply()
  }

  private const val PACKAGE_NAME = "com.gerardbradshaw.fairday"
  const val PREF_KEY_GPS_REQUESTED = "${PACKAGE_NAME}.KEY_GPS_ENABLED"
}