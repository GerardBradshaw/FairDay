package com.gerardbradshaw.whetherweather.ui

import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
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
import java.util.concurrent.TimeUnit

class MainActivity : AbstractLocationActivity(UPDATE_INTERVAL_IN_MS, UPDATE_INTERVAL_FASTEST_IN_MS){
  private lateinit var viewModel: MainViewModel
  private lateinit var viewPager: ViewPager2


  // ------------------------ INIT ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    supportActionBar?.setDisplayShowTitleEnabled(false)
    viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

    initViewPager()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_locations_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    showToast("Not implemented")
    return super.onOptionsItemSelected(item)
  }



  // ------------------------ UI ------------------------

  override fun onCurrentLocationUpdate() {
    Log.d(TAG, "onCurrentLocationUpdate: location updated. Current location = $currentLocation, current postcode = $currentPostcode")
    if (currentLocation != null || currentPostcode != null) updateLocationUi()
  }

  private fun updateLocationUi() {
    AlertDialog.Builder(this)
      .setMessage("Lat: ${currentLocation?.latitude}\nLong: ${currentLocation?.longitude}\nAddress: $currentPostcode\n")
      .setTitle("Location information")
      .create()
      .show()
  }



  // ------------------------ VIEW PAGER ------------------------

  private fun initViewPager() {
    viewPager = findViewById(R.id.view_pager)
    val adapter = LocationListAdapter(this)
    viewPager.adapter = adapter

    viewModel.locationDataSet.observe(this, Observer { locations ->
      adapter.dataSet = locations
    })

    if (shouldLoadTestLocations) {
      val testLocations = arrayOf("San Francisco", "London", "New York")
      for (locationName in testLocations) requestWeatherFor(locationName)
    }
  }

  private fun requestWeatherFor(name: String) {
    val params = HashMap<String, String>()
    params["q"] = name
    params["appId"] = API_KEY_OPEN_WEATHER

    val openWeatherApi = (application as BaseApplication).openWeatherApi

    val call = openWeatherApi.getWeather(params)

    call.enqueue(object : Callback<WeatherFile> {
      override fun onFailure(call: Call<WeatherFile>, t: Throwable) {
        toastLocationError("onFailure: failed to call openweather.org")
      }

      override fun onResponse(call: Call<WeatherFile>, response: Response<WeatherFile>) {
        if (!response.isSuccessful) {
          Log.d(TAG, "onResponse: weather request unsuccessful")
          return onWeatherRequestResponse(RESULT_FAILURE, null)
        }

        val weatherFile = response.body()

        if (weatherFile == null) {
          Log.d(TAG, "onResponse: weather request file is empty!")
          return onWeatherRequestResponse(RESULT_FAILURE, null)
        }

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

        onWeatherRequestResponse(RESULT_SUCCESS, locationData)
      }
    })
  }

  fun onWeatherRequestResponse(responseCode: Int, locationData: LocationData?) {
    if (responseCode == RESULT_SUCCESS) {
      if (locationData != null) viewModel.insertLocationData(locationData)
      else Log.d(TAG, "onWeatherRequestResponse: no location data")
    }
  }



  // ------------------------ UTIL ------------------------

  companion object {
    private const val TAG = "MainActivity"

    private val UPDATE_INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(30)
    private val UPDATE_INTERVAL_FASTEST_IN_MS = TimeUnit.MINUTES.toMillis(5)

    private const val API_KEY_OPEN_WEATHER = BuildConfig.OPEN_WEATHER_APP_KEY
  }
}