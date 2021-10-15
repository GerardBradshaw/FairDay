package com.gerardbradshaw.fairday.widget

import android.app.*
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.activities.detail.DetailActivity
import com.gerardbradshaw.fairday.activities.detail.utils.ConditionUtil
import com.gerardbradshaw.fairday.activities.detail.utils.WeatherUtil
import com.gerardbradshaw.fairday.room.LocationEntity
import com.gerardbradshaw.weatherview.datamodels.WeatherData
import com.google.android.gms.location.*
import kotlin.math.roundToInt
import java.util.*

import androidx.work.*
import com.gerardbradshaw.fairday.SharedPrefManager
import android.app.PendingIntent

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [FairDayWidgetConfigureActivity]
 */
class FairDayWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (appWidgetId in appWidgetIds) {
      if (isConfigured(context, appWidgetId)) {
        Log.d(TAG, "onUpdate: updating widget $appWidgetId")
        updateAppWidgetContent(context, appWidgetId)
      } else {
        Log.d(TAG, "onUpdate: skipping widget $appWidgetId update for now")
      }
    }
  }

  /**
   * Returns true if the user has confirmed widget configuration, false otherwise.
   */
  private fun isConfigured(context: Context, appWidgetId: Int): Boolean {
    return SharedPrefManager.getBoolean(
      context,
      PREF_KEY_IS_CONFIGURED + appWidgetId,
      false)
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    super.onReceive(context, intent)
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      deleteAllWidgetPrefs(context, appWidgetId)
    }
  }

  class WidgetUpdater(
    private var appWidgetId: Int,
    private val appContext: Context) :
    WeatherUtil.WeatherDetailsListener
  {
    fun update() {
      val errorFloat = 404f
      val name = SharedPrefManager.getString(appContext, PREF_KEY_NAME + appWidgetId)
      val lat = SharedPrefManager.getFloat(appContext, PREF_KEY_LAT + appWidgetId, errorFloat)
      val lon = SharedPrefManager.getFloat(appContext, PREF_KEY_LON + appWidgetId, errorFloat)

      if (name == null || lat == errorFloat || lon == errorFloat) {
        val errorText = appContext.getString(R.string.places_search_error)
        Toast.makeText(appContext, errorText, Toast.LENGTH_SHORT).show()
      } else {
        requestWeatherFor(name, lat, lon)
      }
    }

    override fun onWeatherReceived(weatherData: WeatherData, locationEntity: LocationEntity?) {
      val temperature = weatherData.tempC?.roundToInt()?.toString() ?: "-"
      val locationName = locationEntity?.locality ?: "Unknown location"
      Log.d(TAG, "onWeatherReceived: $temperature at $locationName")

      val openAppPendingIntent: PendingIntent = Intent(appContext, DetailActivity::class.java).let {
        PendingIntent.getActivity(appContext, 0, it, 0)
      }

      val views = RemoteViews(appContext.packageName, R.layout.fair_day_widget).apply {
        setOnClickPendingIntent(R.id.widget_top_level_view, openAppPendingIntent)
        setTextViewText(R.id.widget_current_temp, temperature)
        setTextViewText(R.id.widget_location_name, locationName)
        loadConditionImage(this, weatherData)
        loadBackgroundGradient(this, weatherData)
      }

      if (appWidgetId != -1) {
        Log.d(TAG, "onWeatherReceived: binding view data to widget $appWidgetId")
        AppWidgetManager.getInstance(appContext).updateAppWidget(appWidgetId, views)
      } else {
        Log.d(TAG, "missing widget ID")
      }
      Log.i(TAG, "onWeatherReceived: finished updating widget $appWidgetId")
    }

    private fun loadConditionImage(views: RemoteViews, weatherData: WeatherData) {
      val url = appContext.getString(R.string.weather_view_condition_url_prefix) +
          weatherData.conditionIconId +
          appContext.getString(R.string.weather_view_condition_url_suffix)

      val conditionImageTarget =
        AppWidgetTarget(appContext, R.id.widget_image, views, appWidgetId)

      Glide
        .with(appContext)
        .asBitmap()
        .load(url)
        .into(conditionImageTarget)

      val cd =
        appContext.getString(R.string.weather_view_cd_condition_description_prefix) +
            weatherData.conditionDescription +
            appContext.getString(R.string.weather_view_cd_condition_description_suffix)

      views.setContentDescription(R.id.widget_image, cd)
    }

    private fun loadBackgroundGradient(views: RemoteViews, weatherData: WeatherData) {
      val backgroundId = ConditionUtil.getConditionImageResId(weatherData.conditionIconId)

      val backgroundTarget =
        AppWidgetTarget(appContext, R.id.widget_background, views, appWidgetId)

      Glide
        .with(appContext)
        .asBitmap()
        .override(50, 100)
        .load(backgroundId)
        .into(backgroundTarget)
    }

    private fun requestWeatherFor(name: String, lat: Float, lon: Float) {
      WeatherUtil(appContext).apply {
        setWeatherDetailsListener(this@WidgetUpdater)
        requestFullWeatherFor(LocationEntity(name, lat, lon))
      }
    }

    companion object {
      private const val TAG = "GGG FDWP.WidgetUpdater"
    }
  }
}

internal fun updateAppWidgetContent(context: Context, appWidgetId: Int) {
  FairDayWidgetProvider.WidgetUpdater(appWidgetId, context).update()
}

private const val TAG = "GGG FDWP"