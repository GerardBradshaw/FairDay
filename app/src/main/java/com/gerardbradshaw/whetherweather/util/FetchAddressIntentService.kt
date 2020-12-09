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
import java.lang.IndexOutOfBoundsException
import java.util.*

class FetchAddressIntentService : IntentService("FetchAddress") {

  private var receiver: ResultReceiver? = null

  override fun onHandleIntent(intent: Intent?) {
    receiver = intent?.getParcelableExtra(Constants.RECEIVER)

    var address: Address? = null
    var resultCode = Constants.FAILURE_RESULT

    if (intent == null || receiver == null) {
      Log.d(TAG, "onHandleIntent: No receiver. There is nowhere to send the results")
    } else {
      val location = intent.getParcelableExtra<Location>(Constants.LOCATION_DATA_EXTRA)

      if (location == null) Log.d(TAG, "onHandleIntent: No location data provided")
      else {
        try {
          address = Geocoder(this, Locale.getDefault())
            .getFromLocation(location.latitude, location.longitude, 1)
            .get(0)

          resultCode = Constants.SUCCESS_RESULT
          Log.d(TAG, "onHandleIntent: address found")

        } catch (ioException: IOException) {
          Log.e(TAG, "onHandleIntent: service unavailable", ioException)

        } catch (illegalArgumentException: IllegalArgumentException) {
          Log.e(TAG, "onHandleIntent: Invalid lat or long", illegalArgumentException)

        } catch (indexException: IndexOutOfBoundsException) {
          Log.e(TAG, "onHandleIntent: no address found")
        }
      }
    }

    deliverResultToReceiver(resultCode, address)
  }

  private fun deliverResultToReceiver(resultCode: Int, address: Address?) {
    val bundle = Bundle().apply { putParcelable(Constants.RESULT_DATA_KEY, address) }
    receiver?.send(resultCode, bundle)
  }

  companion object {
    private const val TAG = "FetchAddressService"
  }
}