package com.gerardbradshaw.whetherweather

import android.app.Application
import com.gerardbradshaw.whetherweather.di.app.AppComponent
import com.gerardbradshaw.whetherweather.di.app.DaggerAppComponent
import com.gerardbradshaw.whetherweather.retrofit.OpenWeatherApi
import com.gerardbradshaw.whetherweather.room.Repository
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
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
      .setMainDispatcher(Main)
      .setDefaultDispatcher(Default)
      .setIoDispatcher(IO)
      .build()

    component.inject(this)
    openWeatherApi = retrofit.create(OpenWeatherApi::class.java)
  }


  // ------------------------ BASE APPLICATION METHODS ------------------------

  fun replaceDispatchersForTests() {
    component = DaggerAppComponent
      .builder()
      .setApplication(this)
      .setMainDispatcher(Main)
      .setDefaultDispatcher(Main)
      .setIoDispatcher(Main)
      .build()

    component.inject(this)
  }

  fun getAppComponent(): AppComponent {
    return component
  }
}