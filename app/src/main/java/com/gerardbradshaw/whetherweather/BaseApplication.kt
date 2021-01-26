package com.gerardbradshaw.whetherweather

import android.app.Application
import com.gerardbradshaw.whetherweather.retrofit.OpenWeatherApi
import com.gerardbradshaw.whetherweather.room.Repository
import com.gerardbradshaw.whetherweather.util.weather.WeatherUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BaseApplication : Application() {

  lateinit var openWeatherApi: OpenWeatherApi
    private set

  private lateinit var repository: Repository
  private lateinit var weatherUtil: WeatherUtil

  override fun onCreate() {
    super.onCreate()

    val retrofit = Retrofit.Builder()
      .baseUrl("https://api.openweathermap.org/data/2.5/")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    openWeatherApi = retrofit.create(OpenWeatherApi::class.java)
    repository = Repository(this)

    weatherUtil = WeatherUtil(this)
  }

  fun getRepository(): Repository {
    return repository
  }

  fun getWeatherUtil(listener: WeatherUtil.WeatherDetailsListener): WeatherUtil {
    weatherUtil.setWeatherDetailsListener(listener)
    return weatherUtil
  }
}