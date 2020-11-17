package com.gerardbradshaw.whetherweather.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.util.WeatherData

class MainActivity : AppCompatActivity() {
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
  private lateinit var viewPager: ViewPager2
  private var currentLocation: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    supportActionBar?.hide()
//    supportActionBar?.setDisplayShowTitleEnabled(false)

    initLocationAccess()
    initViewPager()
  }

  private fun initLocationAccess() {
    requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted: Boolean ->
      currentLocation =
        if (isPermissionGranted) "San Francisco" // TODO change to actual current location
        else null
    }
    requestLocationAccess()
  }

  private fun initViewPager() {
    viewPager = findViewById(R.id.view_pager)

    val locations = ArrayList<WeatherData>()

    val adapter = LocationPagerAdapter(this, locations)
    viewPager.adapter = adapter
  }

  private fun requestLocationAccess() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val permissionName = Manifest.permission.ACCESS_COARSE_LOCATION
      val permissionStatus = ContextCompat.checkSelfPermission(this, permissionName)
      val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)

      if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
        if (shouldShowRationale) showLocationPermissionRationale(permissionName)
        else requestPermissionLauncher.launch(permissionName)
      }
    }
  }

  private fun showLocationPermissionRationale(permissionName: String) {
    AlertDialog.Builder(this)
      .setMessage(getString(R.string.app_name) + getString(R.string.location_rationale_message))
      .setTitle(getString(R.string.location_rationale_title))
      .setPositiveButton("Ok") { _, _ -> requestPermissionLauncher.launch(permissionName) }
      .setNegativeButton("Not now") { _, _ -> }
      .create()
      .show()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_locations_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }
}