package com.gerardbradshaw.whetherweather.activities.detail.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.location.Address
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.whetherweather.Constants.KEY_GPS_NEEDED
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.activities.detail.utils.PermissionUtil.Companion.isGranted
import com.gerardbradshaw.whetherweather.application.BaseApplication
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

  private var settingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
  private var fusedLocationClient: FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(activity)

  private lateinit var locationCallback: LocationCallback
  private lateinit var locationRequest: LocationRequest
  private lateinit var locationSettingsRequest: LocationSettingsRequest
  private var listener: GpsUpdateListener? = null
  private var isGpsNeeded = false

  private val addressChangeListener: AddressUtil.AddressChangeListener =
      object : AddressUtil.AddressChangeListener {
        override fun onAddressChanged(address: Address?) {
          listener?.onGpsUpdate(address)
        }
      }

  private val activityResultLauncher =
    activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
      when (it) {
        true -> onLocationPermissionGranted()
        false -> Log.e(TAG, "activityResultLauncher: user denied permission")
        else -> Log.e(TAG, "activityResultLauncher: user ignored permission")
      }
    }


  // ------------------------ INIT ------------------------

  init {
    createLocationCallback()
    createLocationRequest()
    buildLocationSettingsRequest()
  }

  private fun createLocationCallback() {
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult?) {
        super.onLocationResult(locationResult)
        if (locationResult != null) {
          addressUtil.fetchAddress(locationResult.lastLocation, activity, addressChangeListener)
        } else {
          Log.e(TAG, "onLocationResult: locationResult is null")
        }
      }
    }
  }

  private fun createLocationRequest() {
    locationRequest = LocationRequest()
      .setInterval(DEFAULT_LOCATION_UPDATE_INTERVAL_MS)
      .setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL_MS)
      .setPriority(LocationRequest.PRIORITY_LOW_POWER)
  }

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
    val app = activity.application as BaseApplication
    isGpsNeeded = app.getBooleanPref(KEY_GPS_NEEDED, false)
    if (isGpsNeeded) startLocationUpdates()
  }

  /**
   * To be called by a registered [GpsUpdateListener] to stop requesting location updates.
   * Method is named to indicate its logical position in an [Activity] listener class.
   */
  fun onPause() {
    requestLocationUpdatesStop()
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
          toastLocationErrorAndLog("onActivityResult: User denied location")
          requestLocationUpdatesStop()
        }
      }
    } else toastLocationErrorAndLog("onActivityResult: unexpected request code")
  }

  /**
   * Sets the [GpsUpdateListener] which will hear the location updates.
   */
  fun setOnGpsUpdateListener(listener: GpsUpdateListener) {
    this.listener = listener
  }

  fun requestUpdates() {
    isGpsNeeded = true
    startLocationUpdates()
  }

  fun stopRequestingUpdates() {
    isGpsNeeded = false
    requestLocationUpdatesStop()
  }

  fun isRequestingUpdates(): Boolean {
    return isGpsNeeded
  }


  // ------------------------ STARTING ------------------------

  private fun startLocationUpdates() {
    if (isGranted(LOCATION_PERMISSION, activity)) onLocationPermissionGranted()
    else requestLocationPermission()
  }

  private fun requestLocationPermission() {
    PermissionUtil.RequestBuilder(LOCATION_PERMISSION, activity)
        .setRationaleDialogTitle(activity.resources.getString(R.string.message_location_rationale_title))
        .setRationaleDialogMessage(activity.resources.getString(R.string.app_name) + activity.resources.getString(R.string.message_location_rationale_body))
        .setActivityResultLauncher(activityResultLauncher)
        .setOnPermissionGranted { onLocationPermissionGranted() }
        .buildAndRequest()
  }

  private fun onLocationPermissionGranted() {
    verifyDeviceSettings()
  }

  @SuppressLint("MissingPermission")
  private fun verifyDeviceSettings() {
    settingsClient
      .checkLocationSettings(locationSettingsRequest)
      .addOnSuccessListener(activity) { onDeviceSettingsSatisfied() }
      .addOnFailureListener(activity) { e -> onDeviceSettingsNotSatisfied(e) }
  }

  @RequiresPermission(LOCATION_PERMISSION)
  private fun onDeviceSettingsSatisfied() {
    requestLocationUpdates()
  }

  @RequiresPermission(LOCATION_PERMISSION)
  private fun requestLocationUpdates() {
    fusedLocationClient.requestLocationUpdates(
      locationRequest,
      locationCallback,
      Looper.getMainLooper())
  }

  private fun onDeviceSettingsNotSatisfied(e: Exception) {
    when ((e as ApiException).statusCode) {
      LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> onSettingsResolutionRequired(e)
      LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> onSettingsChangeUnavailable()
    }
  }

  private fun onSettingsResolutionRequired(e: Exception) {
    Log.i(TAG, "onSettingsResolutionRequired: attempting to resolve settings")
    try {
      val rae = e as ResolvableApiException
      rae.startResolutionForResult(activity, REQUEST_CODE_CHECK_SETTINGS)
    } catch (sie: IntentSender.SendIntentException) {
      toastLocationErrorAndLog("onSettingsResolutionRequired: PendingIntent can't execute request.")
    }
  }

  private fun onSettingsChangeUnavailable() {
    toastLocationErrorAndLog("onSettingsChangeUnavailable: location settings change unavailable")
    stopRequestingUpdates()
  }


  // ------------------------ STOPPING ------------------------

  private fun requestLocationUpdatesStop() {
    fusedLocationClient
        .removeLocationUpdates(locationCallback)
        .addOnCompleteListener(activity) { onLocationUpdatesStopped() }
  }
  
  private fun onLocationUpdatesStopped() {
    Log.i(TAG, "onLocationUpdatesStopped: updates stopped")
  }

  private fun toastLocationErrorAndLog(logMsg: String) {
    Log.e(TAG, logMsg)

    Toast.makeText(
        activity,
        activity.resources.getString(R.string.string_unable_to_determine_location),
        Toast.LENGTH_SHORT).show()
  }


  // ------------------------ UTIL ------------------------

  interface GpsUpdateListener {
    fun onGpsUpdate(address: Address?)
  }

  companion object {
    private const val TAG = "GGG GpsUtil"
    private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    
    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS =  TimeUnit.MINUTES.toMillis(30)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30)

    const val REQUEST_CODE_CHECK_SETTINGS = 101
  }
}