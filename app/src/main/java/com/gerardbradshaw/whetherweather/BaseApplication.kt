package com.gerardbradshaw.whetherweather

import android.app.Application
import com.gerardbradshaw.whetherweather.di.app.AppComponent
import com.gerardbradshaw.whetherweather.di.app.DaggerAppComponent
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
  private lateinit var component: AppComponent


  // ------------------------ APPLICATION EVENTS ------------------------

  override fun onCreate() {
    super.onCreate()
    initApp()
  }


  // ------------------------ INIT ------------------------

  private fun initApp() {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://api.openweathermap.org/data/2.5/")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    openWeatherApi = retrofit.create(OpenWeatherApi::class.java)

    repository = Repository(this)
    weatherUtil = WeatherUtil(this)

    component = DaggerAppComponent
      .builder()
      .setApplication(this)
      .build()
  }


  // ------------------------ BASE APPLICATION METHODS ------------------------

  fun getRepository(): Repository {
    return repository
  }

  fun getWeatherUtil(listener: WeatherUtil.WeatherDetailsListener): WeatherUtil {
    weatherUtil.setWeatherDetailsListener(listener)
    return weatherUtil
  }

  fun getAppComponent(): AppComponent {
    return component
  }
}