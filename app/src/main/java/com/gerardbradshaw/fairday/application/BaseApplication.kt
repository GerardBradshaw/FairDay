package com.gerardbradshaw.fairday.application

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import com.gerardbradshaw.fairday.application.di.AppComponent
import com.gerardbradshaw.fairday.application.di.DaggerAppComponent
import com.gerardbradshaw.fairday.retrofit.OpenWeatherApi
import com.gerardbradshaw.fairday.room.Repository
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jetbrains.annotations.TestOnly
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
      .setHttpUrl(OPEN_WEATHER_BASE_URL.toHttpUrl())
      .build()

//    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
//
//    if (!prefs.contains(Constants.KEY_GPS_ENABLED)) {
//      prefs.edit().putBoolean(Constants.KEY_GPS_ENABLED, false).apply()
//    }

    component.inject(this)
    openWeatherApi = retrofit.create(OpenWeatherApi::class.java)
    Log.d(TAG, "initApp: retrofit is using ${retrofit.baseUrl()}")
  }


  // ------------------------ PUBLIC FUNCTIONS ------------------------

  @TestOnly
  fun prepareForTests(mockWebServer: HttpUrl) {
    component = DaggerAppComponent
      .builder()
      .setApplication(this)
      .setIsTest(true)
      .setHttpUrl(mockWebServer)
      .build()

    PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()

    component.inject(this)
    openWeatherApi = retrofit.create(OpenWeatherApi::class.java)
    Log.d(TAG, "initApp: retrofit is using ${retrofit.baseUrl()}")
  }

  fun getAppComponent(): AppComponent {
    return component
  }

  @TestOnly
  fun wipeDb() {
    Log.i(TAG, "wipeDb: Wiping database...")
    repository.wipeDb()
  }

  companion object {
    private const val TAG = "GGG BaseApplication"
    const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
  }
}