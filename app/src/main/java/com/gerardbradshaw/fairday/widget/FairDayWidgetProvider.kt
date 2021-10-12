package com.gerardbradshaw.fairday.widget

import android.annotation.SuppressLint
import android.app.*
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
import java.text.SimpleDateFormat
import java.util.*

import android.os.Build
import androidx.work.*
import com.gerardbradshaw.fairday.Constants

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
      if (isConfigured(context, appWidgetId) && !isUpdatedRecently(context, appWidgetId)) {
        Log.d(TAG, "onUpdate: updating widget $appWidgetId")
        refreshWidget(context, appWidgetId)
      } else {
        Log.d(TAG, "onUpdate: skipping widget $appWidgetId update for now")
      }
    }
  }

  private fun isUpdatedRecently(context: Context, appWidgetId: Int): Boolean {
    val prefs = context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    val lastCallTime = prefs.getLong(PREF_WIDGET_UPDATE_TIME + appWidgetId, 0)
    val minsSinceLastUpdate = (Date().time - lastCallTime)/60000
    Log.d(TAG, "isUpdatedRecently: updated ${minsSinceLastUpdate.toInt()} minutes ago")
    return minsSinceLastUpdate < 5
  }

  private fun markUpdateTime(context: Context, appWidgetId: Int) {
    context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
      .edit()
      .putLong(PREF_WIDGET_UPDATE_TIME + appWidgetId, Date().time)
      .apply()
  }

  private fun isConfigured(context: Context, appWidgetId: Int): Boolean {
    val prefs = context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    return prefs.getBoolean(PREF_IS_CONFIGURED_KEY + appWidgetId, false)
  }

  private fun refreshWidget(
    context: Context,
    appWidgetId: Int
  ) {
    val openAppPendingIntent: PendingIntent = Intent(context, DetailActivity::class.java)
      .let {
        PendingIntent.getActivity(context, 0, it, 0)
      }

    // Set listener so Widget opens the app when clicked
    val views = RemoteViews(context.packageName, R.layout.fair_day_widget)
    views.setOnClickPendingIntent(R.id.widget_top_level_view, openAppPendingIntent)
    updateAppWidgetContent(context, appWidgetId)
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    // When the user deletes the widget, delete the preference associated with it.
    for (appWidgetId in appWidgetIds) {
      deleteAllWidgetPrefs(context, appWidgetId)

      context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
        .edit()
        .remove(PREF_WIDGET_UPDATE_TIME + appWidgetId)
        .apply()
    }
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    super.onReceive(context, intent)
  }

  class UpdateWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters
    ) :
    Worker(appContext, workerParams),
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

    override fun doWork(): Result {
      Log.d(TAG, "doWork: work started")
      appWidgetId = workerParams.inputData.getInt(EXTRA_WIDGET_ID, -1)

      weatherUtil = WeatherUtil(appContext).apply {
        setWeatherDetailsListener(this@UpdateWorker)
      }

      val locationName = loadWidgetPrefString(appContext, appWidgetId, PREF_NAME_PREFIX_KEY)
      val lat = loadWidgetPrefFloat(appContext, appWidgetId, PREF_LAT_PREFIX_KEY)
      val lon = loadWidgetPrefFloat(appContext, appWidgetId, PREF_LON_PREFIX_KEY)

      if (locationName == null || lat == null || lon == null) loadCurrentLocation()
      else loadUserDefinedLocation(locationName, lat, lon)
      return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
      Log.d(TAG, "loadCurrentLocation: loading weather for current location")
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
            Log.d(TAG, "onLocationResult: location found. looking up address.")
            addressUtil = AddressUtil(false).also {
              it.fetchAddress(
                locationResult.lastLocation,
                applicationContext,
                this@UpdateWorker
              )
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

      val launchAppPendingIntent: PendingIntent = Intent(appContext, DetailActivity::class.java)
        .let {
          PendingIntent.getActivity(appContext, 0, it, 0)
        }

      val cal = Calendar.getInstance()
      val sdf = SimpleDateFormat("dd/MMM HH:mm")

      val views = RemoteViews(appContext.packageName, R.layout.fair_day_widget).apply {
        setOnClickPendingIntent(R.id.widget_top_level_view, launchAppPendingIntent)
        setTextViewText(R.id.widget_current_temp, temperature)

        setTextViewText(R.id.widget_location_name, sdf.format(cal.time))
//        setTextViewText(R.id.widget_location_name, locationName)

        loadConditionImage(this, weatherData)
        loadBackgroundGradient(this, weatherData)
      }

      if (appWidgetId != -1) {
        AppWidgetManager.getInstance(appContext).updateAppWidget(appWidgetId, views)
      } else {
        Log.d(TAG, "missing widget ID")
      }
      Log.i(TAG, "onWeatherReceived: done for now! stopping service")
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

    companion object {
      private const val TAG = "GGG FDWP.UpdateWorker"
    }
  }





  @Deprecated("Use UpdateWorker class instead")
  class UpdateService :
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

    override fun onCreate() {
      super.onCreate()
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        val channelId = "app_widget_channel"

        val channel =
          NotificationChannel(
            channelId,
            "Widget update service",
            NotificationManager.IMPORTANCE_DEFAULT
          )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
          .createNotificationChannel(channel)

        val notification: Notification = Notification.Builder(this, channelId)
          .setContentTitle("")
          .setContentText("").build()
        startForeground(1, notification)
      }
    }

    override fun onBind(intent: Intent?): IBinder? {
      return null
    }

    override fun onStart(intent: Intent?, startId: Int) {
      Log.d(TAG, "onStart: service started")
      appWidgetId = intent?.getIntExtra(EXTRA_WIDGET_ID, -1) ?: -1

      weatherUtil = WeatherUtil(this).apply {
        setWeatherDetailsListener(this@UpdateService)
      }

      val locationName = loadWidgetPrefString(this, appWidgetId, PREF_NAME_PREFIX_KEY)
      val lat = loadWidgetPrefFloat(this, appWidgetId, PREF_LAT_PREFIX_KEY)
      val lon = loadWidgetPrefFloat(this, appWidgetId, PREF_LON_PREFIX_KEY)

      if (locationName == null || lat == null || lon == null) loadCurrentLocation()
      else loadUserDefinedLocation(locationName, lat, lon)
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
      Log.d(TAG, "loadCurrentLocation: loading weather for current location")
      fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
            addressUtil = AddressUtil(false).also {
              it.fetchAddress(
                locationResult.lastLocation,
                applicationContext,
                this@UpdateService)
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

      val cal = Calendar.getInstance()
      val sdf = SimpleDateFormat("dd/MMM HH:mm")


      val views = RemoteViews(this.packageName, R.layout.fair_day_widget).apply {
        setOnClickPendingIntent(R.id.widget_top_level_view, launchAppPendingIntent)
        setTextViewText(R.id.widget_current_temp, temperature)

        setTextViewText(R.id.widget_location_name, sdf.format(cal.time))
//        setTextViewText(R.id.widget_location_name, locationName)

        loadConditionImage(this, weatherData)
        loadBackgroundGradient(this, weatherData)
      }

      if (appWidgetId != -1) {
        AppWidgetManager.getInstance(this).updateAppWidget(appWidgetId, views)
      } else {
        Log.d(TAG, "missing widget ID")
      }
      Log.i(TAG, "onWeatherReceived: done for now! stopping service")
      this.stopSelf()
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

    companion object {
      private const val TAG = "GGG FDWP.UpdateService"
    }
  }

  companion object {
    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.HOURS.toMillis(1)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5)
  }
}

