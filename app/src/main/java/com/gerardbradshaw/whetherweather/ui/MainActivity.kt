package com.gerardbradshaw.whetherweather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.BuildConfig
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.retrofit.WeatherFile
import com.gerardbradshaw.whetherweather.room.LocationData
import com.gerardbradshaw.whetherweather.util.Constants
import com.gerardbradshaw.whetherweather.util.FetchAddressIntentService
import com.gerardbradshaw.whetherweather.util.WeatherData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
  private lateinit var viewModel: MainViewModel
  private lateinit var viewPager: ViewPager2

  private lateinit var addressResultReceiver: AddressResultReceiver
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var settingsClient: SettingsClient
  private lateinit var locationRequest: LocationRequest
  private lateinit var locationSettingsRequest: LocationSettingsRequest

  private lateinit var locationCallback: LocationCallback
  private var currentLocation: Location? = null
  private var isRequestingLocationUpdates = false
  private var isAddressRequested = false
  private var lastUpdateTime: Long? = null
  private var currentAddress: String? = null

  private val addTestLocations = true



  // ------------------------ INIT ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    supportActionBar?.setDisplayShowTitleEnabled(false)
    viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

    addressResultReceiver = AddressResultReceiver(Handler())

    updateValuesFromBundle(savedInstanceState)

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    settingsClient = LocationServices.getSettingsClient(this)

    initViewPager()

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
        currentAddress = savedInstanceState.getString(it)
        Log.d(TAG, "updateValuesFromBundle: location from savedInstanceState = $currentAddress")
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

    updateUi()
  }

  @SuppressLint("MissingPermission")
  override fun onResume() {
    super.onResume()

    val isPermissionGranted = isLocationPermissionGranted()

    if (isRequestingLocationUpdates && isPermissionGranted) startLocationUpdates()
    else if (!isPermissionGranted) requestPermissions()

    updateUi()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_locations_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    showToast("Not implemented")
    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_CODE_CHECK_SETTINGS -> {
        when (resultCode) {
          Activity.RESULT_OK -> return // Nothing to do - startLocationUpdates() called in onResume
          Activity.RESULT_CANCELED -> {
            toastLocationError("onActivityResult: User denied location permission")
            stopLocationUpdates()
            updateUi()
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
      putString(KEY_ADDRESS, currentAddress)
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

  private fun updateUi() {
    if (currentLocation != null || currentAddress != null) updateLocationUi()
  }

  private fun updateLocationUi() {
    AlertDialog.Builder(this)
      .setMessage("Lat: ${currentLocation?.latitude}\nLong: ${currentLocation?.longitude}\nAddress: $currentAddress\n")
      .setTitle("Location information")
      .create()
      .show()
  }



  // ------------------------ PERMISSIONS ------------------------

  private fun requestPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!isLocationPermissionGranted()) {
        val shouldShowRationale = ActivityCompat
          .shouldShowRequestPermissionRationale(this, LOCATION_PERMISSION_NAME)

        if (shouldShowRationale) showLocationPermissionRationale()
        else showSystemPermissionsRequest()
      }
    }
  }

  private fun isLocationPermissionGranted(): Boolean {
    val state = ContextCompat.checkSelfPermission(this, LOCATION_PERMISSION_NAME)
    return state == PackageManager.PERMISSION_GRANTED
  }

  private fun showLocationPermissionRationale() {
    AlertDialog.Builder(this)
      .setMessage(getString(R.string.app_name) + getString(R.string.location_rationale_message))
      .setTitle(getString(R.string.location_rationale_title))
      .setPositiveButton(getString(R.string.ok)) { _, _ -> showSystemPermissionsRequest() } //requestPermissionLauncher.launch(LOCATION_PERMISSION) }
      .setNegativeButton(getString(R.string.not_now)) { _, _ -> }
      .create()
      .show()
  }

  private fun showSystemPermissionsRequest() {
    ActivityCompat.requestPermissions(
      this,
      arrayOf(LOCATION_PERMISSION_NAME),
      REQUEST_CODE_PERMISSIONS)
  }

  @SuppressLint("MissingPermission")
  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode != REQUEST_CODE_PERMISSIONS) return

    when {
      grantResults.isEmpty() -> toastLocationError("onRequestPermissionsResult: user cancelled")
      grantResults[0] == PackageManager.PERMISSION_GRANTED -> startLocationUpdates() //getAddress()
      else -> toastLocationError("onRequestPermissionsResult: user denied permission")
    }

    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }



  // ------------------------ LOCATION & ADDRESS ------------------------

  @RequiresPermission(LOCATION_PERMISSION_NAME)
  private fun startLocationUpdates() {
    Log.d(TAG, "startLocationUpdates: starting location updates")

    settingsClient.checkLocationSettings(locationSettingsRequest)
      .addOnSuccessListener(this) {
        Log.i(TAG, "startLocationUpdates: All location settings are satisfied.")

        fusedLocationClient.requestLocationUpdates(
          locationRequest,
          locationCallback,
          Looper.myLooper())
      }
      .addOnFailureListener(this) { e ->
        when ((e as ApiException).statusCode) {

          LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
            Log.i(TAG, "startLocationUpdates: attempting to resolve settings")
            try {
              val rae = e as ResolvableApiException
              rae.startResolutionForResult(this, REQUEST_CODE_CHECK_SETTINGS)
            } catch (sie: SendIntentException) {
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
    val intent = Intent(this, FetchAddressIntentService::class.java).apply {
      putExtra(Constants.RECEIVER, addressResultReceiver)
      putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation)
    }
    if (isAddressRequested) startService(intent)
  }

  private inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
      currentAddress = resultData?.getString(Constants.RESULT_DATA_KEY)
      updateUi()

      if (resultCode == Constants.SUCCESS_RESULT) Log.d(TAG, "onReceiveResult: Address found")
      isAddressRequested = false
    }
  }



  // ------------------------ VIEW PAGER ------------------------

  private fun initViewPager() {
    viewPager = findViewById(R.id.view_pager)
    val adapter = LocationListAdapter(this)
    viewPager.adapter = adapter

    viewModel.locationDataSet.observe(this, Observer { locations ->
      adapter.dataSet = locations
    })

    if (addTestLocations) {
      val testLocations = arrayOf("San Francisco", "London", "New York")
      for (locationName in testLocations) getWeatherFor(locationName)
    }
  }

  private fun getWeatherFor(name: String) {
    val params = HashMap<String, String>()
    params["q"] = name
    params["appId"] = API_KEY_OPEN_WEATHER

    val openWeatherApi = (application as BaseApplication).openWeatherApi

    val call = openWeatherApi.getWeather(params)

    call.enqueue(object : Callback<WeatherFile> {
      override fun onFailure(call: Call<WeatherFile>, t: Throwable) {
        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
      }

      override fun onResponse(call: Call<WeatherFile>, response: Response<WeatherFile>) {
        if (!response.isSuccessful) {
          val msg = "Code: ${response.code()}"
          Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
          return
        }

        val weatherFile = response.body()

        if (weatherFile == null) Toast.makeText(
          applicationContext,
          getString(R.string.message_no_data),
          Toast.LENGTH_LONG
        ).show()
        else {
          val weatherData = WeatherData(weatherFile)

          val locationData = LocationData(
            location = weatherData.location ?: "Unknown location",
            time = weatherData.timeUpdated ?: 0, // TODO update this to current time
            condition = weatherData.condition ?: "",
            conditionIconId = weatherData.conditionIconId ?: "", // TODO update this to an error value
            description = weatherData.description ?: "", // TODO update this to an error value
            temp = weatherData.temp ?: Int.MAX_VALUE, // TODO update this to an error value
            min = weatherData.min ?: Int.MIN_VALUE, // TODO update this to an error value
            max = weatherData.max ?: Int.MAX_VALUE // TODO update this to an error value
          )

          viewModel.insertLocationData(locationData)
        }
      }
    })
  }



  // ------------------------ UTIL ------------------------

  private fun showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
  }

  private fun toastLocationError(logMsg: String? = null) {
    showToast(getString(R.string.message_location_error))
    if (logMsg != null) Log.e(TAG, logMsg)
  }

  companion object {
    private const val TAG = "MainActivity"
    private const val LOCATION_PERMISSION_NAME = Manifest.permission.ACCESS_COARSE_LOCATION

    private val UPDATE_INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(30)
    private val UPDATE_INTERVAL_FASTEST_IN_MS = TimeUnit.MINUTES.toMillis(5)

    private const val API_KEY_OPEN_WEATHER = BuildConfig.OPEN_WEATHER_APP_KEY

    private const val REQUEST_CODE_CHECK_SETTINGS = 101
    private const val REQUEST_CODE_PERMISSIONS = 37

    private const val KEY_ADDRESS_REQUESTED = "address-request-pending"
    private const val KEY_LOCATION = "location-parcelable"
    private const val KEY_ADDRESS = "location-address"
    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
    private const val KEY_LAST_UPDATED_TIME = "last-updated-time"
  }
}