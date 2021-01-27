package com.gerardbradshaw.whetherweather.util

import com.gerardbradshaw.whetherweather.BuildConfig

object Constants {
  private const val PACKAGE_NAME = "com.gerardbradshaw.wheatherweather"

  const val API_KEY_MAPS = BuildConfig.GOOGLE_MAPS_API_KEY

  const val EXTRA_LOCATION_DATA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"

  const val KEY_IS_FIRST_LAUNCH = "pref_location_updates"
  const val KEY_RESULT_DATA = "$PACKAGE_NAME.RESULT_DATA_KEY"

  const val RECEIVER = "$PACKAGE_NAME.RECEIVER"

  const val RESULT_SUCCESS = 0
  const val RESULT_FAILURE = 1
}