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
import com.bumptech.glide.RequestManager
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
import javax.inject.Inject

class DetailActivity :
    AppCompatActivity(),
    GpsUtil.GpsUpdateListener,
    WeatherUtil.WeatherDetailsListener
{
  private lateinit var viewModel: BaseViewModel
  private lateinit var viewPager: ViewPager2
  private lateinit var backgroundImage: ImageView

  @Inject lateinit var pagerAdapter: DetailPagerAdapter
  @Inject lateinit var gpsUtil: GpsUtil
  @Inject lateinit var weatherUtil: WeatherUtil
  @Inject lateinit var glideInstance: RequestManager

  private var isFirstLaunch = true

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


  // ------------------------ ACTIVITY EVENTS ------------------------

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

    injectFields()
    initListeners()
    initViewPager()
    initCurrentLocationWeather()
  }

  private fun injectFields() {
    val component = (application as BaseApplication)
      .getAppComponent()
      .getDetailActivityComponentFactory()
      .create(this, this)

    component.inject(this)
  }

  private fun initListeners() {
    weatherUtil.setWeatherDetailsListener(this)
    gpsUtil.setOnGpsUpdateListener(this)
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

  override fun onGpsUpdate(address: Address?) {
    if (address == null) {
      Log.d(TAG, "onLocationUpdate: ERROR: address is null")
      return
    }
    requestWeatherForCurrentLocation(address)
  }

  private fun requestWeatherForCurrentLocation(address: Address) {
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

    glideInstance
      .asBitmap()
      .load(imageResId)
      .transition(BitmapTransitionOptions.withCrossFade())
      .into(backgroundImage)

//    Glide
//        .with(this@DetailActivity)
//        .asBitmap()
//        .load(imageResId)
//        .transition(BitmapTransitionOptions.withCrossFade())
//        .into(backgroundImage)
  }


  // ------------------------ UTIL ------------------------

  companion object {
    private const val TAG = "GGG WeatherActivity"
    const val EXTRA_PAGER_POSITION = "detail_pager_adapter_position"
  }
}