package com.gerardbradshaw.fairday.activities.detail.utils

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
import androidx.preference.PreferenceManager
import com.gerardbradshaw.fairday.Constants
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.activities.detail.utils.PermissionUtil.Companion.isGranted
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

  var isGpsEnabled = false
    private set

  private var isGpsRequestedSharedPref: Boolean
    get() = PreferenceManager.getDefaultSharedPreferences(activity)
      .getBoolean(Constants.KEY_GPS_REQUESTED, false)
    set(value) = PreferenceManager.getDefaultSharedPreferences(activity).edit()
      .putBoolean(Constants.KEY_GPS_REQUESTED, value).apply()

  private var listener: GpsUtilListener? = null

  private val addressChangeListener: AddressUtil.AddressChangeListener =
    object : AddressUtil.AddressChangeListener {
      override fun onAddressChanged(address: Address?) {
        listener?.onGpsUpdate(address)
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
        if (locationResult != null) {
          super.onLocationResult(locationResult)
          addressUtil.fetchAddress(locationResult.lastLocation, activity, addressChangeListener)
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


  // ------------------------ TO BE CALLED IN ACTIVITY ------------------------

  /**
   * To be called by a registered [GpsUtilListener] to start location updates if it was
   * previously set to receive them. It is suggested that this method is run during the listening
   * Activity / Fragment onCreate() or onResume().
   */
  fun start() {
    if (isGpsRequestedSharedPref) startLocationUpdates()
  }

  /**
   * To be called by a registered [GpsUtilListener] to stop requesting location updates.
   * Method is named to indicate its logical position in an [Activity] listener class.
   */
  fun stop() {
    requestLocationUpdatesStop()
  }

  /**
   * To be called by a registered [GpsUtilListener] to handle Android settings changes
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
   * Sets the [GpsUtilListener] which will hear the location updates.
   */
  fun setOnGpsUpdateListener(listener: GpsUtilListener) {
    this.listener = listener
  }

  fun requestUpdates() {
    startLocationUpdates()
  }

  fun stopRequestingUpdates() {
    requestLocationUpdatesStop()
  }

  /**
   * Returns the new (toggled) state of this.isGpsEnabled
   */
  fun toggleUpdateState(): Boolean {
    if (isGpsEnabled) stopRequestingUpdates()
    else requestUpdates()
    return isGpsEnabled
  }


  // ------------------------ STARTING ------------------------

  private val activityResultLauncher =
    activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
      if (it == true) onRationaleAccepted()
      else onRationaleDismissed()
    }

  private fun startLocationUpdates() {
    if (isGranted(LOCATION_PERMISSION, activity)) onRationaleAccepted()
    else requestLocationPermission()
  }

  private fun requestLocationPermission() {
    PermissionUtil.RationaleDialogBuilder(LOCATION_PERMISSION, activity)
      .setTitle(activity.resources.getString(R.string.message_location_rationale_title))
      .setMessage(activity.resources.getString(R.string.app_name) + activity.resources.getString(R.string.message_location_rationale_body))
      .setActivityResultLauncher(activityResultLauncher)
      .buildAndRequest()
  }

  private fun onRationaleDismissed() {
    isGpsRequestedSharedPref = false
  }

  private fun onRationaleAccepted() {
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
    isGpsEnabled = true

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
    isGpsRequestedSharedPref = false
    stopRequestingUpdates()
  }


  // ------------------------ STOPPING ------------------------

  private fun requestLocationUpdatesStop() {
    isGpsEnabled = false

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

  interface GpsUtilListener {
    fun onGpsUpdate(address: Address?)
  }

  companion object {
    private const val TAG = "GGG GpsUtil"
    private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS =  TimeUnit.MINUTES.toMillis(30)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5)

    const val REQUEST_CODE_CHECK_SETTINGS = 101
  }
}