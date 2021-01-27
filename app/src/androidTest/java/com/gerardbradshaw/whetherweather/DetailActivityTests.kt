package com.gerardbradshaw.whetherweather

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.gerardbradshaw.whetherweather.ui.detail.DetailActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.experimental.runners.Enclosed

@RunWith(Enclosed::class)
class DetailActivityTests {

  // ---------------- FIRST LAUNCH ----------------

  @RunWith(AndroidJUnit4::class)
  class FirstLaunchTests {
    @Rule
    @JvmField
    val asr = ActivityScenarioRule(DetailActivity::class.java)

    @Test
    fun should_useAppContext_when_launched() {
      val appContext = InstrumentationRegistry.getInstrumentation().targetContext
      assertEquals("com.gerardbradshaw.wheatherweather", appContext.packageName)
    }

    @Test
    fun should_showInstructions_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_haveTransparentActionBar_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_displayOpenWeatherCredit_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_askUserForLocationPermission_when_firstEntering() {
      fail("Not implemented")
    }
  }



  // ---------------- ACTION BAR ----------------

  @RunWith(AndroidJUnit4::class)
  class ActionBarTests {
    @Rule
    @JvmField
    val asr = ActivityScenarioRule(DetailActivity::class.java)

    @Test
    fun should_displayListButtonInActionBarMenu_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_displayAddButtonInActionBarMenu_when_firstEntering() {
      fail("Not implemented")
    }

    @Test
    fun should_launchSavedLocationsActivity_when_listButtonClickedInActionBarMenu() {
      fail("Not implemented")
    }

    @Test
    fun should_launchAddActivity_when_addButtonClickedInActionBarMenu() {
      fail("Not implemented")
    }
  }



  // ---------------- PAGER ADAPTER ----------------

  @RunWith(AndroidJUnit4::class)
  class AdapterTest {
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

    @Before
    fun setup() {
      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @Test
    fun should_showLoadingMessageAsLocationName_when_newLocationAdded() {
      fail("Not implemented")
    }

    @Test
    fun should_scrollToPosition_when_anotherLocationClickedInSavedLocationsActivity() {
      fail("Not implemented") // this should be in the tests for SavedLocationsActivity
    }

    @Test
    fun should_displayNoDuplicates_when_manyLocationsLoaded() {
      fail("Not implemented")
    }

    @Test
    fun should_displayAllLocationsFromDb_when_manyLocationsAddedToDb() {
      fail("Not implemented")
    }

    @Test
    fun should_displayWeatherForAnotherLocation_when_pageChanged() {
      fail("Not implemented")
    }

    @Test
    fun should_displayWeatherForCurrentLocation_when_weatherAtCurrentLocationEnabled() {
      fail("Not implemented")
    }

    @Test
    fun should_notDisplayWeatherForCurrentLocation_when_weatherAtCurrentLocationDisabled() {
      fail("Not implemented")
    }

    @Test
    fun should_displayWeatherForCurrentLocationInFirstPosition_when_weatherAtCurrentLocationEnabled() {
      fail("Not implemented")
    }
  }
}