package com.gerardbradshaw.fairday.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.fairday.Constants
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.SharedPrefManager
import com.gerardbradshaw.fairday.activities.utils.AutocompleteUtil
import com.gerardbradshaw.fairday.databinding.ActivityFairDayWidgetConfigureBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

/**
 * The configuration screen for the [AppWidgetProvider] AppWidget.
 */
class FairDayWidgetConfigureActivity : AppCompatActivity() {
  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
  private lateinit var autocompleteFragment: AutocompleteUtil
  private lateinit var binding: ActivityFairDayWidgetConfigureBinding

  private var searchForLocationButtonOnClickListener = View.OnClickListener {
    autocompleteFragment.getPlaceFromAutocomplete()
  }

  private var useCurrentLocationButtonOnClickListener = View.OnClickListener {
    deleteAllWidgetPrefs(this, appWidgetId)
    finishAndClose()
  }

  private fun finishAndClose() {
    Log.d(TAG, "finishAndClose: config finished")

    markWidgetAsConfigured(applicationContext, appWidgetId)
    updateAppWidgetContent(applicationContext, appWidgetId)

    // Make sure we pass back the original appWidgetId
    val resultValue = Intent()
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    setResult(RESULT_OK, resultValue)
    finish()
  }

  public override fun onCreate(icicle: Bundle?) {
    super.onCreate(icicle)
    setResult(RESULT_CANCELED) // deletes widget when activity closed

    binding = ActivityFairDayWidgetConfigureBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.searchForLocationButton.setOnClickListener(searchForLocationButtonOnClickListener)
    binding.useCurrentLocationButton.setOnClickListener(useCurrentLocationButtonOnClickListener)

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

    val title = "${getString(R.string.app_name)} ${getString(R.string.string_widget_setup)}"
    supportActionBar?.title = title

    initAutocompleteFragment()
  }

  private fun initAutocompleteFragment() {
    autocompleteFragment = AutocompleteUtil(this)

    val callback =
      ActivityResultCallback<ActivityResult> { result ->
        val intent = result?.data

        when {
          result == null || intent == null -> Log.wtf(TAG, "callback: missing intent!")

          result.resultCode == AutocompleteActivity.RESULT_OK -> {
            val place = Autocomplete.getPlaceFromIntent(intent)
            saveAllWidgetPrefs(this@FairDayWidgetConfigureActivity, appWidgetId, place)
            finishAndClose()
          }

          result.resultCode == AutocompleteActivity.RESULT_CANCELED -> {
            Log.i(TAG, "callback: no place selected.")
          }

          result.resultCode == AutocompleteActivity.RESULT_ERROR -> {
            Log.e(TAG, "callback: something went wrong with the autocomplete fragment.")
          }
        }
      }

    autocompleteFragment.overrideActivityResult(callback)
  }
}

internal const val PREF_NAME_PREFIX_KEY = "appwidget_name_"
internal const val PREF_LAT_PREFIX_KEY = "appwidget_lat_"
internal const val PREF_LON_PREFIX_KEY = "appwidget_lon_"
const val PREF_IS_CONFIGURED_PREFIX_KEY = "is_configured_"
private const val TAG = "GGG WidgetConfActivity"

internal fun markWidgetAsConfigured(context: Context, appWidgetId: Int) {
  context.getSharedPreferences(Constants.PREFS_FILE_KEY, 0)
    .edit()
    .putBoolean(PREF_IS_CONFIGURED_PREFIX_KEY + appWidgetId, true)
    .apply()
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

internal fun saveAllWidgetPrefs(context: Context, appWidgetId: Int, place: Place) {
  val addressComponents = place.addressComponents
  val locationName = extractLocalityFromAddressComponents(context, addressComponents)
  SharedPrefManager.putString(context, PREF_NAME_PREFIX_KEY + appWidgetId, locationName)

  val latLng = place.latLng

  if (latLng == null) {
    Log.d(TAG, "saveAllWidgetPrefs: no lat or lon!")
    return
  }

  SharedPrefManager.apply {
    putFloat(context, PREF_LAT_PREFIX_KEY + appWidgetId, latLng.latitude.toFloat())
    putFloat(context, PREF_LON_PREFIX_KEY + appWidgetId, latLng.longitude.toFloat())
  }
}

internal fun deleteAllWidgetPrefs(context: Context, appWidgetId: Int) {
  SharedPrefManager.apply {
    remove(context, PREF_NAME_PREFIX_KEY + appWidgetId)
    remove(context, PREF_LAT_PREFIX_KEY + appWidgetId)
    remove(context, PREF_LON_PREFIX_KEY + appWidgetId)
    remove(context, PREF_LON_PREFIX_KEY + appWidgetId)
  }
}