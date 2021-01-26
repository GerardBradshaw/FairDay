package com.gerardbradshaw.whetherweather.util.address

import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.whetherweather.util.Constants

class AddressUtil {

  private var addressResultReceiver: AddressResultReceiver
  private var listener: AddressChangeListener? = null

  init {
    addressResultReceiver = AddressResultReceiver(Handler())
  }

  fun fetchAddress(location: Location, activity: AppCompatActivity, listener: AddressChangeListener) {
    this.listener = listener

    val intent = Intent(activity, FetchAddressIntentService::class.java).apply {
      putExtra(Constants.RECEIVER, addressResultReceiver)
      putExtra(Constants.EXTRA_LOCATION_DATA, location)
    }

    activity.startService(intent)
  }

  private inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
      val address: Address? = resultData?.getParcelable(Constants.KEY_RESULT_DATA)
      listener?.onAddressChanged(address)

      if (resultCode == Constants.RESULT_SUCCESS) Log.d(TAG, "onReceiveResult: Address found")
    }
  }

  interface AddressChangeListener {
    fun onAddressChanged(address: Address?)
  }

  companion object {
    private const val TAG = "GGG AddressUtil"
  }
}