package com.gerardbradshaw.whetherweather

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(Enclosed::class)
class DetailActivityTests {

  @RunWith(RobolectricTestRunner::class)
  @Config(sdk = [28])
  class FirstLaunchTests {
    lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
      mockWebServer = MockWebServer()
      mockWebServer.start()
    }

    @After
    fun tearDown() {
      mockWebServer.shutdown()
    }

    @Test
    fun should_showInstructions_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_shouldAskForLocationPermission_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_haveTransparentActionBar_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_displayOpenWeatherCredit_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_askUserForLocationPermission_when_firstEntering() {
      fail("Not implemented")
    }
  }
}