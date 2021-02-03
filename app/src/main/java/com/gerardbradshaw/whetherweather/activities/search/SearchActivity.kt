package com.gerardbradshaw.whetherweather.activities.search

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.activities.BaseViewModel
import com.gerardbradshaw.whetherweather.activities.detail.DetailActivity
import com.gerardbradshaw.whetherweather.Constants.API_KEY_MAPS
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class SearchActivity : AppCompatActivity() {
  private lateinit var viewModel: BaseViewModel
  private lateinit var placesClient: PlacesClient
  private lateinit var autocompleteFragment: AutocompleteSupportFragment


  // ------------------------ ACTIVITY EVENTS ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search)
    initActivity()
  }


  // ------------------------ INIT ------------------------

  private fun initActivity() {
    supportActionBar?.hide()
    viewModel = ViewModelProvider(this).get(BaseViewModel::class.java)

    initPlacesApi()
    initAutocompleteFragment()
  }

  private fun initPlacesApi() {
    if (!Places.isInitialized()) {
      Places.initialize(applicationContext, API_KEY_MAPS)
      placesClient = Places.createClient(this)
    }
  }

  private fun initAutocompleteFragment() {
    autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
        as AutocompleteSupportFragment

    autocompleteFragment.let {
      it.setPlaceFields(listOf(
          Place.Field.ADDRESS_COMPONENTS,
          Place.Field.LAT_LNG))

      it.setOnPlaceSelectedListener(object : PlaceSelectionListener {
        override fun onPlaceSelected(p0: Place) {
          saveLocationToDb(p0)

          val returnIntent = Intent()
          returnIntent.putExtra(DetailActivity.EXTRA_PAGER_POSITION, Int.MAX_VALUE)
          setResult(RESULT_OK, returnIntent)
          finish()
        }

        override fun onError(p0: Status) {
          Log.e(TAG, "onError: ERROR: no place was selected.")
          setResult(RESULT_CANCELED)
          finish()
        }
      })

      it.view?.requestFocus()
    }
  }


  // ------------------------ UTIL ------------------------

  private fun saveLocationToDb(place: Place) {
    val addressComponents = place.addressComponents

    if (addressComponents == null) {
      Log.e(TAG, "saveLocationToDb: no address components.")
      Toast.makeText(this, getString(R.string.string_unable_to_determine_location), Toast.LENGTH_SHORT).show()
      return
    }

    val locality = extractFromAddress(addressComponents.asList(), "locality")
    val lat = place.latLng?.latitude?.toFloat()
    val lon = place.latLng?.longitude?.toFloat()

    if (lat == null || lon == null) {
      Log.e(TAG, "saveLocationToDb: ERROR: coordinate missing from $locality ($lat, $lon).")
      return
    }

    val entity = LocationEntity(locality, lat, lon)

    viewModel.saveLocation(entity)
    Log.i(TAG, "saveLocationToDb: Success! ${entity.locality} sent to DB")
  }

  private fun extractFromAddress(
    components: List<AddressComponent>,
    componentToExtract: String
  ): String {
    for (component in components) {
      for (type in component.types) {
        if (type == componentToExtract) {
          return component.name
        }
      }
    }
    return getString(R.string.string_unknown_location)
  }

  companion object {
    private const val TAG = "GGG SearchActivity"
  }
}