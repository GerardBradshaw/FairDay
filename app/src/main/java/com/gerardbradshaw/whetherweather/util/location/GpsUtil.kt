package com.gerardbradshaw.whetherweather.util.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.util.address.AddressUtil
import com.gerardbradshaw.whetherweather.util.permissions.PermissionUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Utility for requesting device location updates.
 *
 * @param activity the activity to register for location updates.
 */
class GpsUtil(private val activity: AppCompatActivity) {

  private var settingsClient: SettingsClient =
      LocationServices.getSettingsClient(activity)
  private var fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(activity)

  private lateinit var locationCallback: LocationCallback
  private lateinit var locationRequest: LocationRequest
  private lateinit var locationSettingsRequest: LocationSettingsRequest

  private val addressUtil = AddressUtil()
  private var listener: LocationUpdateListener? = null

  private var isRequestingGpsUpdates = false
  private var isAddressRequested = false

  private var updateTime: Long? = null
//  private var location: Location? = null
  private var address: Address? = null

  private val addressChangeListener: AddressUtil.AddressChangeListener =
      object : AddressUtil.AddressChangeListener {
        override fun onAddressChanged(address: Address?) {
          listener?.onAddressUpdate(address)
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
          Log.d(TAG, "onLocationResult: ERROR: locationResult is null")
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
   * To be called by a registered [LocationUpdateListener] to start location updates if it was 
   * previously set to receive them. Method is named to indicate its logical position in an
   * [Activity] listener class.
   */
  fun onResume() {
    if (isRequestingGpsUpdates) startLocationUpdates()
  }

  /**
   * To be called by a registered [LocationUpdateListener] to stop requesting location updates.
   * Method is named to indicate its logical position in an [Activity] listener class.
   */
  fun onPause() {
    stopRequestingLocationUpdates()
  }

  /**
   * To be called by a registered [LocationUpdateListener] to handle Android settings changes
   * related to permissions.
   *
   * [requestCode], [resultCode], [data] are as per [Activity.onActivityResult].
   */
  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CODE_CHECK_SETTINGS) {
      when (resultCode) {
        Activity.RESULT_OK -> {
          Log.d(TAG, "onActivityResult: Settings change successful!")
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
   * Saved fields so they can be quickly reloaded when the activity is restarted.
   */
  fun onSaveInstanceState(outState: Bundle) {
    with(outState) {
      putBoolean(KEY_ADDRESS_REQUESTED, isAddressRequested)
//      putParcelable(KEY_LOCATION, location)
      putParcelable(KEY_ADDRESS, address)
      putBoolean(KEY_REQUESTING_LOCATION_UPDATES, isRequestingGpsUpdates)
      if (updateTime != null) putLong(KEY_LAST_UPDATED_TIME, updateTime!!)
    }
  }

  /**
   * Updates fields based on data stored [savedInstanceState]
   */
  fun onLoadInstanceState(savedInstanceState: Bundle?) {
    savedInstanceState ?: return
    Log.d(TAG, "onLoadInstanceState: loading data from bundle")

    KEY_REQUESTING_LOCATION_UPDATES.let {
      if (savedInstanceState.keySet().contains(it)) {
        isRequestingGpsUpdates = savedInstanceState.getBoolean((it))
      }
    }

//    KEY_LOCATION.let {
//      if (savedInstanceState.keySet().contains(it)) {
//        location = savedInstanceState.getParcelable(it)
//      }
//    }

    KEY_LAST_UPDATED_TIME.let {
      if (savedInstanceState.keySet().contains(it)) {
        updateTime = savedInstanceState.getLong(it)
      }
    }

    KEY_ADDRESS_REQUESTED.let {
      if (savedInstanceState.keySet().contains(it)) {
        isAddressRequested = savedInstanceState.getBoolean(it)
      }
    }

    KEY_ADDRESS.let {
      if (savedInstanceState.keySet().contains(it)) {
        address = savedInstanceState.getParcelable(it)
      }
    }

    listener?.onAddressUpdate(address)
  }

  /**
   * Sets the [LocationUpdateListener] which will hear the location updates.
   */
  fun setOnLocationUpdateListener(listener: LocationUpdateListener) {
    this.listener = listener
  }


  // ------------------------ LOCATION ------------------------

  private fun startLocationUpdates() {
    val isLocationPermissionGranted = PermissionUtil.isGranted(LOCATION_PERMISSION, activity)

    if (!isLocationPermissionGranted) requestLocationPermissions()
    else requestGpsUpdates()
  }

  private fun requestLocationPermissions() {
    val activityResultLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
          when (it) {
            true -> requestGpsUpdates()
            false -> Log.d(TAG, "onRequestPermissionsResult: ERROR: user denied permission")
            else -> Log.d(TAG, "onRequestPermissionsResult: ERROR: user ignored permission")
          }
        }

    PermissionUtil.RequestBuilder(LOCATION_PERMISSION, activity)
        .setRationaleDialogTitle(activity.resources.getString(R.string.message_location_rationale_title))
        .setRationaleDialogMessage(activity.resources.getString(R.string.app_name) + activity.resources.getString(R.string.message_location_rationale_body))
        .setActivityResultLauncher(activityResultLauncher)
        .setOnPermissionGranted { requestGpsUpdates() }
        .buildAndRequest()
  }

  @SuppressLint("MissingPermission") // permission is checked in onCreate()
  private fun requestGpsUpdates()
  {
    Log.d(TAG, "requestGpsUpdates: starting location updates")
    isRequestingGpsUpdates = true

    settingsClient
        .checkLocationSettings(locationSettingsRequest)
        .addOnSuccessListener(activity) { onLocationSettingsSatisfied() }
        .addOnFailureListener(activity) { e -> onLocationSettingsNotSatisfied(e) }
  }
  
  @RequiresPermission(LOCATION_PERMISSION)
  private fun onLocationSettingsSatisfied() {
    Log.d(TAG, "requestGpsUpdates: All settings satisfied. Starting updates.")

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.myLooper())
  }

  private fun onLocationSettingsNotSatisfied(e: Exception) {
    when ((e as ApiException).statusCode) {

      LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
        Log.d(TAG, "requestGpsUpdates: attempting to resolve settings")
        try {
          val rae = e as ResolvableApiException
          rae.startResolutionForResult(activity, REQUEST_CODE_CHECK_SETTINGS)
        } catch (sie: IntentSender.SendIntentException) {
          toastLocationErrorAndLog("requestGpsUpdates: ERROR: PendingIntent can't execute request.")
        }
      }

      LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
        toastLocationErrorAndLog("requestGpsUpdates: ERROR: location settings change unavailable")
        stopRequestingLocationUpdates()
      }
    }
  }

