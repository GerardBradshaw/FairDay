package com.gerardbradshaw.whetherweather.util

import androidx.test.core.app.ApplicationProvider
import com.gerardbradshaw.whetherweather.application.BaseApplication
import com.gerardbradshaw.whetherweather.room.LocationEntity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import javax.net.ssl.HttpsURLConnection

class MyMockServer {
  private val mockWebServer = MockWebServer()

  fun start() = mockWebServer.start()

  fun shutdown() = mockWebServer.shutdown()

  fun url() = mockWebServer.url("/")

  fun addLocation(n: Int) {
    val nRounded = getSafeIndex(n)
    enqueueWeatherResultFor(nRounded)

    val locationEntity = LocationEntity("Location$nRounded", 0f, 0f)

    ApplicationProvider.getApplicationContext<BaseApplication>()
      .repository
      .saveLocation(locationEntity)
  }

  fun addAllLocations() {
    addLocation(0)
    addLocation(1)
    addLocation(2)
  }

  private fun getSafeIndex(n: Int): Int {
    return n%3
  }

  private fun enqueueWeatherResultFor(n: Int) {
    val nValid = getSafeIndex(n)

    MockResponse().let {
      it.setResponseCode(HttpsURLConnection.HTTP_OK)
      it.setBody(FileUtils.readTestResourceFile("weather$nValid.txt"))
      mockWebServer.enqueue(it)
    }
  }
}