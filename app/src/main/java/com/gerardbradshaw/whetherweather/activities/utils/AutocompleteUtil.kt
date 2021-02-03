package com.gerardbradshaw.whetherweather.activities.utils

import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gerardbradshaw.whetherweather.Constants
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import javax.inject.Inject

class AutocompleteUtil @Inject constructor(private val activity: AppCompatActivity) {
  private val viewModel = ViewModelProvider(activity).get(BaseViewModel::class.java)

  private val getPlaceFromSearch =
    activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      val intent = it.data

      when {
        intent == null -> Log.wtf(TAG, "getPlaceFromSearch: ERROR: missing intent.")

        it.resultCode == AutocompleteActivity.RESULT_OK -> {
          val place = Autocomplete.getPlaceFromIntent(intent)
          viewModel.savePlaceAsLocation(place)
        }

        it.resultCode == AutocompleteActivity.RESULT_CANCELED -> {
          Log.i(TAG, "getPlaceFromSearch: no place selected.")
        }

        it.resultCode == AutocompleteActivity.RESULT_ERROR -> {
          Log.e(TAG, "getPlaceFromSearch: ERROR: something went wrong with the autocomplete fragment.")
        }
      }
    }

  fun getPlaceFromAutocomplete() {
    initPlacesApi()
    val fields = listOf(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)
    val intent = Autocomplete
      .IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
      .build(activity)

    getPlaceFromSearch.launch(intent)
  }

  private fun initPlacesApi() {
    if (!Places.isInitialized()) {
      Places.initialize(activity.application, Constants.API_KEY_MAPS)
    }
  }

  companion object {
    private const val TAG = "GGG AutocompleteUtil"
  }
}