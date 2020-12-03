package com.gerardbradshaw.whetherweather.util

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

class FetchAddressIntentService : IntentService("FetchAddress") {

  private var receiver: ResultReceiver? = null

  override fun onHandleIntent(intent: Intent?) {
    var errorMessage = ""

    receiver = intent?.getParcelableExtra(Constants.RECEIVER)

    if (intent == null || receiver == null) {
      Log.d(TAG, "onHandleIntent: No receiver. There is nowhere to send the results")
    }

    val location = intent!!.getParcelableExtra<Location>(Constants.LOCATION_DATA_EXTRA)

    if (location == null) {
      errorMessage = "No location data provided"
      Log.d(TAG, "onHandleIntent: $errorMessage")
      deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
      return
    }

    val geocoder = Geocoder(this, Locale.getDefault())

    var addresses: List<Address> = emptyList()

    try {
      addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    } catch (ioException: IOException) {
      errorMessage = "The service is not available"
      Log.e(TAG, "onHandleIntent: $errorMessage", ioException)
    } catch (illegalArgumentException: IllegalArgumentException) {
      errorMessage = "Invalid lat or long used"
      Log.e(TAG, "onHandleIntent: $errorMessage", illegalArgumentException)
    }

    if (addresses.isEmpty()) {
      if (errorMessage.isEmpty()) {
        errorMessage = "No address found"
        Log.e(TAG, "onHandleIntent: $errorMessage")
      }
      deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage)
    } else {
      val address = addresses[0]
      val postalCode = address.postalCode

      Log.i(TAG, "onHandleIntent: address found")
      deliverResultToReceiver(Constants.SUCCESS_RESULT, postalCode)
    }
  }

  private fun deliverResultToReceiver(resultCode: Int, message: String) {
    val bundle = Bundle().apply { putString(Constants.RESULT_DATA_KEY, message) }
    receiver?.send(resultCode, bundle)
  }

  companion object {
    private const val TAG = "FetchAddressService"
  }
}