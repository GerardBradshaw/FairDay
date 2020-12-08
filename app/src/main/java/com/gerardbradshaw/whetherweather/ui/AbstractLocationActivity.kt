package com.gerardbradshaw.whetherweather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.util.Constants
import com.gerardbradshaw.whetherweather.util.FetchAddressIntentService
import com.gerardbradshaw.whetherweather.util.PermissionUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.*

abstract class AbstractLocationActivity(
  private val locationUpdateIntervalInMs: Long,
  private val shortestLocationUpdateIntervalInMs: Long
  ) : AppCompatActivity() {

  private lateinit var addressResultReceiver: AddressResultReceiver
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var settingsClient: SettingsClient
  private lateinit var locationRequest: LocationRequest
  private lateinit var locationSettingsRequest: LocationSettingsRequest

  private lateinit var locationCallback: LocationCallback
  protected var currentLocation: Location? = null
  private var isRequestingLocationUpdates = false
  private var isAddressRequested = false
  protected var lastUpdateTime: Long? = null
  protected var currentAddress: Address? = null

  private lateinit var requestPermission: ActivityResultLauncher<String?>



  // ------------------------ INIT ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    addressResultReceiver = AddressResultReceiver(Handler())

    updateValuesFromBundle(savedInstanceState)

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    settingsClient = LocationServices.getSettingsClient(this)

    requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
      when (it) {
        true -> {
          Log.d(TAG, "onRequestPermissionsResult: user granted permission")
          requestLocationUpdates()
        }
        false -> Log.d(TAG, "onRequestPermissionsResult: user denied permission")
        else -> Log.d(TAG, "onRequestPermissionsResult: user ignored permission")
      }
    }

    createLocationCallback()
    createLocationRequest()
    buildLocationSettingsRequest()
  }

  /**
   * Updates fields based on data stored [savedInstanceState]
   */
  private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
    savedInstanceState ?: return
    Log.d(TAG, "updateValuesFromBundle: loading data from bundle")

    KEY_REQUESTING_LOCATION_UPDATES.let {
      if (savedInstanceState.keySet().contains(it)) {
        isRequestingLocationUpdates = savedInstanceState.getBoolean((it))
      }
    }

    KEY_LOCATION.let {
      if (savedInstanceState.keySet().contains(it)) {
        currentLocation = savedInstanceState.getParcelable(it)
      }
    }

    KEY_LAST_UPDATED_TIME.let {
      if (savedInstanceState.keySet().contains(it)) {
        lastUpdateTime = savedInstanceState.getLong(it)
      }
    }

    KEY_ADDRESS_REQUESTED.let {
      if (savedInstanceState.keySet().contains(it)) {
        isAddressRequested = savedInstanceState.getBoolean(it)
      }
    }

    KEY_ADDRESS.let {
      if (savedInstanceState.keySet().contains(it)) {
        currentAddress = savedInstanceState.getParcelable(it)
      }
    }

    onCurrentLocationUpdate()
  }

  /**
   * Creates a callback for receiving location events.
   */
  private fun createLocationCallback() {
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult?) {
        super.onLocationResult(locationResult)

        currentLocation = locationResult?.lastLocation
        lastUpdateTime = currentLocation?.time

        val cal = Calendar.getInstance().also { it.timeInMillis = lastUpdateTime ?: 0 }
        val date = "${cal.get(Calendar.YEAR)}.${cal.get(Calendar.MONTH) + 1}.${cal.get(Calendar.DAY_OF_MONTH)}"
        val time = "${cal.get(Calendar.HOUR_OF_DAY)}:${cal.get(Calendar.MINUTE)}"
        Log.d(TAG, "onLocationResult: location updated $date, $time")

        if (!Geocoder.isPresent()) {
          toastLocationError("toastCurrentLocation: Geocoder unavailable")
          return
        } else {
          if (currentLocation != null) {
            isAddressRequested = true
            startIntentService()
          }
        }
      }
    }
  }

  /**
   * Sets up the location request.
   */
  private fun createLocationRequest() {
    locationRequest = LocationRequest()
      .setInterval(locationUpdateIntervalInMs)
      .setFastestInterval(shortestLocationUpdateIntervalInMs)
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

  override fun onResume() {
    super.onResume()

    if (isRequestingLocationUpdates) {
      val isPermissionGranted = PermissionUtil.isGranted(LOCATION_PERMISSION_NAME, this)

      if (isPermissionGranted) startLocationUpdates()
      else requestPermissionsAndStartLocationUpdates()

      onCurrentLocationUpdate()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_CODE_CHECK_SETTINGS -> {
        when (resultCode) {
          Activity.RESULT_OK -> return // Nothing to do - startLocationUpdates() called in onResume
          Activity.RESULT_CANCELED -> {
            toastLocationError("onActivityResult: User denied location permission")
            stopRequestingLocationUpdates()
            onCurrentLocationUpdate()
          }
          else -> super.onActivityResult(requestCode, resultCode, data)
        }
      }
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }



  // ------------------------ FINALIZATION ------------------------

  override fun onPause() {
    super.onPause()
    stopRequestingLocationUpdates()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    with(outState) {
      putBoolean(KEY_ADDRESS_REQUESTED, isAddressRequested)
      putParcelable(KEY_LOCATION, currentLocation)
      putParcelable(KEY_ADDRESS, currentAddress)
      putBoolean(KEY_REQUESTING_LOCATION_UPDATES, isRequestingLocationUpdates)
      if (lastUpdateTime != null) putLong(KEY_LAST_UPDATED_TIME, lastUpdateTime!!)
      super.onSaveInstanceState(this)
    }
  }

  /**
   * Removes location updates from the FusedLocationApi.
   */
  private fun stopRequestingLocationUpdates() {
    if (!isRequestingLocationUpdates) {
      Log.d(TAG, "stopLocationUpdates: updates never requested. Nothing to stop!")
      return
    }

    fusedLocationClient
      .removeLocationUpdates(locationCallback)
      .addOnCompleteListener(this) {
        isRequestingLocationUpdates = false
      }

    Log.d(TAG, "stopLocationUpdates: updates stopped")
  }



  // ------------------------ UI ------------------------

  abstract fun onCurrentLocationUpdate()



  // ------------------------ PERMISSIONS ------------------------

  private fun requestPermissionsAndStartLocationUpdates() {
    PermissionUtil
      .RequestBuilder(LOCATION_PERMISSION_NAME, this)
      .setRationaleDialogTitle(getString(R.string.location_rationale_title))
      .setRationaleDialogMessage(getString(R.string.app_name) + getString(R.string.location_rationale_message))
      .setActivityResultLauncher(requestPermission)
      .setOnPermissionGranted { requestLocationUpdates() }
      .buildAndRequest()
  }



  // ------------------------ LOCATION ------------------------

  fun startLocationUpdates() {
    val isPermissionGranted = PermissionUtil.isGranted(LOCATION_PERMISSION_NAME, this)

    if (isPermissionGranted) requestLocationUpdates()
    else requestPermissionsAndStartLocationUpdates()
  }

  fun stopLocationUpdates() {
    if (isRequestingLocationUpdates) stopRequestingLocationUpdates()
  }

  @SuppressLint("MissingPermission") // permission is checked in onCreate()
  protected fun requestLocationUpdates() {
    Log.d(TAG, "startLocationUpdates: starting location updates")

    isRequestingLocationUpdates = true

    settingsClient.checkLocationSettings(locationSettingsRequest)
      .addOnSuccessListener(this) {
        Log.d(TAG, "startLocationUpdates: All location settings are satisfied.")

        fusedLocationClient.requestLocationUpdates(
          locationRequest,
          locationCallback,
          Looper.myLooper())
      }
      .addOnFailureListener(this) { e ->
        when ((e as ApiException).statusCode) {

          LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
            Log.d(TAG, "startLocationUpdates: attempting to resolve settings")
            try {
              val rae = e as ResolvableApiException
              rae.startResolutionForResult(this, REQUEST_CODE_CHECK_SETTINGS)
            } catch (sie: IntentSender.SendIntentException) {
              Log.d(TAG, "startLocationUpdates: PendingIntent unable to execute request.")
            }
          }

          LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
            toastLocationError("checkSettingsClient: location settings change unavailable")
            stopRequestingLocationUpdates()
          }
        }
      }
  }



  // ------------------------ ADDRESS ------------------------

  private fun startIntentService() {
    val intent = Intent(this, FetchAddressIntentService::class.java).apply {
      putExtra(Constants.RECEIVER, addressResultReceiver)
      putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation)
    }
    if (isAddressRequested) startService(intent)
  }

  private inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
      currentAddress = resultData?.getParcelable(Constants.RESULT_DATA_KEY)
      onCurrentLocationUpdate()

      if (resultCode == Constants.SUCCESS_RESULT) Log.d(TAG, "onReceiveResult: Address found")
      isAddressRequested = false
    }
  }



  // ------------------------ UTIL ------------------------

  protected fun showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
  }

  protected fun toastLocationError(logMsg: String? = null) {
    showToast(getString(R.string.message_location_error))
    if (logMsg != null) Log.e(TAG, logMsg)
  }

  companion object {
    private const val TAG = "AbstractLocationActivit"
    private const val LOCATION_PERMISSION_NAME = Manifest.permission.ACCESS_COARSE_LOCATION

    private const val REQUEST_CODE_CHECK_SETTINGS = 101

    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
    private const val KEY_LOCATION = "location-parcelable"
    private const val KEY_LAST_UPDATED_TIME = "last-updated-time"
    private const val KEY_ADDRESS_REQUESTED = "address-request-pending"
    private const val KEY_ADDRESS = "location-address"

    const val RESULT_SUCCESS = 0
    const val RESULT_FAILURE = 1
  }
}