package com.gerardbradshaw.whetherweather.activities.utils

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.application.BaseApplication
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.room.Repository
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place

class BaseViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: Repository = (application as BaseApplication).repository

  fun getLiveLocations(): LiveData<List<LocationEntity>> {
    return repository.getLiveLocations()
  }

  fun saveLocation(location: LocationEntity) {
    return repository.saveLocation(location)
  }

  fun saveLocation(place: Place) {
    val addressComponents = place.addressComponents

    if (addressComponents == null) {
      Log.e(TAG, "saveLocationToDb: no address components.")

      Toast.makeText(
        getApplication(),
        getApplication<BaseApplication>().getString(R.string.string_unable_to_determine_location),
        Toast.LENGTH_SHORT).show()

      return
    }

    val locality = extractLocalityFromAddressComponents(addressComponents)
    val lat = place.latLng?.latitude?.toFloat()
    val lon = place.latLng?.longitude?.toFloat()

    if (lat == null || lon == null) {
      Log.e(TAG, "saveLocationToDb: coordinate missing from $locality ($lat, $lon).")
      return
    }

    val entity = LocationEntity(locality, lat, lon)

    saveLocation(entity)
    Log.i(TAG, "saveLocationToDb: Success! ${entity.locality} sent to DB")
  }

  fun deleteLocation(location: LocationEntity) = repository.deleteLocation(location)

  private fun extractLocalityFromAddressComponents(components: AddressComponents): String {
    val componentsList = components.asList()
    for (component in componentsList) {
      for (type in component.types) {
        if (type == "locality") {
          return component.name
        }
      }
    }
    return getApplication<BaseApplication>().getString(R.string.string_unknown_location)
  }

  companion object {
    private const val TAG = "GGG BaseViewModel"
  }
}