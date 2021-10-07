package com.gerardbradshaw.fairday.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.location.Address
import android.os.IBinder
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
import android.os.Build




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
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    // When the user deletes the widget, delete the preference associated with it.
    for (appWidgetId in appWidgetIds) {
      deleteAllWidgetPrefs(context, appWidgetId)
    }
  }

  override fun onEnabled(context: Context) {
    // Enter relevant functionality for when the first widget is created
  }

  override fun onDisabled(context: Context) {
    // Enter relevant functionality for when the last widget is disabled
  }


  class FairDayWidgetUpdateService :
    Service(),
    AddressUtil.AddressChangeListener,
    WeatherUtil.WeatherDetailsListener
  {
    private var appWidgetId: Int = -1
    private lateinit var weatherUtil: WeatherUtil
    private lateinit var addressUtil: AddressUtil
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest

    override fun onBind(intent: Intent?): IBinder? {
      return null
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
      Log.d(TAG, "loadCurrentLocation: loading weather for current location")
      fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

      weatherUtil = WeatherUtil(this).apply {
        setWeatherDetailsListener(this@FairDayWidgetUpdateService)
      }

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

    override fun onStart(intent: Intent?, startId: Int) {
      Log.d(TAG, "onStart: service started")
      appWidgetId = intent?.getIntExtra(EXTRA_WIDGET_ID, -1) ?: -1

      val locationName = loadWidgetPrefString(this, appWidgetId, PREF_NAME_PREFIX_KEY)
      val lat = loadWidgetPrefFloat(this, appWidgetId, PREF_LAT_PREFIX_KEY)
      val lon = loadWidgetPrefFloat(this, appWidgetId, PREF_LON_PREFIX_KEY)

      if (locationName == null || lat == null || lon == null) loadCurrentLocation()
      else loadUserDefinedLocation(locationName, lat, lon)
    }

    private fun createLocationCallback() {
      locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
          if (locationResult != null) {
            super.onLocationResult(locationResult)
            addressUtil = AddressUtil(false).also {
              it.fetchAddress(
                locationResult.lastLocation,
                applicationContext,
                this@FairDayWidgetUpdateService)
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

      val launchAppPendingIntent: PendingIntent = Intent(this, DetailActivity::class.java)
        .let {
          PendingIntent.getActivity(this, 0, it, 0)
        }

      val views = RemoteViews(this.packageName, R.layout.fair_day_widget).apply {
        setOnClickPendingIntent(R.id.widget_top_level_view, launchAppPendingIntent)
        setTextViewText(R.id.widget_current_temp, temperature)
        setTextViewText(R.id.widget_location_name, locationName)
        loadConditionImage(this, weatherData)
        loadBackgroundGradient(this, weatherData)
      }

      if (appWidgetId != -1) {
        AppWidgetManager.getInstance(this).updateAppWidget(appWidgetId, views)
      } else {
        Log.d(TAG, "missing widget ID")
      }
    }

    private fun loadConditionImage(views: RemoteViews, weatherData: WeatherData) {
      val url = applicationContext.getString(R.string.weather_view_condition_url_prefix) +
          weatherData.conditionIconId +
          applicationContext.getString(R.string.weather_view_condition_url_suffix)

      val conditionImageTarget =
        AppWidgetTarget(applicationContext, R.id.widget_image, views, appWidgetId)

      Glide
        .with(applicationContext)
        .asBitmap()
        .load(url)
        .into(conditionImageTarget)

      val cd =
        applicationContext.getString(R.string.weather_view_cd_condition_description_prefix) +
            weatherData.conditionDescription +
            applicationContext.getString(R.string.weather_view_cd_condition_description_suffix)

      views.setContentDescription(R.id.widget_image, cd)
    }

    private fun loadBackgroundGradient(views: RemoteViews, weatherData: WeatherData) {
      val backgroundId = ConditionUtil.getConditionImageResId(weatherData.conditionIconId)

      val backgroundTarget =
        AppWidgetTarget(applicationContext, R.id.widget_background, views, appWidgetId)

      Glide
        .with(applicationContext)
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
  }

  companion object {
    const val EXTRA_WIDGET_ID = "com.gerardbradshaw.fairday.widget.FairDayWidgetProvider.EXTRA_WIDGET_ID"
    private const val TAG = "GGG WidgetProvider"
    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.HOURS.toMillis(1)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5)
  }
}

internal fun updateAppWidget(
  context: Context,
  appWidgetManager: AppWidgetManager,
  appWidgetId: Int
) {
  val openAppPendingIntent: PendingIntent = Intent(context, DetailActivity::class.java)
    .let {
      PendingIntent.getActivity(context, 0, it, 0)
    }

  // Set listener so Widget opens the app when clicked
  val views = RemoteViews(context.packageName, R.layout.fair_day_widget)
  views.setOnClickPendingIntent(R.id.widget_top_level_view, openAppPendingIntent)
  appWidgetManager.updateAppWidget(appWidgetId, views)

  val intent = Intent(context.applicationContext, FairDayWidgetProvider.FairDayWidgetUpdateService::class.java).apply {
    putExtra(FairDayWidgetProvider.EXTRA_WIDGET_ID, appWidgetId)
  }

  context.startService(intent)

  // Start service to update widget
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//      context.startForegroundService(intent)
//    } else {
//      context.startService(intent)
//    }
//    context.startService(
//      Intent(context.applicationContext, FairDayWidgetUpdateService::class.java).apply {
//        putExtra(EXTRA_WIDGET_ID, appWidgetId)
//      }
//    )
}