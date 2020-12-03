package com.gerardbradshaw.whetherweather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.whetherweather.BuildConfig
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.util.Constants
import com.gerardbradshaw.whetherweather.util.FetchAddressIntentService
import com.gerardbradshaw.whetherweather.util.PermissionUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.util.*

abstract class AbstractLocationActivity(
  val locationUpdateInterval: Long,
  val shortestLocationUpdateInterval: Long
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
  protected var currentPostcode: String? = null

  protected var shouldLoadTestLocations = true



  // ------------------------ INIT ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    addressResultReceiver = AddressResultReceiver(Handler())

    updateValuesFromBundle(savedInstanceState)

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    settingsClient = LocationServices.getSettingsClient(this)

    isRequestingLocationUpdates = true

    createLocationCallback()
    createLocationRequest()
    buildLocationSettingsRequest()
  }

  private fun createLocationCallback() {
    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult?) {
        super.onLocationResult(locationResult)
        currentLocation = locationResult?.lastLocation
        lastUpdateTime = Calendar.getInstance().timeInMillis

        if (!Geocoder.isPresent()) {
          toastLocationError("toastCurrentLocation: Geocoder unavailable")
          return
        }

        isAddressRequested = true
        if (isAddressRequested) startIntentService()
      }
    }
  }

  private fun createLocationRequest() {
    locationRequest = LocationRequest()
      .setInterval(locationUpdateInterval)
      .setFastestInterval(shortestLocationUpdateInterval)
      .setPriority(LocationRequest.PRIORITY_LOW_POWER)
  }

  private fun buildLocationSettingsRequest() {
    locationSettingsRequest = LocationSettingsRequest
      .Builder()
      .addLocationRequest(locationRequest)
      .build()
  }

  private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
    savedInstanceState ?: return

    Log.d(TAG, "updateValuesFromBundle: loading data from bundle")

    KEY_ADDRESS_REQUESTED.let {
      if (savedInstanceState.keySet().contains(it)) {
        isAddressRequested = savedInstanceState.getBoolean(it)
      }
    }

    KEY_LOCATION.let {
      if (savedInstanceState.keySet().contains(it)) {
        currentLocation = savedInstanceState.getParcelable(it)
      }
    }

    KEY_ADDRESS.let {
      if (savedInstanceState.keySet().contains(it)) {
        currentPostcode = savedInstanceState.getString(it)
        Log.d(TAG, "updateValuesFromBundle: location from savedInstanceState = $currentPostcode")
      }
    }

    KEY_REQUESTING_LOCATION_UPDATES.let {
      if (savedInstanceState.keySet().contains(it)) {
        isRequestingLocationUpdates = savedInstanceState.getBoolean((it))
      }
    }

    KEY_LAST_UPDATED_TIME.let {
      if (savedInstanceState.keySet().contains(it)) {
        lastUpdateTime = savedInstanceState.getLong(it)
      }
    }

    onCurrentLocationUpdate()
  }

  @SuppressLint("MissingPermission")
  override fun onResume() {
    super.onResume()

    val isPermissionGranted = PermissionUtil
      .isPermissionGranted(LOCATION_PERMISSION_NAME, this)

    if (isRequestingLocationUpdates && isPermissionGranted) startLocationUpdates()
    else if (!isPermissionGranted) {
      PermissionUtil
        .RequestBuilder(LOCATION_PERMISSION_NAME, this)
        .setPermissionRationaleDialogText(
          getString(R.string.app_name) + getString(R.string.location_rationale_message),
          getString(R.string.location_rationale_title),
          getString(R.string.ok),
          getString(R.string.not_now))
        .buildAndRequest()
    }

    onCurrentLocationUpdate()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_CODE_CHECK_SETTINGS -> {
        when (resultCode) {
          Activity.RESULT_OK -> return // Nothing to do - startLocationUpdates() called in onResume
          Activity.RESULT_CANCELED -> {
            toastLocationError("onActivityResult: User denied location permission")
            stopLocationUpdates()
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
    stopLocationUpdates()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    with(outState) {
      putBoolean(KEY_ADDRESS_REQUESTED, isAddressRequested)
      putParcelable(KEY_LOCATION, currentLocation)
      putString(KEY_ADDRESS, currentPostcode)
      putBoolean(KEY_REQUESTING_LOCATION_UPDATES, isRequestingLocationUpdates)
      if (lastUpdateTime != null) putLong(KEY_LAST_UPDATED_TIME, lastUpdateTime!!)
      super.onSaveInstanceState(this)
    }
  }

  private fun stopLocationUpdates() {
    if (!isRequestingLocationUpdates) {
      Log.d(TAG, "stopLocationUpdates: updates never requested. Nothing to stop!")
      return
    }

    fusedLocationClient
      .removeLocationUpdates(locationCallback)
      .addOnCompleteListener(this) {
        isRequestingLocationUpdates = false
      }

  }



  // ------------------------ UI ------------------------

  abstract fun onCurrentLocationUpdate()



  // ------------------------ PERMISSIONS ------------------------

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE) {
      PermissionUtil.onRequestPermissionResultHelper(requestCode, grantResults, ::startLocationUpdates)
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }



  // ------------------------ LOCATION & ADDRESS ------------------------

  @RequiresPermission(LOCATION_PERMISSION_NAME)
  private fun startLocationUpdates() {
    Log.d(TAG, "startLocationUpdates: starting location updates")

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
            stopLocationUpdates()
          }
        }
      }
  }

  private fun startIntentService() {
    val intent = Intent(this, FetchAddressIntentService::class.java).apply {
      putExtra(Constants.RECEIVER, addressResultReceiver)
      putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation)
    }
    if (isAddressRequested) startService(intent)
  }

  private inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
      currentPostcode = resultData?.getString(Constants.RESULT_DATA_KEY)
      onCurrentLocationUpdate()

      if (resultCode == Constants.SUCCESS_RESULT) Log.d(TAG, "onReceiveResult: Address found")
      isAddressRequested = false
    }
  }



  // ------------------------ UTIL ------------------------

  protected fun showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
  }

  protected fun toastLocationError(logMsg: String? = null) {
    showToast(getString(R.string.message_location_error))
    if (logMsg != null) Log.e(TAG, logMsg)
  }

  companion object {
    private const val TAG = "AbstractLocationActivit"
    private const val LOCATION_PERMISSION_NAME = Manifest.permission.ACCESS_COARSE_LOCATION

    private const val REQUEST_CODE_CHECK_SETTINGS = 101

    private const val KEY_ADDRESS_REQUESTED = "address-request-pending"
    private const val KEY_LOCATION = "location-parcelable"
    private const val KEY_ADDRESS = "location-address"
    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
    private const val KEY_LAST_UPDATED_TIME = "last-updated-time"

    const val RESULT_SUCCESS = 0
    const val RESULT_FAILURE = 1
  }
}