internal fun updateAppWidgetContent(context: Context, appWidgetId: Int) {
  val inputData = Data.Builder().putInt(EXTRA_WIDGET_ID, appWidgetId).build()

  val updateWorkRequest: WorkRequest =
    OneTimeWorkRequestBuilder<FairDayWidgetProvider.UpdateWorker>()
      .setInputData(inputData)
      .build()

  WorkManager.getInstance(context).enqueue(updateWorkRequest)
}

@Deprecated("Use updateAppWidgetContent() instead")
internal fun updateAppWidgetContentWithService(context: Context, appWidgetId: Int) {
  val intent = Intent(
    context.applicationContext,
    FairDayWidgetProvider.UpdateService::class.java
  ).apply {
    putExtra(EXTRA_WIDGET_ID, appWidgetId)
  }

  Log.d(TAG, "updateAppWidgetContent: updating widget $appWidgetId")
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
    context.startForegroundService(intent)
  } else{
    context.startService(intent)
  }
}

private const val TAG = "GGG WidgetProvider"
private const val PACKAGE_NAME = "com.gerardbradshaw.fairday.widget.FairDayWidgetProvider"
internal const val PREF_WIDGET_UPDATE_TIME = "is_updated_recently"
const val EXTRA_WIDGET_ID = "$PACKAGE_NAME.EXTRA_WIDGET_ID"