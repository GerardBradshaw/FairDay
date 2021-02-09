package com.gerardbradshaw.whetherweather.activities.detail

import android.content.Intent
import android.location.Address
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.Constants.KEY_GPS_NEEDED
import com.gerardbradshaw.whetherweather.Constants.RESULT_ERROR
import com.gerardbradshaw.whetherweather.application.BaseApplication
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.activities.utils.AutocompleteUtil
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.activities.utils.BaseViewModel
import com.gerardbradshaw.whetherweather.activities.detail.utils.ConditionImageUtil
import com.gerardbradshaw.whetherweather.activities.detail.utils.GpsUtil
import com.gerardbradshaw.whetherweather.activities.saved.SavedActivity
import com.gerardbradshaw.whetherweather.activities.detail.utils.GpsUtil.Companion.REQUEST_CODE_CHECK_SETTINGS
import com.gerardbradshaw.whetherweather.activities.detail.utils.WeatherUtil
import com.gerardbradshaw.whetherweather.activities.detail.viewpager.DetailPagerAdapter
import com.gerardbradshaw.whetherweather.activities.detail.viewpager.PagerItemUtil
import java.util.LinkedHashMap
import javax.inject.Inject
import kotlin.math.min

class DetailActivity :
  AppCompatActivity(),
  GpsUtil.GpsUpdateListener,
  WeatherUtil.WeatherDetailsListener,
  DetailPagerAdapter.DataChangeListener
{
  private lateinit var app: BaseApplication
  private lateinit var viewModel: BaseViewModel
  private lateinit var backgroundImage: ImageView
  private lateinit var instructionsTextView: TextView
  private lateinit var viewPager: ViewPager2
  private lateinit var pagerItemUtil: PagerItemUtil

  @Inject lateinit var pagerAdapter: DetailPagerAdapter
  @Inject lateinit var gpsUtil: GpsUtil
  @Inject lateinit var weatherUtil: WeatherUtil
  @Inject lateinit var glideInstance: RequestManager
  @Inject lateinit var autocompleteUtil: AutocompleteUtil

  private var isFirstLaunch = true
  private val liveWeather = MutableLiveData<LinkedHashMap<LocationEntity, WeatherData>>()

  private val viewPagerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      updateBackgroundImageToMatchAdapterAt(viewPager.currentItem)
      super.onPageSelected(position)
    }
  }

  private val movePagerResultLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      val intent = it.data

      if (intent == null) {
        Log.e(TAG, "movePagerToPosition: missing intent.")
      } else {
        when (it.resultCode) {
          RESULT_CANCELED -> Log.i(TAG, "movePagerToPosition: no place selected.")
          RESULT_ERROR -> Log.e(TAG, "movePagerToPosition: intent error. Cannot scroll.")
          RESULT_OK -> movePagerToPositionInIntent(intent)
        }
      }
    }

  private fun movePagerToPositionInIntent(intent: Intent) {
    if (!intent.hasExtra(EXTRA_PAGER_POSITION)) {
      Log.e(TAG, "movePagerToPositionInIntent: no position received.")
    } else {
      val divisor = min(1, pagerAdapter.itemCount)
      val pos = intent.getIntExtra(EXTRA_PAGER_POSITION, 0) % divisor
      viewPager.setCurrentItem(pos, true)
    }
  }


  // ------------------------ ACTIVITY EVENTS ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)
    initActivity(savedInstanceState)
  }

  override fun onResume() {
    gpsUtil.onResume()
    viewPager.registerOnPageChangeCallback(viewPagerPageChangeCallback)
    super.onResume()
  }

  override fun onPause() {
    gpsUtil.onPause()
    viewPager.unregisterOnPageChangeCallback(viewPagerPageChangeCallback)
    super.onPause()
  }

  override fun onDataUpdate() {
    updateBackgroundImageToMatchAdapterAt(viewPager.currentItem)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_CODE_CHECK_SETTINGS -> gpsUtil.onActivityResult(requestCode, resultCode)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_detail_activity, menu)

    val icon =
      if (app.getBooleanPref(KEY_GPS_NEEDED, false)) {
        ContextCompat.getDrawable(this, R.drawable.ic_location_on)
      } else {
        ContextCompat.getDrawable(this, R.drawable.ic_location_off)
      }

    menu.getItem(0).icon = icon
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_pin -> onPinButtonClicked(item)
      R.id.action_list -> onSavedButtonClicked()
      R.id.action_add -> onAddButtonClicked()
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun onPinButtonClicked(item: MenuItem): Boolean {
    val wasUsingGps = app.getBooleanPref(KEY_GPS_NEEDED, false)
    val isNowUsingGps = app.setBooleanPref(KEY_GPS_NEEDED, !wasUsingGps)

    if (isNowUsingGps) {
      Log.d(TAG, "onPinButtonClicked: Starting GPS updates.")
      gpsUtil.requestUpdates()
      item.icon = ContextCompat.getDrawable(this, R.drawable.ic_location_on)
    } else {
      Log.d(TAG, "onPinButtonClicked: Stopping GPS updates.")
      gpsUtil.stopRequestingUpdates()
      pagerItemUtil.disableCurrentLocation()
      item.icon = ContextCompat.getDrawable(this, R.drawable.ic_location_off)
    }
    return true
  }

  private fun onSavedButtonClicked(): Boolean {
    val intent = Intent(this, SavedActivity::class.java)
    movePagerResultLauncher.launch(intent)
    return true
  }

  private fun onAddButtonClicked(): Boolean {
    autocompleteUtil.getPlaceFromAutocomplete()
    return true
  }


  // ------------------------ INIT ------------------------

  private fun initActivity(savedInstanceState: Bundle?) {
    supportActionBar?.setDisplayShowTitleEnabled(false)
    app = application as BaseApplication
    viewModel = ViewModelProvider(this).get(BaseViewModel::class.java)
    backgroundImage = findViewById(R.id.background_image_view)
    instructionsTextView = findViewById(R.id.instructions_text_view)

    loadInstanceState(savedInstanceState)
    injectFields()
    initListeners()
    initViewPager()
  }

  private fun loadInstanceState(savedInstanceState: Bundle?) {
    Log.d(TAG, "loadInstanceState: no state loaded from bundle ($savedInstanceState)")
  }

  private fun injectFields() {
    val component = app
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
    pagerAdapter.setItemCountChangeListener(this)
    viewPager.adapter = pagerAdapter

    pagerItemUtil =
      PagerItemUtil(this, viewModel.getLiveLocations(), liveWeather, weatherUtil)

    pagerItemUtil.dataLive.observe(this) {
      pagerAdapter.setData(it)
      showInstructions(it.isEmpty())
    }
  }


  // ------------------------ LOCATION ------------------------

  override fun onGpsUpdate(address: Address?) {
    if (address == null) {
      Log.e(TAG, "onGpsUpdate: address is null")
      return
    }

    val currentLocationEntity = LocationEntity(
      address.locality,
      address.latitude.toFloat(),
      address.longitude.toFloat())

    pagerItemUtil.setCurrentLocation(currentLocationEntity)

    showInstructions(false)
  }

  override fun onWeatherReceived(weatherData: WeatherData, locationEntity: LocationEntity?) {
    if (isFirstLaunch) {
      updateBackgroundImageToMatchAdapterAt(viewPager.currentItem)
      isFirstLaunch = false
    }

    if (locationEntity != null) {
      pagerItemUtil.setWeather(locationEntity, weatherData)
    }
  }

  private fun updateBackgroundImageToMatchAdapterAt(position: Int) {
    val conditionId = pagerAdapter.getConditionIdFor(position)
    val resId = ConditionImageUtil.getResId(conditionId)

    glideInstance
      .asBitmap()
      .load(resId)
      .transition(BitmapTransitionOptions.withCrossFade())
      .into(backgroundImage)
  }


  // ------------------------ UTIL ------------------------

  private fun showInstructions(b: Boolean) {
    instructionsTextView.visibility = if (b) View.VISIBLE else View.GONE
  }

  companion object {
    private const val TAG = "GGG DetailActivity"
    const val EXTRA_PAGER_POSITION = "detail_pager_adapter_position"
  }
}