package com.gerardbradshaw.fairday.activities.detail

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Address
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.fairday.Constants
import com.gerardbradshaw.fairday.application.BaseApplication
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.activities.utils.AutocompleteUtil
import com.gerardbradshaw.fairday.room.LocationEntity
import com.gerardbradshaw.fairday.activities.utils.BaseViewModel
import com.gerardbradshaw.fairday.activities.detail.utils.ConditionUtil
import com.gerardbradshaw.fairday.activities.detail.utils.GpsUtil
import com.gerardbradshaw.fairday.activities.saved.SavedActivity
import com.gerardbradshaw.fairday.activities.detail.utils.GpsUtil.Companion.REQUEST_CODE_CHECK_SETTINGS
import com.gerardbradshaw.fairday.activities.detail.utils.OpenWeatherCreditView
import com.gerardbradshaw.fairday.activities.detail.utils.WeatherUtil
import com.gerardbradshaw.fairday.activities.detail.viewpager.DetailPagerAdapter
import com.gerardbradshaw.fairday.activities.detail.viewpager.PagerItemUtil
import com.gerardbradshaw.fairday.activities.utils.DrawableAlwaysCrossFadeFactory
import com.github.matteobattilana.weather.PrecipitationType
import com.github.matteobattilana.weather.WeatherView
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class DetailActivity :
  AppCompatActivity(),
  GpsUtil.GpsUtilListener,
  WeatherUtil.WeatherDetailsListener,
  DetailPagerAdapter.DataChangeListener,
  SharedPreferences.OnSharedPreferenceChangeListener {
  private lateinit var app: BaseApplication
  private lateinit var viewModel: BaseViewModel
  private lateinit var backgroundImage: ImageView
  private lateinit var instructionsTextView: TextView
  private lateinit var precipitationAnimationView: WeatherView
  private lateinit var viewPager: ViewPager2
  private lateinit var pagerItemUtil: PagerItemUtil
  private lateinit var optionsMenu: Menu
  private lateinit var prefs: SharedPreferences

  @Inject
  lateinit var pagerAdapter: DetailPagerAdapter
  @Inject
  lateinit var gpsUtil: GpsUtil
  @Inject
  lateinit var weatherUtil: WeatherUtil
  @Inject
  lateinit var glideInstance: RequestManager
  @Inject
  lateinit var autocompleteUtil: AutocompleteUtil




  // ------------------------ ACTIVITY WAKE ------------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)
    initActivity(savedInstanceState)
  }

  override fun onResume() {
    viewPager.registerOnPageChangeCallback(viewPagerPageChangeCallback)
    initListeners()
    gpsUtil.start()
    super.onResume()
  }

  private fun initListeners() {
    weatherUtil.setWeatherDetailsListener(this)
    gpsUtil.setOnGpsUpdateListener(this)

    findViewById<OpenWeatherCreditView>(R.id.open_weather_credit_view).setOnClickListener {
      Intent(Intent.ACTION_VIEW).also {
        it.data = Uri.parse(Constants.URL_OPEN_WEATHER)
        startActivity(it)
      }
    }

    prefs.registerOnSharedPreferenceChangeListener(this)
  }

  private val viewPagerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
      pagerAdapter.getWeatherDataFor(position)?.let { updateBackgroundViews(it) }
      super.onPageSelected(position)
    }
  }


  // ------------------------ INIT ------------------------

  private fun initActivity(savedInstanceState: Bundle?) {
    app = application as BaseApplication
    viewModel = ViewModelProvider(this).get(BaseViewModel::class.java)

    initFields()
    injectFields()
    initPager()
    loadInstanceState(savedInstanceState)
  }

  private fun initFields() {
    prefs = PreferenceManager.getDefaultSharedPreferences(this)
    backgroundImage = findViewById(R.id.background_image_view)
    instructionsTextView = findViewById(R.id.instructions_text_view)
    precipitationAnimationView = findViewById(R.id.weather_view)
  }

  private fun injectFields() {
    val component = app
      .getAppComponent()
      .getDetailActivityComponentFactory()
      .create(this, this)

    component.inject(this)
  }

  private fun initPager() {
    viewPager = findViewById(R.id.view_pager)
    pagerAdapter = DetailPagerAdapter(this)
    pagerAdapter.setItemCountChangeListener(this)
    viewPager.adapter = pagerAdapter

    pagerItemUtil = PagerItemUtil(
      this,
      viewModel.getLiveLocations(),
      MutableLiveData(),
      weatherUtil)

    pagerItemUtil.dataLive.observe(this) {
      pagerAdapter.setData(it)
      showInstructions(it.isEmpty())
    }
  }

  private fun loadInstanceState(savedInstanceState: Bundle?) {
    Log.d(TAG, "loadInstanceState: no state loaded from bundle ($savedInstanceState)")
  }


  // ------------------------ ACTIVITY SLEEP ------------------------

  override fun onPause() {
    gpsUtil.stop()
    unregisterListeners()
    super.onPause()
  }

  private fun unregisterListeners() {
    viewPager.unregisterOnPageChangeCallback(viewPagerPageChangeCallback)
    prefs.unregisterOnSharedPreferenceChangeListener(this)
  }


  // ------------------------ ACTION MENU ------------------------

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    optionsMenu = menu
    menuInflater.inflate(R.menu.menu_action_bar_detail_activity, menu)
    supportActionBar?.setDisplayShowTitleEnabled(false)

    onSharedPreferenceChanged(prefs, Constants.KEY_GPS_REQUESTED)

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_add -> onAddButtonClicked()
      R.id.action_pin -> onPinButtonClicked()
      R.id.action_list -> onListButtonClicked()
      R.id.action_refresh -> onRefreshButtonClicked()
      R.id.action_more_apps -> onMoreAppsButtonClicked()
      R.id.action_libraries_used -> onLibrariesUsedButtonClicked()
      else -> return super.onOptionsItemSelected(item)
    }
    return true
  }

  private fun onAddButtonClicked() {
    autocompleteUtil.getPlaceFromAutocomplete()
  }

  private fun onPinButtonClicked() {
    val isGpsRequestedOldPref = prefs.getBoolean(Constants.KEY_GPS_REQUESTED, false)
    prefs.edit().putBoolean(Constants.KEY_GPS_REQUESTED, !isGpsRequestedOldPref).apply()

    val isGpsEnabled = gpsUtil.toggleUpdateState()
    if (!isGpsEnabled) pagerItemUtil.disableCurrentLocation()
  }

  private fun setPinIconState(state: PinState) {
    optionsMenu.findItem(R.id.action_pin)?.let {
      when (state) {
        PinState.PIN_ENABLED -> {
          it.icon = ContextCompat.getDrawable(this, R.drawable.ic_pin_on)
          it.title = getString(R.string.string_disable_location_services)
        }
        PinState.PIN_DISABLED -> {
          it.icon = ContextCompat.getDrawable(this, R.drawable.ic_pin_off)
          it.title = getString(R.string.string_enable_location_services)
        }
      }

      it.icon.setTint(Color.WHITE)
    }
  }

  private fun onListButtonClicked() {
    val intent = Intent(this, SavedActivity::class.java)
    savedActivityResultLauncher.launch(intent)
  }

  private val savedActivityResultLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      when (it.resultCode) {
        RESULT_CANCELED -> Log.i(TAG, "movePagerToPosition: no place selected.")
        RESULT_OK -> {
          val intentPos = it.data?.getIntExtra(EXTRA_PAGER_POSITION, -1) ?: -1
          if (intentPos == -1) return@registerForActivityResult

          val offset = if (prefs.getBoolean(Constants.KEY_GPS_REQUESTED, false)) 1 else 0
          updatePagerPosition(intentPos + offset)
        }
      }
    }

  private fun updatePagerPosition(position: Int) {
    if (position != -1) viewPager.setCurrentItem(position, true)
    else Log.i(TAG, "updatePagerPosition: no update as position was null")
  }

  private fun onRefreshButtonClicked() {
    pagerItemUtil.refreshWeather()
  }

  private fun onMoreAppsButtonClicked() {
    Intent(Intent.ACTION_VIEW).also {
      it.data = Uri.parse(Constants.URL_PLAY_STORE)
      startActivity(it)
    }
  }

  private fun onLibrariesUsedButtonClicked() {
    val text = readFile(resources.openRawResource(R.raw.open_source_libraries_used))

    val layout = layoutInflater.inflate(R.layout.dialog_libraries_used, null)
    val textView: TextView = layout.findViewById(R.id.libraries_used_text_view)
    textView.text = text

    val dialog = AlertDialog.Builder(this)
      .setTitle(getString(R.string.string_open_source_libraries_used))
      .setView(layout)
      .setPositiveButton("OK", null)
      .create()

    dialog.show()
  }

  private fun readFile(inputStream: InputStream): String {
    try {
      val bytes = ByteArray(inputStream.available())
      inputStream.read(bytes, 0, bytes.size)
      return String(bytes)

    } catch (e: IOException) {
      Log.e(TAG, "readFile: failed to read file.", e)
      return "An error occurred"
    }
  }


  // ------------------------ EXTERNAL EVENTS ------------------------

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_CODE_CHECK_SETTINGS -> gpsUtil.onActivityResult(requestCode, resultCode)
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    if (sharedPreferences != null && key == Constants.KEY_GPS_REQUESTED) {
      val isLocationOn = sharedPreferences.getBoolean(key, false)
      val pinState = if (isLocationOn) PinState.PIN_ENABLED else PinState.PIN_DISABLED
      setPinIconState(pinState)
    }
  }

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

  private fun showInstructions(b: Boolean) {
    instructionsTextView.visibility = if (b) View.VISIBLE else View.GONE
  }

  override fun onWeatherReceived(weatherData: WeatherData, locationEntity: LocationEntity?) {
    if (locationEntity != null) {
      pagerItemUtil.setWeather(locationEntity, weatherData)
    }
  }

  private fun updateBackgroundViews(weatherData: WeatherData?) {
    updateBackgroundImage(weatherData)
    updatePrecipitationView(weatherData)
    updateCloudView(weatherData)
  }

  private fun updateBackgroundImage(weatherData: WeatherData?) {
    val conditionImageResId = ConditionUtil.getConditionImageResId(weatherData?.conditionIconId)

    glideInstance
      .load(conditionImageResId)
      .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
      .into(backgroundImage)

    Log.d(TAG, "updateBackgroundImage: updated image for ${weatherData?.name}")
  }

  private fun updatePrecipitationView(weatherData: WeatherData?) {
    val precipitationType =
      if (weatherData == null) PrecipitationType.CLEAR
      else ConditionUtil.getPrecipitationType(weatherData.weatherId)

    precipitationAnimationView.setWeatherData(precipitationType)

    if (weatherData != null) {
      val windSpeed = weatherData.windSpeed ?: 0f
      val speedFactor = if (windSpeed / 20f > 1f) 1f else windSpeed / 20f
      val windDirection = (weatherData.windDirection?.toInt() ?: 0) % 360

      val angle = speedFactor * if (windDirection <= 180) 60 else -60
      precipitationAnimationView.angle = angle.toInt()
    }
  }

  private fun updateCloudView(weatherData: WeatherData?) {
    val cloudType = ConditionUtil.getCloudType(weatherData?.conditionIconId)

    // cloudView.setType(cloudType)
  }

  override fun onDataUpdate() {
    pagerAdapter.getWeatherDataFor(viewPager.currentItem)?.let { updateBackgroundViews(it) }
  }


  // ------------------------ UTIL ------------------------

  enum class PinState {
    PIN_ENABLED, PIN_DISABLED
  }

  companion object {
    private const val TAG = "GGG DetailActivity"
    const val EXTRA_PAGER_POSITION = "detail_pager_adapter_position"
  }
}