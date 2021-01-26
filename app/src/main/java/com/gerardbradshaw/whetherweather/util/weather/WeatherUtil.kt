package com.gerardbradshaw.whetherweather.util.weather

import android.content.Context
import android.location.Address
import android.util.Log
import android.widget.Toast
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.BuildConfig
import com.gerardbradshaw.whetherweather.retrofit.WeatherFile
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.util.location.GpsUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherUtil(private val context: Context) {

  private var listener: WeatherDetailsListener? = null

  fun setWeatherDetailsListener(listener: WeatherDetailsListener) {
    this.listener = listener
  }


  // ------------------------ WEATHER REQUESTS ------------------------

  fun requestWeatherFor(locationEntity: LocationEntity, isCurrentLocation: Boolean = false) {
    val params = HashMap<String, String>()
    params["lat"] = locationEntity.lat.toString()
    params["lon"] = locationEntity.lon.toString()
    params["appId"] = API_KEY_OPEN_WEATHER

    makeOpenWeatherCall(params, locationEntity, isCurrentLocation)
  }
  
  fun requestWeatherFor(address: Address, isCurrentLocation: Boolean = false) {
    val zipCode = address.postalCode
    val countryCode = address.countryCode
    val zip = "$zipCode,$countryCode"
    
    val params = HashMap<String, String>()
    params["zip"] = zip
    params["appId"] = API_KEY_OPEN_WEATHER
    
    makeOpenWeatherCall(params, null, isCurrentLocation)
  }


  // ------------------------ OPEN WEATHER CALLS ------------------------

  private fun makeOpenWeatherCall(
      params: HashMap<String, String>,
      locationEntity: LocationEntity?,
      isCurrentLocation: Boolean
  ) {
    val openWeatherApi = (context.applicationContext as BaseApplication).openWeatherApi
    val call = openWeatherApi.getWeather(params)
    
    call.enqueue(object : Callback<WeatherFile> {
      override fun onFailure(call: Call<WeatherFile>, t: Throwable) {
        Log.d(TAG, "onFailure: ERROR: failed to call OpenWeather.org")
        Toast.makeText(context, "Can't connect to OpenWeather.com", Toast.LENGTH_SHORT).show()

        return onWeatherRequestResponse(
            GpsUtil.RESULT_FAILURE,
            null,
            locationEntity,
            isCurrentLocation)
      }

      override fun onResponse(call: Call<WeatherFile>, response: Response<WeatherFile>) {
        var responseCode = GpsUtil.RESULT_FAILURE
        var weatherFile: WeatherFile? = null

        when {
          !response.isSuccessful -> Log.d(TAG, "onResponse: ERROR: weather request unsuccessful")
          response.body() == null -> Log.d(TAG, "onResponse: ERROR: Weather response empty!")
          else -> {
            responseCode = GpsUtil.RESULT_SUCCESS
            weatherFile = response.body()
          }
        }

        return onWeatherRequestResponse(responseCode, weatherFile, locationEntity, isCurrentLocation)
      }
    })
  }
  
  fun onWeatherRequestResponse(
      responseCode: Int,
      weatherFile: WeatherFile?,
      locationEntity: LocationEntity?,
      isCurrentLocation: Boolean = false
  ) {
    if (responseCode == GpsUtil.RESULT_SUCCESS && weatherFile != null) {
      val weatherData = WeatherDataUtil.getWeatherDataFromWeatherFile(weatherFile)
      listener?.onWeatherReceived(weatherData, locationEntity, isCurrentLocation)
    }
    else Log.d(TAG, "onWeatherRequestResponse: ERROR: no location data")
  }
  
  interface WeatherDetailsListener {
    fun onWeatherReceived(
        weatherData: WeatherData,
        locationEntity: LocationEntity?,
        isCurrentLocation: Boolean)
  }
  
  companion object {
    private const val TAG = "GGG WeatherUtil"
    private const val API_KEY_OPEN_WEATHER = BuildConfig.OPEN_WEATHER_APP_KEY
  }
}