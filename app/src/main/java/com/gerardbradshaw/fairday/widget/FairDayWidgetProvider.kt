package com.gerardbradshaw.fairday.widget

import android.annotation.SuppressLint
import android.app.*
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.location.Address
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.activities.detail.DetailActivity
import com.gerardbradshaw.fairday.activities.detail.utils.AddressUtil
import com.gerardbradshaw.fairday.activities.detail.utils.ConditionUtil
import com.gerardbradshaw.fairday.activities.detail.utils.WeatherUtil
import com.gerardbradshaw.fairday.room.LocationEntity
import com.gerardbradshaw.weatherview.datamodels.WeatherData
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

import androidx.work.*
import com.gerardbradshaw.fairday.Constants
import java.lang.RuntimeException

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

  private fun isConfigured(context: Context, appWidgetId: Int): Boolean {
    val prefs = context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    val isConfigured = prefs.getBoolean(PREF_IS_CONFIGURED_PREFIX_KEY + appWidgetId, false)
    Log.d(TAG, "isConfigured: widget $appWidgetId is ${if (!isConfigured) "not " else ""}configured")
    return isConfigured
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    for (appWidgetId in appWidgetIds) {
      deleteAllWidgetPrefs(context, appWidgetId)
    }
  }

  class UpdateWorker(
    private var appWidgetId: Int,
    private val appContext: Context) :
    AddressUtil.AddressChangeListener,
    WeatherUtil.WeatherDetailsListener
  {
//    private var appWidgetId: Int = -1
    private lateinit var weatherUtil: WeatherUtil
    private lateinit var addressUtil: AddressUtil
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest

    fun doWork() {
//      appWidgetId = workerParams.inputData.getInt(EXTRA_WIDGET_ID, -1)
      Log.d(TAG, "doWork: worker started on widget $appWidgetId")

      weatherUtil = WeatherUtil(appContext).apply {
        setWeatherDetailsListener(this@UpdateWorker)
      }

      val locationName = loadWidgetPrefString(appContext, appWidgetId, PREF_NAME_PREFIX_KEY)
      val lat = loadWidgetPrefFloat(appContext, appWidgetId, PREF_LAT_PREFIX_KEY)
      val lon = loadWidgetPrefFloat(appContext, appWidgetId, PREF_LON_PREFIX_KEY)

      if (locationName == null || lat == null || lon == null) loadCurrentLocation()
      else loadUserDefinedLocation(locationName, lat, lon)
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
      Log.d(TAG, "loadCurrentLocation: requesting GPS location")
      fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

      createLocationCallback()
      createLocationRequest()
      buildLocationSettingsRequest()

      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper())
    }

    private fun loadUserDefinedLocation(locationName: String, lat: Float, lon: Float) {
      Log.d(TAG, "loadUserDefinedLocation: loading weather for $locationName")
      requestWeatherFor(locationName, lat, lon)
    }

    private fun createLocationCallback() {
      locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
          if (locationResult != null) {
            super.onLocationResult(locationResult)
            Log.d(TAG, "onLocationResult: GPS location found. Now fetching address.")
            addressUtil = AddressUtil(false).also {
              it.fetchAddress(locationResult.lastLocation, appContext, this@UpdateWorker)
            }
          } else {
            Log.e(TAG, "onLocationResult: locationResult is null")
          }
        }
      }
    }

    @Suppress("DEPRECATION") // TODO replace
    private fun createLocationRequest() {
      locationRequest = LocationRequest()
        .setInterval(DEFAULT_LOCATION_UPDATE_INTERVAL_MS)
        .setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL_MS)
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    }

    private fun buildLocationSettingsRequest() {
      locationSettingsRequest = LocationSettingsRequest
        .Builder()
        .addLocationRequest(locationRequest)
        .build()
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

        val sdf = SimpleDateFormat("dd/MMM HH:mm")
        setTextViewText(R.id.widget_location_name, sdf.format(Calendar.getInstance().time))
//        setTextViewText(R.id.widget_location_name, locationName)

        loadConditionImage(this, weatherData)
        loadBackgroundGradient(this, weatherData)
      }

      if (appWidgetId != -1) {
        Log.d(TAG, "onWeatherReceived: binding view data to widget $appWidgetId")
        AppWidgetManager.getInstance(appContext).updateAppWidget(appWidgetId, views)
        markUpdateTime(appContext, appWidgetId)
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
        .override(100, 50)
        .load(backgroundId)
        .into(backgroundTarget)
    }

    override fun onAddressChanged(address: Address?) {
      if (address != null) {
        Log.d(TAG, "onAddressChanged: address received")
        requestWeatherFor(address.locality, address.latitude.toFloat(), address.longitude.toFloat())
      } else {
        Log.d(TAG, "onAddressChanged: address was null")
      }
    }

    private fun requestWeatherFor(locationName: String?, lat: Float, lon: Float) {
      val entity = LocationEntity(locationName ?: "Unknown location", lat, lon)
      weatherUtil.requestFullWeatherFor(entity)
    }

    companion object {
      private const val TAG = "GGG FDWP.UpdateWorker"
    }
  }

  companion object {
    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.HOURS.toMillis(1)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.HOURS.toMillis(1)
  }
}

internal fun updateAppWidgetContent(context: Context, appWidgetId: Int) {
  val updateWorker = FairDayWidgetProvider.UpdateWorker(appWidgetId, context)
  updateWorker.doWork()

//  val inputData = Data.Builder().putInt(EXTRA_WIDGET_ID, appWidgetId).build()
//
//  val updateWorkRequest: WorkRequest =
//    OneTimeWorkRequestBuilder<FairDayWidgetProvider.UpdateWorker>()
//      .setInputData(inputData)
//      .build()
//
//  WorkManager.getInstance(context).enqueue(updateWorkRequest)
}

internal fun markUpdateTime(context: Context, appWidgetId: Int) {
  Log.d(TAG, "markUpdateTime: marking widget $appWidgetId update time")
  context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    .edit()
    .putLong(PREF_WIDGET_UPDATE_TIME + appWidgetId, Date().time)
    .apply()
}

private const val TAG = "GGG FDWP"
private const val PACKAGE_NAME = "com.gerardbradshaw.fairday.widget.FairDayWidgetProvider"
internal const val PREF_WIDGET_UPDATE_TIME = "is_updated_recently"
const val EXTRA_WIDGET_ID = "$PACKAGE_NAME.EXTRA_WIDGET_ID"
