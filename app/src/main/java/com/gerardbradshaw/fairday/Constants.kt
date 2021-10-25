package com.gerardbradshaw.fairday

import com.gerardbradshaw.fairday.BuildConfig
import java.util.concurrent.TimeUnit

object Constants {
  private const val PACKAGE_NAME = "com.gerardbradshaw.wheatherweather"

  const val API_KEY_MAPS = BuildConfig.GOOGLE_MAPS_API_KEY

  const val EXTRA_IS_TEST = "$PACKAGE_NAME.EXTRA_IS_TEST"
  const val EXTRA_LOCATION_DATA = "$PACKAGE_NAME.EXTRA_LOCATION_DATA"

  const val KEY_RESULT_DATA = "$PACKAGE_NAME.KEY_RESULT_DATA"

  const val RECEIVER = "$PACKAGE_NAME.RECEIVER"

  const val URL_OPEN_WEATHER = "https://openweathermap.org"
  const val URL_PLAY_STORE = "https://play.google.com/store/apps/developer?id=Gerard+Bradshaw"

  const val DEFAULT_LOCATION_UPDATE_INTERVAL_MS = 1800000L // 30 minutes
  const val FASTEST_LOCATION_UPDATE_INTERVAL_MS = 300000L // 5 minutes

  const val RESULT_SUCCESS = 0
  const val RESULT_FAILURE = 1
  const val RESULT_ERROR = 2
}