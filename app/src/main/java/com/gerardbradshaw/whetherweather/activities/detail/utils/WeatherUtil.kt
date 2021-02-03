package com.gerardbradshaw.whetherweather.activities.detail.utils

import android.content.Context
import android.location.Address
import android.util.Log
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.application.BaseApplication
import com.gerardbradshaw.whetherweather.BuildConfig
import com.gerardbradshaw.whetherweather.retrofit.WeatherFile
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class WeatherUtil @Inject constructor(private val context: Context) {

  private var listener: WeatherDetailsListener? = null

  fun setWeatherDetailsListener(listener: WeatherDetailsListener) {
    this.listener = listener
  }


  // ------------------------ WEATHER REQUESTS ------------------------

  fun requestWeatherForEntityUsingLatLon(locationEntity: LocationEntity) {
    val params = HashMap<String, String>()
    params["lat"] = locationEntity.lat.toString()
    params["lon"] = locationEntity.lon.toString()
    params["appId"] = API_KEY_OPEN_WEATHER

    enqueueOpenWeatherCall(params, locationEntity)
  }
  
  fun requestWeatherForEntityUsingPostCode(locationEntity: LocationEntity, address: Address) {
    val zipCode = address.postalCode
    val countryCode = address.countryCode
    val zip = "$zipCode,$countryCode"
    
    val params = HashMap<String, String>()
    params["zip"] = zip
    params["appId"] = API_KEY_OPEN_WEATHER
    
    enqueueOpenWeatherCall(params, locationEntity)
  }


  // ------------------------ OPEN WEATHER CALLS ------------------------

  private fun enqueueOpenWeatherCall(
      params: HashMap<String, String>,
      locationEntity: LocationEntity?
  ) {
    val openWeatherApi = (context.applicationContext as BaseApplication).openWeatherApi
    val call = openWeatherApi.getWeather(params)
    
    call.enqueue(object : Callback<WeatherFile> {
      override fun onFailure(call: Call<WeatherFile>, t: Throwable) {
        Log.e(TAG, "onFailure: ERROR: failed to call web host.")
//        Toast.makeText(context, "Can't connect to OpenWeather.com", Toast.LENGTH_SHORT).show()

        return onWeatherRequestResponse(Constants.RESULT_FAILURE, null, locationEntity)
      }

      override fun onResponse(call: Call<WeatherFile>, response: Response<WeatherFile>) {
        var responseCode = Constants.RESULT_FAILURE
        var weatherFile: WeatherFile? = null

        when {
          !response.isSuccessful -> Log.e(TAG, "onResponse: ERROR: unsuccessful response.")
          response.body() == null -> Log.e(TAG, "onResponse: ERROR: Weather response empty.")
          else -> {
            responseCode = Constants.RESULT_SUCCESS
            weatherFile = response.body()
          }
        }

        return onWeatherRequestResponse(responseCode, weatherFile, locationEntity)
      }
    })
  }
  
  fun onWeatherRequestResponse(
      responseCode: Int,
      weatherFile: WeatherFile?,
      locationEntity: LocationEntity?,
  ) {
    if (responseCode == Constants.RESULT_SUCCESS && weatherFile != null) {
      val weatherData = WeatherDataUtil.getWeatherDataFromWeatherFile(weatherFile)
      listener?.onWeatherReceived(weatherData, locationEntity)
    }
    else Log.e(TAG, "onWeatherRequestResponse: ERROR: no location data")
  }
  
  interface WeatherDetailsListener {
    fun onWeatherReceived(weatherData: WeatherData, locationEntity: LocationEntity?)
  }
  
  companion object {
    private const val TAG = "GGG WeatherUtil"
    private const val API_KEY_OPEN_WEATHER = BuildConfig.OPEN_WEATHER_APP_KEY
  }
}