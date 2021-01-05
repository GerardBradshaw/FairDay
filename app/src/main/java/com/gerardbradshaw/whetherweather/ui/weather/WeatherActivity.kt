package com.gerardbradshaw.whetherweather.ui.weather

import android.location.Address
import android.location.Location
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.BuildConfig
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.retrofit.WeatherFile
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.models.WeatherData
import com.gerardbradshaw.whetherweather.util.ConditionImageUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class WeatherActivity : AbstractLocationActivity(UPDATE_INTERVAL_IN_MS, UPDATE_INTERVAL_FASTEST_IN_MS) {
  private lateinit var viewModel: MainViewModel
  private lateinit var viewPager: ViewPager2
  private lateinit var conditionImageView: ImageView

  private var shouldLoadTestLocations = true
  private var isRequestingUpdates = false



  // ------------------------ INIT ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_weather)

    supportActionBar?.setDisplayShowTitleEnabled(false)
    viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

    conditionImageView = findViewById(R.id.condition_image_view)

    initViewPager()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_locations_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    isRequestingUpdates = !isRequestingUpdates

    if (isRequestingUpdates) {
      stopLocationUpdates()
      showToast("Updates stopped")
    }
    else {
      showToast("Updates started")
      startLocationUpdates()
    }

    return super.onOptionsItemSelected(item)
  }



  // ------------------------ UI ------------------------

  override fun onCurrentLocationUpdate() {
    Log.d(TAG, "onCurrentLocationUpdate: updated ZIP = ${currentAddress?.postalCode}")

    if (currentAddress != null) {
      requestWeatherFor(currentAddress!!, true)
    }
  }

  private fun showLocationAlertDialog() {
    AlertDialog.Builder(this)
      .setMessage("Lat: ${currentLocation?.latitude}\nLong: ${currentLocation?.longitude}\nAddress: $currentAddress\n")
      .setTitle("Location information")
      .create()
      .show()
  }



  // ------------------------ VIEW PAGER ------------------------

  private fun initViewPager() {
    viewPager = findViewById(R.id.view_pager)
    val adapter = LocationListAdapter(this)
    viewPager.adapter = adapter

    val savedLocations = viewModel.locationDataSet

    for (entity in savedLocations) {
      requestWeatherFor(entity.lat, entity.lon)
    }

    if (shouldLoadTestLocations) {
      val testLocations = arrayOf("San Francisco", "London", "New York")
      for (locationName in testLocations) requestWeatherFor(locationName)
    }
  }

  private fun requestWeatherFor(location: Location, isCurrentLocation: Boolean = false) {
    requestWeatherFor(
      location.latitude.toFloat(),
      location.longitude.toFloat(),
      isCurrentLocation)
  }

  private fun requestWeatherFor(lat: Float, lon: Float, isCurrentLocation: Boolean = false) {
    val params = HashMap<String, String>()
    params["lat"] = lat.toString()
    params["lon"] = lon.toString()
    params["appId"] = API_KEY_OPEN_WEATHER

    makeOpenWeatherCall(params, isCurrentLocation)
  }

  private fun requestWeatherFor(address: Address, isCurrentLocation: Boolean = false) {
    val zipCode = address.postalCode
    val countryCode = address.countryCode
    val zip = "$zipCode,$countryCode"

    val params = HashMap<String, String>()
    params["zip"] = zip
    params["appId"] = API_KEY_OPEN_WEATHER

    makeOpenWeatherCall(params, isCurrentLocation)
  }

  private fun requestWeatherFor(name: String, isCurrentLocation: Boolean = false) {
    val params = HashMap<String, String>()
    params["q"] = name
    params["appId"] = API_KEY_OPEN_WEATHER

    makeOpenWeatherCall(params, isCurrentLocation)
  }

  private fun makeOpenWeatherCall(params: HashMap<String, String>, isCurrentLocation: Boolean) {
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

        onWeatherRequestResponse(RESULT_SUCCESS, weatherFile, isCurrentLocation)
      }
    })
  }

  fun onWeatherRequestResponse(
    responseCode: Int,
    weatherFile: WeatherFile?,
    isCurrentLocation: Boolean = false
  ) {
    if (responseCode == RESULT_SUCCESS && weatherFile != null) {
      val adapter = viewPager.adapter as LocationListAdapter
      val weatherData = WeatherData(weatherFile)

      viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
          super.onPageSelected(position)

          val id = adapter.getConditionIdForPosition(position)
          val imageResId = ConditionImageUtil.getConditionImageUri(id)

          Glide
            .with(this@WeatherActivity)
            .asBitmap()
            .load(imageResId)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(conditionImageView)
        }
      })

      if (isCurrentLocation) {
        adapter.setCurrentLocation(weatherData)
      }
      else {
        val locationEntity = LocationEntity.getEntityFromWeatherData(weatherData)
        viewModel.insertWeatherData(locationEntity)
        adapter.addLocations(weatherData)
      }
    }
    else Log.d(TAG, "onWeatherRequestResponse: no location data")
  }



  // ------------------------ UTIL ------------------------

  companion object {
    private const val TAG = "WeatherActivity"

    private val UPDATE_INTERVAL_IN_MS = TimeUnit.MINUTES.toMillis(30)
    private val UPDATE_INTERVAL_FASTEST_IN_MS = TimeUnit.MINUTES.toMillis(5)

    private const val API_KEY_OPEN_WEATHER = BuildConfig.OPEN_WEATHER_APP_KEY
  }
}