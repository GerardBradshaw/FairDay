package com.gerardbradshaw.fairday.activities.detail

import android.app.Service
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.util.Log
import com.gerardbradshaw.fairday.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IndexOutOfBoundsException
import java.util.*

class FetchAddressIntentService : Service() {
  private var receiver: ResultReceiver? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    receiver = intent?.getParcelableExtra(Constants.RECEIVER)

    val isTest = intent?.getBooleanExtra(Constants.EXTRA_IS_TEST, false) ?: false
    Log.i(TAG, "onStartCommand: address fetch requested (test status: $isTest).")

    if (isTest) fetchAddressInForeground(intent)
    else fetchAddressInBackground(intent)

    return START_NOT_STICKY
  }

  private fun fetchAddressInForeground(intent: Intent?) {
    Log.i(TAG, "fetchAddressInForeground: fetching address in foreground.")
    getAddressRunnable(intent).run()
  }

  private fun getAddressRunnable(intent: Intent?): Runnable {
    return Runnable {
      var address: Address? = null
      var resultCode = Constants.RESULT_FAILURE

      if (intent == null || receiver == null) {
        Log.e(TAG, "onHandleIntent: No receiver. There is nowhere to send the results")
      } else {
        val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION_DATA)

        if (location == null) {
          Log.e(TAG, "onHandleIntent: No location data provided")
        }
        else {
          try {
            address = Geocoder(this, Locale.getDefault())
              .getFromLocation(location.latitude, location.longitude, 1)
              .get(0)

            resultCode = Constants.RESULT_SUCCESS

          } catch (e: IOException) {
            Log.e(TAG, "onHandleIntent: service unavailable", e)

          } catch (e: IllegalArgumentException) {
            Log.e(TAG, "onHandleIntent: Invalid lat or long", e)

          } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "onHandleIntent: no address found", e)
          }
        }
      }

      CoroutineScope(Dispatchers.Main).launch {
        Log.i(TAG, "getAddressRunnable: success! delivering results to receiver (${address?.locality}).")
        deliverResultToReceiver(resultCode, address)
      }
    }
  }

  private fun fetchAddressInBackground(intent: Intent?) {
    Log.i(TAG, "fetchAddressInBackground: fetching address in background.")
    CoroutineScope(Dispatchers.Main).launch {
      withContext(Dispatchers.Default) {
        getAddressRunnable(intent).run()
      }
    }
  }

  override fun onDestroy() {
    stopSelf()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  private fun deliverResultToReceiver(resultCode: Int, address: Address?) {
    val bundle = Bundle().apply { putParcelable(Constants.KEY_RESULT_DATA, address) }
    receiver?.send(resultCode, bundle)
  }

  companion object {
    private const val TAG = "GGG FetchAddressService"
  }
}
