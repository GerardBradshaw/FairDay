package com.gerardbradshaw.whetherweather.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.BuildConfig
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.retrofit.WeatherFile
import com.gerardbradshaw.whetherweather.room.LocationData
import com.gerardbradshaw.whetherweather.ui.LocationListAdapter
import com.gerardbradshaw.whetherweather.ui.MainActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
class LocationProvider(
  private val activity: Activity,
  private val listener: OnLocationUpdateListener
  ) {
  private var addressResultReceiver: AddressResultReceiver
  private var fusedLocationClient: FusedLocationProviderClient
  private var settingsClient: SettingsClient
  private lateinit var locationRequest: LocationRequest
  private lateinit var locationSettingsRequest: LocationSettingsRequest

  private lateinit var locationCallback: LocationCallback
  private var currentLocation: Location? = null
  private var isRequestingLocationUpdates = false
  private var isAddressRequested = false
  private var lastUpdateTime: Long? = null
  private var currentPostcode: String? = null



  // ------------------------ INIT ------------------------

  init {
    addressResultReceiver = AddressResultReceiver(Handler())
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    settingsClient = LocationServices.getSettingsClient(activity)

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
          toastLocationError("toastCurrentLocation: No geocoder available")
          return
        }

        isAddressRequested = true
        if (isAddressRequested) startIntentService()
      }
    }
  }

  private fun createLocationRequest() {
    locationRequest = LocationRequest()
      .setInterval(UPDATE_INTERVAL_IN_MS)
      .setFastestInterval(UPDATE_INTERVAL_FASTEST_IN_MS)
      .setPriority(LocationRequest.PRIORITY_LOW_POWER)
  }

  private fun buildLocationSettingsRequest() {
    locationSettingsRequest = LocationSettingsRequest
      .Builder()
      .addLocationRequest(locationRequest)
      .build()
  }

  fun onResume() {
    val isPermissionGranted = isLocationPermissionGranted()

    if (isRequestingLocationUpdates && isPermissionGranted) startLocationUpdates()
    else if (!isPermissionGranted) requestPermissions()
  }

  fun onRequestCodeCheckSettingsActivityResult(resultCode: Int, data: Intent?) {
    when (resultCode) {
      Activity.RESULT_OK -> return // Nothing to do - startLocationUpdates() called in onResume
      Activity.RESULT_CANCELED -> {
        toastLocationError("onActivityResult: User denied location permission")
        stopLocationUpdates()
        listener.onLocationUpdate(Constants.PERMISSIONS_ERROR_RESULT, null, null)
      }
    }
  }



  // ------------------------ FINALIZATION ------------------------

  fun onPause() {
    stopLocationUpdates()
  }

  private fun stopLocationUpdates() {
    if (!isRequestingLocationUpdates) {
      Log.d(TAG, "stopLocationUpdates: updates never requested. Nothing to stop!")
      return
    }

    fusedLocationClient
      .removeLocationUpdates(locationCallback)
      .addOnCompleteListener(activity) {
        isRequestingLocationUpdates = false
      }
  }



  // ------------------------ PERMISSIONS ------------------------

  private fun requestPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!isLocationPermissionGranted()) {
        val shouldShowRationale = ActivityCompat
          .shouldShowRequestPermissionRationale(activity, LOCATION_PERMISSION_NAME)

        if (shouldShowRationale) showLocationPermissionRationale()
        else showSystemPermissionsRequest()
      }
    }
  }

  private fun isLocationPermissionGranted(): Boolean {
    val state = ContextCompat.checkSelfPermission(activity, LOCATION_PERMISSION_NAME)
    return state == PackageManager.PERMISSION_GRANTED
  }

  private fun showLocationPermissionRationale() {
    AlertDialog.Builder(activity)
      .setMessage(activity.getString(R.string.app_name) + activity.getString(R.string.location_rationale_message))
      .setTitle(activity.getString(R.string.location_rationale_title))
      .setPositiveButton(activity.getString(R.string.ok)) { _, _ -> showSystemPermissionsRequest() }
      .setNegativeButton(activity.getString(R.string.not_now)) { _, _ -> }
      .create()
      .show()
  }

  private fun showSystemPermissionsRequest() {
    ActivityCompat.requestPermissions(
      activity,
      arrayOf(LOCATION_PERMISSION_NAME),
      REQUEST_CODE_PERMISSIONS
    )
  }

  fun onLocationRequestPermissionResult(grantResults: IntArray) {
    when {
      grantResults.isEmpty() -> toastLocationError("onRequestPermissionsResult: user cancelled")
      grantResults[0] == PackageManager.PERMISSION_GRANTED -> startLocationUpdates()
      else -> toastLocationError("onRequestPermissionsResult: user denied permission")
    }
  }



  // ------------------------ LOCATION & ADDRESS ------------------------

  fun requestLocationUpdates() {
    isRequestingLocationUpdates = true
    isAddressRequested = false
  }

  fun stopLocationUpdates2() {
    isRequestingLocationUpdates = false
    isAddressRequested = false
  }

  private fun startLocationUpdates() {
    Log.d(TAG, "startLocationUpdates: starting location updates")

    settingsClient.checkLocationSettings(locationSettingsRequest)
      .addOnSuccessListener(activity) {
        Log.i(TAG, "startLocationUpdates: All location settings are satisfied.")

        fusedLocationClient.requestLocationUpdates(
          locationRequest,
          locationCallback,
          Looper.myLooper())
      }
      .addOnFailureListener(activity) { e ->
        when ((e as ApiException).statusCode) {

          LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
            Log.i(TAG, "startLocationUpdates: attempting to resolve settings")
            try {
              val rae = e as ResolvableApiException
              rae.startResolutionForResult(activity, REQUEST_CODE_CHECK_SETTINGS)
            } catch (sie: IntentSender.SendIntentException) {
              Log.i(TAG, "startLocationUpdates: PendingIntent unable to execute request.")
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
    val intent = Intent(activity, FetchAddressIntentService::class.java).apply {
      putExtra(Constants.RECEIVER, addressResultReceiver)
      putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation)
    }
    if (isAddressRequested) activity.startService(intent)
  }

  private inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
      currentPostcode = resultData?.getString(Constants.RESULT_DATA_KEY)
      listener.onLocationUpdate(resultCode, currentLocation, currentPostcode)

      if (resultCode == Constants.SUCCESS_RESULT) Log.d(TAG, "onReceiveResult: Address found")
      isAddressRequested = false
    }
  }



  // ------------------------ UTIL ------------------------

  private fun showToast(msg: String) {
    Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
  }

  private fun toastLocationError(logMsg: String? = null) {
    showToast(activity.getString(R.string.message_location_error))
    if (logMsg != null) Log.e(TAG, logMsg)
  }

  interface OnLocationUpdateListener {
    fun onLocationUpdate(resultCode: Int, location: Location?, postCode: String?)
  }

  companion object {
    private const val TAG = "LocationProvider"
    private const val LOCATION_PERMISSION_NAME = Manifest.permission.ACCESS_COARSE_LOCATION

    private val UPDATE_INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(30)
    private val UPDATE_INTERVAL_FASTEST_IN_MS = TimeUnit.MINUTES.toMillis(5)
    private const val REQUEST_CODE_CHECK_SETTINGS = 101
    private const val REQUEST_CODE_PERMISSIONS = 37
  }
}