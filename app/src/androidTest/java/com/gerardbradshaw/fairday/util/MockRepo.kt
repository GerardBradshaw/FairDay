package com.gerardbradshaw.fairday.util

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.gerardbradshaw.fairday.application.BaseApplication
import com.gerardbradshaw.fairday.room.LocationEntity
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import javax.net.ssl.HttpsURLConnection

class MockRepo {
  private val mockWebServer = MockWebServer()

  fun start() = mockWebServer.start()

  fun shutdown() = mockWebServer.shutdown()

  fun url() = mockWebServer.url("/")

  fun addLocation(location: MockLocation) {
    enqueueWeatherResultFor(location)

    val n = location.ordinal
    val entity = LocationEntity("Location$n", n * 10f, n * 10f)
    ApplicationProvider.getApplicationContext<BaseApplication>().repository.saveLocation(entity)
  }

  fun addAllLocations() {
    addLocation(MockLocation.LOCATION_0)
    addLocation(MockLocation.LOCATION_1)
    addLocation(MockLocation.LOCATION_2)
  }

  private fun enqueueWeatherResultFor(location: MockLocation) {
    MockResponse().let {
      Log.i(TAG, "enqueueWeatherResultFor: enqueueing Location${location.ordinal}")
      it.setResponseCode(HttpsURLConnection.HTTP_OK)
      it.setBody(FileUtils.readTestResourceFile("weather${location.ordinal}.txt"))
      mockWebServer.enqueue(it)
    }
  }

  enum class MockLocation {
    LOCATION_0, LOCATION_1, LOCATION_2
  }

  companion object {
    private const val TAG = "GGG MockRepo"
  }
}