package com.gerardbradshaw.whetherweather.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.gerardbradshaw.whetherweather.util.WeatherData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
  private lateinit var viewModel: MainViewModel
  private lateinit var viewPager: ViewPager2
  private var currentLocation: String? = null


  // ------------------------ INIT ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    supportActionBar?.hide()
//    supportActionBar?.setDisplayShowTitleEnabled(false)

    viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

    initPermissions()
    initViewPager()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_locations_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    Log.d(TAG, "onOptionsItemSelected: ${item.itemId} clicked")
    return super.onOptionsItemSelected(item)
  }

  // -------- Permissions --------

  private fun initPermissions() {
    requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted: Boolean ->
      currentLocation =
        if (isPermissionGranted) "San Francisco" // TODO change to actual current location
        else null
    }
    requestLocationAccess()
  }

  private fun requestLocationAccess() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val permissionName = Manifest.permission.ACCESS_COARSE_LOCATION
      val permissionStatus = ContextCompat.checkSelfPermission(this, permissionName)
      val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)

      if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
        if (shouldShowRationale) showLocationPermissionRationale(permissionName)
        else requestPermissionLauncher.launch(permissionName)
      }
    }
  }

  private fun showLocationPermissionRationale(permissionName: String) {
    AlertDialog.Builder(this)
      .setMessage(getString(R.string.app_name) + getString(R.string.location_rationale_message))
      .setTitle(getString(R.string.location_rationale_title))
      .setPositiveButton(getString(R.string.location_rationale_positive)) { _, _ -> requestPermissionLauncher.launch(permissionName) }
      .setNegativeButton(getString(R.string.location_rationale_negative)) { _, _ -> }
      .create()
      .show()
  }

  // -------- ViewPager --------

  private fun initViewPager() {
    viewPager = findViewById(R.id.view_pager)
    val adapter = LocationListAdapter(this)
    viewPager.adapter = adapter

    viewModel.locationDataSet.observe(this, Observer { locations ->
      adapter.dataSet = locations
      Toast.makeText(this, "Updated", Toast.LENGTH_SHORT)
    })

    Log.d(TAG, "initViewPager: al g")

    // TEST LOCATIONS - TODO remove
    //val testLocations = arrayOf("San Francisco", "London", "New York")
    //for (locationName in testLocations) getWeatherFor(locationName)
  }

  private fun getWeatherFor(name: String) {
    val params = HashMap<String, String>()
    params["q"] = name
    params["appId"] = OPEN_WEATHER_APP_KEY

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

        if (weatherFile == null) Toast.makeText(applicationContext, getString(R.string.message_no_data), Toast.LENGTH_LONG).show()
        else {
          val weatherData = WeatherData(weatherFile)

          val locationData = LocationData(
            location = weatherData.location ?: "Unknown location",
            time = weatherData.timeUpdated ?: 0, // TODO update this to current time
            condition = weatherData.condition ?: "",
            description = weatherData.description ?: "",
            temp = weatherData.temp ?: Int.MAX_VALUE, // TODO update this to an error value
            min = weatherData.min ?: Int.MIN_VALUE, // TODO update this to an error value
            max = weatherData.max ?: Int.MAX_VALUE // TODO update this to an error value
          )

          viewModel.insertLocationData(locationData)
        }
      }
    })
  }

  companion object {
    private const val OPEN_WEATHER_APP_KEY = BuildConfig.OPEN_WEATHER_APP_KEY
    private const val TAG = "MainActivity"
  }
}