  /**
   * Removes location updates from the FusedLocationApi.
   */
  private fun stopRequestingLocationUpdates() {
    if (!isRequestingGpsUpdates) {
      Log.d(TAG, "stopLocationUpdates: updates never requested. Nothing to stop!")
      return
    }
    isRequestingGpsUpdates = false
    
    fusedLocationClient
        .removeLocationUpdates(locationCallback)
        .addOnCompleteListener(activity) { onLocationUpdateRequestsStopped() }
  }
  
  private fun onLocationUpdateRequestsStopped() {
    Log.d(TAG, "stopRequestingLocationUpdates: updates stopped")
  }

  private fun toastLocationErrorAndLog(logMsg: String) {
    Log.d(TAG, logMsg)

    Toast.makeText(
        activity,
        activity.resources.getString(R.string.string_unable_to_determine_location),
        Toast.LENGTH_SHORT).show()
  }


  // ------------------------ UTIL ------------------------

  interface LocationUpdateListener {
    fun onAddressUpdate(address: Address?)
  }

  companion object {
    private const val TAG = "GGG GpsUtil"
    private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    
    private val DEFAULT_LOCATION_UPDATE_INTERVAL_MS =  TimeUnit.MINUTES.toMillis(30)
    private val FASTEST_LOCATION_UPDATE_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30)

    const val REQUEST_CODE_CHECK_SETTINGS = 101

    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
//    private const val KEY_LOCATION = "location-parcelable"
    private const val KEY_LAST_UPDATED_TIME = "last-updated-time"
    private const val KEY_ADDRESS_REQUESTED = "address-request-pending"
    private const val KEY_ADDRESS = "location-address"

    const val RESULT_SUCCESS = 0
    const val RESULT_FAILURE = 1
  }
}