package com.gerardbradshaw.whetherweather.activities.detail.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.gerardbradshaw.whetherweather.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Utility for requesting device location updates.
 * @param activity the activity to register for location updates.
 */
class GpsUtil @Inject constructor(
  private val activity: AppCompatActivity,
  private val addressUtil: AddressUtil
  ) {
  private var settingsClient: SettingsClient =
      LocationServices.getSettingsClient(activity)
  private var fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(activity)

  private lateinit var locationCallback: LocationCallback
  private lateinit var locationRequest: LocationRequest
  private lateinit var locationSettingsRequest: LocationSettingsRequest

  private var listener: GpsUpdateListener? = null
  private val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

  private var isGpsUpdatesRequested = false
    set(value) {
      field = value
      prefs.edit().putBoolean(KEY_GPS_UPDATES_REQUESTED, field).apply()
    }
    get() {
      return prefs.getBoolean(KEY_GPS_UPDATES_REQUESTED, false)
    }

  private val addressChangeListener: AddressUtil.AddressChangeListener =
      object : AddressUtil.AddressChangeListener {
        override fun onAddressChanged(address: Address?) {
          listener?.onGpsUpdate(address)
        }
      }

  private val activityResultLauncher =
    activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
      when (it) {
        true -> requestGpsUpdates()
        false -> Log.e(TAG, "activityResultLauncher: ERROR: user denied permission")
        else -> Log.e(TAG, "activityResultLauncher: ERROR: user ignored permission")
      }
    }


  // ------------------------ INIT ------------------------

  init {
    createLocationCallback()
    createLocationRequest()
    buildLocationSettingsRequest()
  }

  /**
   * Creates a callback for receiving location events.
   */
  private fun createLocationCallback() {
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult?) {
        super.onLocationResult(locationResult)
        if (locationResult != null) {
          addressUtil.fetchAddress(locationResult.lastLocation, activity, addressChangeListener)
        } else {
          Log.e(TAG, "onLocationResult: ERROR: locationResult is null")
        }
      }
    }
  }

  /**
   * Sets up the location request.
   */
  private fun createLocationRequest() {
    locationRequest = LocationRequest()
      .setInterval(DEFAULT_LOCATION_UPDATE_INTERVAL_MS)
      .setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL_MS)
      .setPriority(LocationRequest.PRIORITY_LOW_POWER)
  }

  /**
   * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
   * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
   * if a device has the needed location settings.
   */
  private fun buildLocationSettingsRequest() {
    locationSettingsRequest = LocationSettingsRequest
      .Builder()
      .addLocationRequest(locationRequest)
      .build()
  }
  

  // ------------------------ TO BE CALLED IN ACTIVITY ------------------------

  /**
   * To be called by a registered [GpsUpdateListener] to start location updates if it was
   * previously set to receive them. Method is named to indicate its logical position in an
   * [Activity] listener class.
   */
  fun onResume() {
    if (isGpsUpdatesRequested) startLocationUpdates()
  }

  /**
   * To be called by a registered [GpsUpdateListener] to stop requesting location updates.
   * Method is named to indicate its logical position in an [Activity] listener class.
   */
  fun onPause() {
    stopRequestingLocationUpdates()
  }

  /**
   * To be called by a registered [GpsUpdateListener] to handle Android settings changes
   * related to permissions.
   *
   * [requestCode], [resultCode], [data] are as per [Activity.onActivityResult].
   */
  fun onActivityResult(requestCode: Int, resultCode: Int) {
    if (requestCode == REQUEST_CODE_CHECK_SETTINGS) {
      when (resultCode) {
        Activity.RESULT_OK -> {
          Log.i(TAG, "onActivityResult: Settings change successful!")
          return // Nothing to do - startLocationUpdates() called in onResume
        }
        Activity.RESULT_CANCELED -> {
          toastLocationErrorAndLog("onActivityResult: ERROR: User denied location")
          stopRequestingLocationUpdates()
        }
      }
    } else toastLocationErrorAndLog("onActivityResult: ERROR: unexpected request code")
  }

  /**
   * Sets the [GpsUpdateListener] which will hear the location updates.
   */
  fun setOnGpsUpdateListener(listener: GpsUpdateListener) {
    this.listener = listener
  }

  fun requestUpdates() {
    isGpsUpdatesRequested = true
    startLocationUpdates()
  }

  fun stopRequestingUpdates() {
    isGpsUpdatesRequested = false
    stopRequestingLocationUpdates()
  }


  // ------------------------ LOCATION ------------------------

  private fun startLocationUpdates() {
    val isLocationPermissionGranted = PermissionUtil.isGranted(LOCATION_PERMISSION, activity)

    if (!isLocationPermissionGranted) requestLocationPermissions()
    else requestGpsUpdates()
  }

  private fun requestLocationPermissions() {
    PermissionUtil.RequestBuilder(LOCATION_PERMISSION, activity)
        .setRationaleDialogTitle(activity.resources.getString(R.string.message_location_rationale_title))
        .setRationaleDialogMessage(activity.resources.getString(R.string.app_name) + activity.resources.getString(R.string.message_location_rationale_body))
        .setActivityResultLauncher(activityResultLauncher)
        .setOnPermissionGranted { requestGpsUpdates() }
        .buildAndRequest()
  }

  @SuppressLint("MissingPermission") // permission is checked in onCreate()
  private fun requestGpsUpdates() {
    Log.i(TAG, "requestGpsUpdates: starting location updates")

    settingsClient
        .checkLocationSettings(locationSettingsRequest)
        .addOnSuccessListener(activity) { onLocationSettingsSatisfied() }
        .addOnFailureListener(activity) { e -> onLocationSettingsNotSatisfied(e) }
  }
  
  @RequiresPermission(LOCATION_PERMISSION)
  private fun onLocationSettingsSatisfied() {
    Log.i(TAG, "requestGpsUpdates: All settings satisfied. Starting updates.")

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.myLooper())
  }

  private fun onLocationSettingsNotSatisfied(e: Exception) {
    when ((e as ApiException).statusCode) {

      LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
        Log.i(TAG, "onLocationSettingsNotSatisfied: attempting to resolve settings")
        try {
          val rae = e as ResolvableApiException
          rae.startResolutionForResult(activity, REQUEST_CODE_CHECK_SETTINGS)
        } catch (sie: IntentSender.SendIntentException) {
          toastLocationErrorAndLog("onLocationSettingsNotSatisfied: ERROR: PendingIntent can't execute request.")
        }
      }

      LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
        toastLocationErrorAndLog("onLocationSettingsNotSatisfied: ERROR: location settings change unavailable")
        stopRequestingLocationUpdates()
      }
    }
  }

  /**
   * Removes location updates from the FusedLocationApi.
   */
  private fun stopRequestingLocationUpdates() {
    if (!isGpsUpdatesRequested) {
      Log.i(TAG, "stopRequestingLocationUpdates: updates never requested. Nothing to stop!")
      return
    }

    fusedLocationClient
        .removeLocationUpdates(locationCallback)
        .addOnCompleteListener(activity) { onLocationUpdateRequestsStopped() }
  }
  
  private fun onLocationUpdateRequestsStopped() {
    Log.i(TAG, "onLocationUpdateRequestsStopped: updates stopped")
  }

  private fun toastLocationErrorAndLog(logMsg: String) {
    Log.e(TAG, logMsg)

    Toast.makeText(
        activity,
        activity.resources.getString(R.string.string_unable_to_determine_location),
        Toast.LENGTH_SHORT).show()
  }


  // ------------------------ UTIL ------------------------

  fun isRequestingUpdates(): Boolean {
    return isGpsUpdatesRequested
  }

  interface GpsUpdateListener {
    fun onGpsUpdate(address: Address?)
  }

  companion object {
    private const val TAG = "GGG GpsUtil"
    private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    
    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS =  TimeUnit.MINUTES.toMillis(30)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30)

    const val REQUEST_CODE_CHECK_SETTINGS = 101

    private const val KEY_GPS_UPDATES_REQUESTED = "requesting-location-updates"
  }
}