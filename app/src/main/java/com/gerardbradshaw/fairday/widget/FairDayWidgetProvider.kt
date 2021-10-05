package com.gerardbradshaw.fairday.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.text.format.Time
import android.util.Log
import android.widget.RemoteViews
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.activities.detail.DetailActivity
import com.gerardbradshaw.fairday.activities.detail.utils.AddressUtil
import com.gerardbradshaw.fairday.activities.detail.utils.GpsUtil
import com.gerardbradshaw.fairday.activities.detail.utils.WeatherUtil
import com.gerardbradshaw.fairday.room.LocationEntity
import com.gerardbradshaw.weatherview.datamodels.WeatherData
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [FairDayWidgetConfigureActivity]
 */
class FairDayWidgetProvider : AppWidgetProvider() {

  private lateinit var appWidgetManager: AppWidgetManager
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    Log.d(TAG, "onUpdate: preparing widget")
    this.appWidgetManager = appWidgetManager
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetId)
    }
  }

  private fun updateAppWidget(context: Context, appWidgetId: Int) {
    val pendingIntent: PendingIntent = Intent(context, DetailActivity::class.java)
      .let {
        PendingIntent.getActivity(context, 0, it, 0)
      }

    // Set listener so Widget opens the app when clicked
    val views = RemoteViews(context.packageName, R.layout.fair_day_widget)
    views.setOnClickPendingIntent(R.id.widget_top_level_view, pendingIntent)
    appWidgetManager.updateAppWidget(appWidgetId, views)

    // Start a service to first get the GPS coordinates, then locale, then weather info
    Log.d(TAG, "updateAppWidget: calling service")
    context.startService(
      Intent(context.applicationContext, FairDayWidgetUpdateService::class.java).apply {
        putExtra(EXTRA_WIDGET_ID, appWidgetId)
      }
    )
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    // When the user deletes the widget, delete the preference associated with it.
    for (appWidgetId in appWidgetIds) {
      deleteTitlePref(context, appWidgetId)
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
    private var widgetId: Int = -1
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
    override fun onStart(intent: Intent?, startId: Int) {
      Log.d(TAG, "onStart: service started")
      widgetId = intent?.getIntExtra(EXTRA_WIDGET_ID, -1) ?: -1
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

      val pendingIntent: PendingIntent = Intent(this, DetailActivity::class.java)
        .let {
          PendingIntent.getActivity(this, 0, it, 0)
        }

      val views = RemoteViews(this.packageName, R.layout.fair_day_widget).apply {
        setOnClickPendingIntent(R.id.widget_top_level_view, pendingIntent)
        setTextViewText(R.id.widget_current_temp, temperature)
        setTextViewText(R.id.widget_location_name, locationName)
      }

      if (widgetId != -1) {
        AppWidgetManager.getInstance(this).updateAppWidget(widgetId, views)
      } else {
        Log.d(TAG, "missing widget ID")
      }
    }

    override fun onAddressChanged(address: Address?) {
      if (address != null) {
        Log.d(TAG, "onAddressChanged: address received")
        val entity = LocationEntity(
          address.locality ?: "Unknown location",
          address.latitude.toFloat(),
          address.longitude.toFloat())

        weatherUtil.requestFullWeatherFor(entity)
      } else {
        Log.d(TAG, "onAddressChanged: address was null")
      }
    }
  }

  companion object {
    const val EXTRA_WIDGET_ID = "com.gerardbradshaw.fairday.widget.FairDayWidgetProvider.EXTRA_WIDGET_ID"
    private const val TAG = "GGG WidgetProvider"
    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS =  TimeUnit.HOURS.toMillis(1)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5)
  }
}