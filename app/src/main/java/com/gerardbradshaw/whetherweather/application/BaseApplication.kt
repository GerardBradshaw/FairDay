package com.gerardbradshaw.whetherweather.application

import android.app.Application
import com.gerardbradshaw.whetherweather.application.di.AppComponent
import com.gerardbradshaw.whetherweather.application.di.DaggerAppComponent
import com.gerardbradshaw.whetherweather.retrofit.OpenWeatherApi
import com.gerardbradshaw.whetherweather.room.Repository
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import javax.inject.Inject

class BaseApplication : Application() {
  private lateinit var component: AppComponent

  lateinit var openWeatherApi: OpenWeatherApi
    private set

  @Inject lateinit var repository: Repository
  @Inject lateinit var retrofit: Retrofit


  // ------------------------ APPLICATION EVENTS ------------------------

  override fun onCreate() {
    super.onCreate()
    initApp()
  }


  // ------------------------ INIT ------------------------

  private fun initApp() {
    component = DaggerAppComponent
      .builder()
      .setApplication(this)
      .setIsTest(false)
      .setHttpUrl(OPEN_WEATHER_URL.toHttpUrl())
      .build()

    component.inject(this)
    openWeatherApi = retrofit.create(OpenWeatherApi::class.java)
  }


  // ------------------------ BASE APPLICATION METHODS ------------------------

  fun prepareForTests(mockWebServer: HttpUrl) {
    component = DaggerAppComponent
      .builder()
      .setApplication(this)
      .setIsTest(true)
      .setHttpUrl(mockWebServer)
      .build()

    component.inject(this)
    openWeatherApi = retrofit.create(OpenWeatherApi::class.java)
  }

  fun getAppComponent(): AppComponent {
    return component
  }

  companion object {
    private const val TAG = "GGG BaseApplication"
    const val OPEN_WEATHER_URL = "https://api.openweathermap.org/data/2.5/"
  }
}