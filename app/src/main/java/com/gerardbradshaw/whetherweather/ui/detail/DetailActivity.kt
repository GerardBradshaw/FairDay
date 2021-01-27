package com.gerardbradshaw.whetherweather.ui.detail

import android.content.Intent
import android.location.Address
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.ui.BaseViewModel
import com.gerardbradshaw.whetherweather.ui.search.SearchActivity
import com.gerardbradshaw.whetherweather.ui.savedlocations.SavedLocationsActivity
import com.gerardbradshaw.whetherweather.util.*
import com.gerardbradshaw.whetherweather.util.conditions.ConditionImageUtil
import com.gerardbradshaw.whetherweather.util.location.GpsUtil
import com.gerardbradshaw.whetherweather.util.location.GpsUtil.Companion.REQUEST_CODE_CHECK_SETTINGS
import com.gerardbradshaw.whetherweather.util.weather.WeatherUtil

class DetailActivity :
    AppCompatActivity(),
    GpsUtil.LocationUpdateListener,
    WeatherUtil.WeatherDetailsListener {

  private lateinit var viewModel: BaseViewModel
  private lateinit var viewPager: ViewPager2
  private lateinit var backgroundImage: ImageView
  private lateinit var pagerAdapter: DetailPagerAdapter

  private lateinit var gpsUtil: GpsUtil
  private lateinit var weatherUtil: WeatherUtil

  private var isFirstLaunch = true


  // ------------------------ ACTIVITY EVENTS ------------------------

  private val movePagerToPosition =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intent = it.data
        when {
          intent == null -> Log.d(TAG, "movePagerToPosition: no intent received from last activity.")

          intent.hasExtra(EXTRA_PAGER_POSITION) -> {
            val position = intent.getIntExtra(EXTRA_PAGER_POSITION, 0)
            val maxPosition = viewPager.adapter?.itemCount?.minus(1) ?: -1

            when (position) {
              Int.MAX_VALUE -> viewPager.setCurrentItem(maxPosition, true)
              in 0..maxPosition -> viewPager.setCurrentItem(position, false)
              else -> Log.d(TAG, "movePagerToPosition: ERROR: given position is invalid")
            }
          }
        }
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)
    initActivity(savedInstanceState)
  }

  override fun onResume() {
    super.onResume()
    gpsUtil.onResume()
  }

  override fun onPause() {
    gpsUtil.onPause()
    super.onPause()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_CODE_CHECK_SETTINGS) {
      gpsUtil.onActivityResult(requestCode, resultCode, data)
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_detail_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_saved_locations -> {
        val intent = Intent(this, SavedLocationsActivity::class.java)
        movePagerToPosition.launch(intent)
        true
      }

      R.id.action_add -> {
        val intent = Intent(this, SearchActivity::class.java)
        movePagerToPosition.launch(intent)
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
  }


  // ------------------------ INIT ------------------------

  private fun initActivity(savedInstanceState: Bundle?) {
    supportActionBar?.setDisplayShowTitleEnabled(false)
    viewModel = ViewModelProvider(this).get(BaseViewModel::class.java)

    backgroundImage = findViewById(R.id.background_image_view)
    weatherUtil = (application as BaseApplication).getWeatherUtil(this)

    initGpsUtil(savedInstanceState)
    initViewPager()
    initCurrentLocationWeather()
  }

  private fun initGpsUtil(savedInstanceState: Bundle?) {
    gpsUtil = GpsUtil(this)
    gpsUtil.setOnLocationUpdateListener(this)
  }

  private fun initViewPager() {
    viewPager = findViewById(R.id.view_pager)
    pagerAdapter = DetailPagerAdapter(this)
    viewPager.adapter = pagerAdapter

    viewModel.getAllLocations().observe(this) {
      for (entity in it) {
        pagerAdapter.addNewLocation(entity)
        weatherUtil.requestWeatherFor(entity)
      }
    }
  }

  private fun initCurrentLocationWeather() {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val isFirstLaunch = prefs.getBoolean(Constants.KEY_IS_FIRST_LAUNCH, true)

    if (isFirstLaunch) {
      prefs.edit()
          .putBoolean(Constants.KEY_IS_FIRST_LAUNCH, false)
          .apply()

      gpsUtil.requestUpdates()
    }
  }


  // ------------------------ LOCATION ------------------------

  override fun onAddressUpdate(address: Address?) {
    if (address == null) {
      Log.d(TAG, "onLocationUpdate: ERROR: address is null")
      return
    }

    Log.d(TAG, "onAddressUpdate: current address received")

    val currentLocationEntity = LocationEntity(
        address.postalCode,
        0L,
        address.latitude.toFloat(),
        address.longitude.toFloat())

    pagerAdapter.setCurrentLocation(currentLocationEntity)
    weatherUtil.requestWeatherFor(currentLocationEntity, address)
  }

  override fun onWeatherReceived(weatherData: WeatherData, locationEntity: LocationEntity?) {
    if (isFirstLaunch) {
      changeConditionBackgroundImage(viewPager.currentItem)
      isFirstLaunch = false
    }

    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        changeConditionBackgroundImage(position)
      }
    })

    pagerAdapter.updateWeatherData(weatherData, locationEntity)
  }

  private fun changeConditionBackgroundImage(position: Int) {
    val id = pagerAdapter.getConditionIdForPosition(position)
    val imageResId = ConditionImageUtil.getConditionImageUri(id)

    Glide
        .with(this@DetailActivity)
        .asBitmap()
        .load(imageResId)
        .transition(BitmapTransitionOptions.withCrossFade())
        .into(backgroundImage)
  }


  // ------------------------ UTIL ------------------------

  companion object {
    private const val TAG = "GGG WeatherActivity"
    const val EXTRA_PAGER_POSITION = "detail_pager_adapter_position"
  }
}