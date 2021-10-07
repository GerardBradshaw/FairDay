package com.gerardbradshaw.fairday.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.fairday.Constants
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.databinding.FairDayWidgetConfigureBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

/**
 * The configuration screen for the [FairDayWidgetProvider] AppWidget.
 */
class FairDayWidgetConfigureActivity : AppCompatActivity() {
  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
  private lateinit var autocompleteFragment: AutocompleteSupportFragment

  private var useSelectedLocationButtonOnClickListener = View.OnClickListener {
    finishAndClose()
  }

  private var useMyLocationButtonOnClickListener = View.OnClickListener {
    deleteAllWidgetPrefs(this, appWidgetId)
    finishAndClose()
  }

  private fun finishAndClose() {
    // It is the responsibility of the configuration activity to update the app widget
    val appWidgetManager = AppWidgetManager.getInstance(this)
    val views = RemoteViews(this.packageName, R.layout.fair_day_widget)
    updateAppWidget(this, appWidgetManager, appWidgetId)

    // Make sure we pass back the original appWidgetId
    val resultValue = Intent()
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    setResult(RESULT_OK, resultValue)
    finish()
  }

  private lateinit var binding: FairDayWidgetConfigureBinding

  public override fun onCreate(icicle: Bundle?) {
    super.onCreate(icicle)
    setResult(RESULT_CANCELED) // deletes widget when activity closed

    binding = FairDayWidgetConfigureBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.useSelectedLocationButton.setOnClickListener(useSelectedLocationButtonOnClickListener)
    binding.useMyLocationButton.setOnClickListener(useMyLocationButtonOnClickListener)

    // Find the widget id from the intent.
    val extras = intent.extras
    if (extras != null) {
      appWidgetId =
        extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    // If this activity was started with an intent without an app widget ID, finish with an error.
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    initPlacesApi()

    autocompleteFragment = supportFragmentManager
      .findFragmentById(R.id.widget_autocomplete_fragment) as AutocompleteSupportFragment

    autocompleteFragment.setPlaceFields(listOf(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG))

    autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
      override fun onPlaceSelected(p0: Place) {
        Log.d(TAG, "onPlaceSelected: ${p0.address}")
        saveAllWidgetPrefs(this@FairDayWidgetConfigureActivity, appWidgetId, p0)
      }

      override fun onError(p0: Status) {
        Log.d(TAG, "onError: ${p0.statusCode}")
        deleteAllWidgetPrefs(this@FairDayWidgetConfigureActivity, appWidgetId)
      }
    })
  }

  private fun initPlacesApi() {
    if (!Places.isInitialized()) {
      Places.initialize(application, Constants.API_KEY_MAPS)
    }
  }
}

private const val PREFS_NAME = "com.gerardbradshaw.fairday.FairDayWidget"
internal const val PREF_NAME_PREFIX_KEY = "appwidget_name_"
internal const val PREF_LAT_PREFIX_KEY = "appwidget_lat_"
internal const val PREF_LON_PREFIX_KEY = "appwidget_lon_"
private const val TAG = "GGG WidgetConfActivity"

internal fun saveWidgetPrefString(context: Context, appWidgetId: Int, keyPrefix: String, value: String) {
  context.getSharedPreferences(PREFS_NAME, 0).edit().apply {
    putString(keyPrefix + appWidgetId, value)
    apply()
  }
}

internal fun saveWidgetPrefFloat(context: Context, appWidgetId: Int, keyPrefix: String, value: Float) {
  context.getSharedPreferences(PREFS_NAME, 0).edit().apply {
    putFloat(keyPrefix + appWidgetId, value)
    apply()
  }
}

internal fun loadWidgetPrefString(context: Context, appWidgetId: Int, keyPrefix: String): String? {
  val prefs = context.getSharedPreferences(PREFS_NAME, 0)
  return prefs.getString(keyPrefix + appWidgetId, null)
}

internal fun loadWidgetPrefFloat(context: Context, appWidgetId: Int, keyPrefix: String): Float? {
  val prefs = context.getSharedPreferences(PREFS_NAME, 0)
  val prefValue = prefs.getFloat(keyPrefix + appWidgetId, 404f)
  return if (prefValue == 404f) null else prefValue
}

internal fun deleteWidgetPref(context: Context, appWidgetId: Int, keyPrefix: String) {
  context.getSharedPreferences(PREFS_NAME, 0).edit().apply {
    remove(keyPrefix + appWidgetId)
    apply()
  }
}

internal fun extractLocalityFromAddressComponents(
  context: Context,
  components: AddressComponents?
): String {
  if (components != null) {
    val componentsList = components.asList()
    for (component in componentsList) {
      for (type in component.types) {
        if (type == "locality") {
          return component.name
        }
      }
    }
  }
  return context.getString(R.string.string_unknown_location)
}

internal fun saveAllWidgetPrefs(
  context: Context,
  appWidgetId: Int,
  place: Place
) {
  val addressComponents = place.addressComponents
  val locationName = extractLocalityFromAddressComponents(context, addressComponents)
  saveWidgetPrefString(context, appWidgetId, PREF_NAME_PREFIX_KEY, locationName)

  val latLng = place.latLng

  if (latLng == null) {
    Log.d(TAG, "saveAllWidgetPrefs: no lat or lon!")
    return
  }

  saveWidgetPrefFloat(context, appWidgetId, PREF_LAT_PREFIX_KEY, latLng.latitude.toFloat())
  saveWidgetPrefFloat(context, appWidgetId, PREF_LON_PREFIX_KEY, latLng.longitude.toFloat())
}

internal fun deleteAllWidgetPrefs(context: Context, appWidgetId: Int) {
  deleteWidgetPref(context, appWidgetId, PREF_NAME_PREFIX_KEY)
  deleteWidgetPref(context, appWidgetId, PREF_LAT_PREFIX_KEY)
  deleteWidgetPref(context, appWidgetId, PREF_LON_PREFIX_KEY)
}