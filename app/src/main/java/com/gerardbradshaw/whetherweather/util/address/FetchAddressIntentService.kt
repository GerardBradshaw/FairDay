package com.gerardbradshaw.whetherweather.util.address

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import com.gerardbradshaw.whetherweather.util.Constants
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException
import java.util.*

class FetchAddressIntentService : IntentService("FetchAddress") {

  private var receiver: ResultReceiver? = null

  override fun onHandleIntent(intent: Intent?) {
    receiver = intent?.getParcelableExtra(Constants.RECEIVER)

    var address: Address? = null
    var resultCode = Constants.RESULT_FAILURE

    if (intent == null || receiver == null) {
      Log.d(TAG, "onHandleIntent: ERROR: No receiver. There is nowhere to send the results")
    } else {
      val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION_DATA)

      if (location == null) {
        Log.d(TAG, "onHandleIntent: ERROR: No location data provided")
      }
      else {
        try {
          address = Geocoder(this, Locale.getDefault())
            .getFromLocation(location.latitude, location.longitude, 1)
            .get(0)

          resultCode = Constants.RESULT_SUCCESS
          Log.d(TAG, "onHandleIntent: address found")

        } catch (ioException: IOException) {
          Log.e(TAG, "onHandleIntent: ERROR: service unavailable", ioException)

        } catch (illegalArgumentException: IllegalArgumentException) {
          Log.e(TAG, "onHandleIntent: ERROR: Invalid lat or long", illegalArgumentException)

        } catch (indexException: IndexOutOfBoundsException) {
          Log.e(TAG, "onHandleIntent: ERROR: no address found")
        }
      }
    }

    deliverResultToReceiver(resultCode, address)
  }

  private fun deliverResultToReceiver(resultCode: Int, address: Address?) {
    val bundle = Bundle().apply { putParcelable(Constants.KEY_RESULT_DATA, address) }
    receiver?.send(resultCode, bundle)
  }

  companion object {
    private const val TAG = "GGG FetchAddressService"
  }
}