package com.gerardbradshaw.fairday.activities.detail.utils

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.fairday.activities.detail.FetchAddressIntentService
import com.gerardbradshaw.fairday.application.annotations.IsTest
import com.gerardbradshaw.fairday.Constants
import com.gerardbradshaw.fairday.R
import com.google.android.libraries.places.api.model.AddressComponents
import javax.inject.Inject

class AddressUtil @Inject constructor(@IsTest private val isTest: Boolean) {
  private var listener: AddressChangeListener? = null

  fun fetchAddress(location: Location, context: Context, listener: AddressChangeListener) {
    this.listener = listener

    val intent = Intent(context.applicationContext, FetchAddressIntentService::class.java).apply {
      putExtra(Constants.RECEIVER, AddressResultReceiver(Handler(Looper.getMainLooper())))
      putExtra(Constants.EXTRA_LOCATION_DATA, location)
      putExtra(Constants.EXTRA_IS_TEST, isTest)
    }

    context.startService(intent)
  }

  private inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
      val address: Address? = resultData?.getParcelable(Constants.KEY_RESULT_DATA)
      listener?.onAddressChanged(address)

      if (resultCode == Constants.RESULT_SUCCESS) Log.i(TAG, "onReceiveResult: Address found & delivered to listener")
    }
  }

  interface AddressChangeListener {
    fun onAddressChanged(address: Address?)
  }

  companion object {
    private const val TAG = "GGG AddressUtil"

    @JvmStatic
    fun extractLocalityFromAddressComponents(
      context: Context,
      components: AddressComponents?
    ): String {
      if (components != null) {
        val componentsList = components.asList()
        for (component in componentsList) {
          for (type in component.types) {
            if (type == "locality") {
              return component.name
            }
          }
        }
      }
      return context.getString(R.string.string_unknown_location)
    }
  }
}