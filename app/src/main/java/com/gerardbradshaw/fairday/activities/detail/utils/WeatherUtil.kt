package com.gerardbradshaw.fairday.activities.detail.utils

import android.content.Context
import android.util.Log
import com.gerardbradshaw.weatherview.datamodels.WeatherData
import com.gerardbradshaw.fairday.application.BaseApplication
import com.gerardbradshaw.fairday.BuildConfig
import com.gerardbradshaw.fairday.retrofit.WeatherFile
import com.gerardbradshaw.fairday.room.LocationEntity
import com.gerardbradshaw.fairday.Constants
import com.gerardbradshaw.fairday.retrofit.OneCallWeatherFile
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

  @Deprecated("Use requestFullWeatherFor() instead.")
  fun requestWeatherFor(locationEntity: LocationEntity) {
    val params = HashMap<String, String>()
    params["lat"] = locationEntity.lat.toString()
    params["lon"] = locationEntity.lon.toString()
    params["appId"] = API_KEY_OPEN_WEATHER

    enqueueOpenWeatherCall(params, locationEntity)
  }

  fun requestFullWeatherFor(locationEntity: LocationEntity) {
    val params = HashMap<String, String>()
    params["lat"] = locationEntity.lat.toString()
    params["lon"] = locationEntity.lon.toString()
    params["appId"] = API_KEY_OPEN_WEATHER

    enqueueOpenWeatherOneCall(params, locationEntity)
  }


  // ------------------------ OPEN WEATHER CALLS ------------------------

  @Deprecated("Use enqueueOpenWeatherOneCall() instead.")
  private fun enqueueOpenWeatherCall(
      params: HashMap<String, String>,
      locationEntity: LocationEntity?
  ) {
    val openWeatherApi = (context.applicationContext as BaseApplication).openWeatherApi
    val call = openWeatherApi.getWeather(params)
    
    call.enqueue(object : Callback<WeatherFile> {
      override fun onFailure(call: Call<WeatherFile>, t: Throwable) {
        Log.e(TAG, "onFailure: failed to call web host.", t)

        return onWeatherRequestResponse(Constants.RESULT_FAILURE, null, locationEntity)
      }

      override fun onResponse(call: Call<WeatherFile>, response: Response<WeatherFile>) {
        var responseCode = Constants.RESULT_FAILURE
        var weatherFile: WeatherFile? = null

        when {
          !response.isSuccessful -> Log.e(TAG, "onResponse: unsuccessful response.")
          response.body() == null -> Log.e(TAG, "onResponse: Weather response empty.")
          else -> {
            responseCode = Constants.RESULT_SUCCESS
            weatherFile = response.body()
          }
        }

        return onWeatherRequestResponse(responseCode, weatherFile, locationEntity)
      }
    })
  }

  private fun enqueueOpenWeatherOneCall(
    params: HashMap<String, String>,
    locationEntity: LocationEntity?
  ) {
    val openWeatherApi = (context.applicationContext as BaseApplication).openWeatherApi
    val call = openWeatherApi.getOneCallWeather(params)

    call.enqueue(object : Callback<OneCallWeatherFile> {
      override fun onFailure(call: Call<OneCallWeatherFile>, t: Throwable) {
        Log.e(TAG, "onFailure: failed to call web host.", t)

        return onWeatherRequestResponse(Constants.RESULT_FAILURE, null, locationEntity)
      }

      override fun onResponse(call: Call<OneCallWeatherFile>, response: Response<OneCallWeatherFile>) {
        var responseCode = Constants.RESULT_FAILURE
        var weatherFile: OneCallWeatherFile? = null

        when {
          !response.isSuccessful -> Log.e(TAG, "onResponse: unsuccessful response.")
          response.body() == null -> Log.e(TAG, "onResponse: Weather response empty.")
          else -> {
            responseCode = Constants.RESULT_SUCCESS
            weatherFile = response.body()
          }
        }

        return onOneCallRequestResponse(responseCode, weatherFile, locationEntity)
      }
    })
  }

  @Deprecated("Use onOneCallRequestResponse() instead.")
  fun onWeatherRequestResponse(
      responseCode: Int,
      weatherFile: WeatherFile?,
      locationEntity: LocationEntity?,
  ) {
    if (responseCode == Constants.RESULT_SUCCESS && weatherFile != null) {
      val weatherData = WeatherDataUtil.getWeatherDataFromWeatherFile(weatherFile)
      listener?.onWeatherReceived(weatherData, locationEntity)
    }
    else Log.e(TAG, "onWeatherRequestResponse: no location data")
  }

  fun onOneCallRequestResponse(
    responseCode: Int,
    weatherFile: OneCallWeatherFile?,
    locationEntity: LocationEntity?,
  ) {
    if (responseCode == Constants.RESULT_SUCCESS && weatherFile != null) {
      val weatherData = WeatherDataUtil.getWeatherDataFromOneCallWeatherFile(weatherFile)
      listener?.onWeatherReceived(weatherData, locationEntity)
    }
    else Log.e(TAG, "onWeatherRequestResponse: no location data")
  }
  
  interface WeatherDetailsListener {
    fun onWeatherReceived(weatherData: WeatherData, locationEntity: LocationEntity?)
  }
  
  companion object {
    private const val TAG = "GGG WeatherUtil"
    private const val API_KEY_OPEN_WEATHER = BuildConfig.OPEN_WEATHER_APP_KEY
  }
}