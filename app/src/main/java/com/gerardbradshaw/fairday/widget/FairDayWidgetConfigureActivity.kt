package com.gerardbradshaw.fairday.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.SharedPrefManager
import com.gerardbradshaw.fairday.activities.detail.DetailActivity
import com.gerardbradshaw.fairday.activities.detail.utils.AddressUtil
import com.gerardbradshaw.fairday.activities.detail.utils.GpsUtil
import com.gerardbradshaw.fairday.activities.utils.AutocompleteUtil
import com.gerardbradshaw.fairday.databinding.ActivityFairDayWidgetConfigureBinding
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity

/**
 * The configuration screen for the [FairDayWidgetProvider] AppWidget.
 */
class FairDayWidgetConfigureActivity :
  AppCompatActivity(),
  GpsUtil.GpsUtilListener
{
  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
  private lateinit var binding: ActivityFairDayWidgetConfigureBinding
  private lateinit var autocompleteFragment: AutocompleteUtil
  private lateinit var gpsUtil: GpsUtil
  private lateinit var addressUtil: AddressUtil

  private var searchForLocationButtonOnClickListener = View.OnClickListener {
    autocompleteFragment.getPlaceFromAutocomplete()
  }

  private var useCurrentLocationButtonOnClickListener = View.OnClickListener {
    deleteAllWidgetPrefs(this, appWidgetId)
    binding.loadingFrame.visibility = View.VISIBLE
    gpsUtil.forceStart()
  }

  // ------------------------ INIT ------------------------

  public override fun onCreate(icicle: Bundle?) {
    super.onCreate(icicle)
    setResult(RESULT_CANCELED) // deletes widget when activity closed

    binding = ActivityFairDayWidgetConfigureBinding.inflate(layoutInflater).apply {
      setContentView(root)
      searchForLocationButton.setOnClickListener(searchForLocationButtonOnClickListener)
      useCurrentLocationButton.setOnClickListener(useCurrentLocationButtonOnClickListener)
      openFairDayButton.setOnClickListener {
        startActivity(Intent(
          this@FairDayWidgetConfigureActivity,
          DetailActivity::class.java))
        finishAndClose(RESULT_CANCELED)
      }
    }

    val title = "${getString(R.string.app_name)} ${getString(R.string.string_widget_setup)}"
    supportActionBar?.title = title

    // Find the widget id from the intent
    appWidgetId = intent.extras
      ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
      ?: AppWidgetManager.INVALID_APPWIDGET_ID

    // If this activity was started with an intent without an app widget ID, finish with an error.
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    addressUtil = AddressUtil(false)
    gpsUtil = GpsUtil(this, addressUtil)
    gpsUtil.setOnGpsUpdateListener(this)

    initAutocompleteFragment()
  }

  private fun initAutocompleteFragment() {
    autocompleteFragment = AutocompleteUtil(this)

    val autocompleteCallback =
      ActivityResultCallback<ActivityResult> { result ->
        val intent = result?.data
        if (result == null || intent == null) {
          Log.e(TAG, "callback: missing intent!")
          return@ActivityResultCallback
        }

        when (result.resultCode){
          AutocompleteActivity.RESULT_OK -> {
            val place = Autocomplete.getPlaceFromIntent(intent)
            finishAndClose(saveAllWidgetPrefs(applicationContext, appWidgetId, place))
          }

          AutocompleteActivity.RESULT_CANCELED -> {
            Log.i(TAG, "callback: no place selected.")
          }

          AutocompleteActivity.RESULT_ERROR -> {
            Log.e(TAG, "callback: something went wrong with the autocomplete fragment.")
          }
        }
      }

    autocompleteFragment.overrideActivityResult(autocompleteCallback)
  }


  // ------------------------ EXTERNAL EVENTS ------------------------

  override fun onGpsUpdate(address: Address?) {
    if (address == null) {
      Log.e(TAG, "onGpsUpdate: gps returned an empty address!")
      Toast.makeText(this, "Service unavailable.", Toast.LENGTH_SHORT).show()
      finishAndClose(RESULT_CANCELED)
    } else {
      finishAndClose(saveAllWidgetPrefs(this, appWidgetId, address))
    }
  }

  @Suppress("DEPRECATION") // TODO remove
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    Log.d(TAG, "onActivityResult: ")
    when (requestCode) {
      GpsUtil.REQUEST_CODE_CHECK_SETTINGS -> {
        gpsUtil.onActivityResult(requestCode, resultCode)
        if (resultCode != Activity.RESULT_OK) {
          Toast.makeText(this, "Please allow location access in the settings app.", Toast.LENGTH_LONG).show()
          finishAndClose()
        } else {
          gpsUtil.forceStart()
        }
      }
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }


  // ------------------------ FINISH ------------------------

  private fun finishAndClose(resultCode: Int = RESULT_OK) {
    if (resultCode == RESULT_OK) {
      markWidgetAsConfigured(appWidgetId)
      updateAppWidgetContent(applicationContext, appWidgetId)
    } else {
      deleteAllWidgetPrefs(applicationContext, appWidgetId)
    }

    // Make sure we pass back the original appWidgetId
    Intent().apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setResult(resultCode, this)
    }
    finish()
  }

  private fun markWidgetAsConfigured(appWidgetId: Int) {
    SharedPrefManager.putBoolean(this, PREF_KEY_IS_CONFIGURED + appWidgetId, true)
  }

  /**
   * Returns RESULT_OK only if prefs were saved successfully.
   */
  private fun saveAllWidgetPrefs(context: Context, appWidgetId: Int, place: Place): Int {
    SharedPrefManager.putString(
      context,
      PREF_KEY_NAME + appWidgetId,
      AddressUtil.extractLocalityFromAddressComponents(context, place.addressComponents))

    val latLng = place.latLng

    if (latLng == null) {
      Log.d(TAG, "saveAllWidgetPrefs: no lat or lon!")
      return Activity.RESULT_CANCELED
    }

    SharedPrefManager.apply {
      putFloat(context, PREF_KEY_LAT + appWidgetId, latLng.latitude.toFloat())
      putFloat(context, PREF_KEY_LON + appWidgetId, latLng.longitude.toFloat())
    }
    return Activity.RESULT_OK
  }

  /**
   * Returns RESULT_OK only if prefs were saved successfully.
   */
  private fun saveAllWidgetPrefs(context: Context, appWidgetId: Int, address: Address): Int {
    SharedPrefManager.putString(
      context,
      PREF_KEY_NAME + appWidgetId,
      address.locality ?: context.getString(R.string.string_unknown_location))

    try {
      SharedPrefManager.apply {
        putFloat(context, PREF_KEY_LAT + appWidgetId, address.latitude.toFloat())
        putFloat(context, PREF_KEY_LON + appWidgetId, address.longitude.toFloat())
      }
    } catch (e: IllegalStateException) {
      Log.e(TAG, "saveAllWidgetPrefs: gps returned a bad address (no coordinates)")
      return Activity.RESULT_CANCELED
    }

    return Activity.RESULT_OK
  }
}

internal fun deleteAllWidgetPrefs(context: Context, appWidgetId: Int) {
  SharedPrefManager.apply {
    remove(context, PREF_KEY_NAME + appWidgetId)
    remove(context, PREF_KEY_LAT + appWidgetId)
    remove(context, PREF_KEY_LON + appWidgetId)
    remove(context, PREF_KEY_IS_CONFIGURED + appWidgetId)
  }
}

internal const val PREF_KEY_NAME = "appwidget_name_"
internal const val PREF_KEY_LAT = "appwidget_lat_"
internal const val PREF_KEY_LON = "appwidget_lon_"
const val PREF_KEY_IS_CONFIGURED = "is_configured_"
private const val TAG = "GGG WidgetConfigActivit"