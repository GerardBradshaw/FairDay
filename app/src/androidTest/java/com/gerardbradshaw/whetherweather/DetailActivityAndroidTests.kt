package com.gerardbradshaw.whetherweather

import android.view.View
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.viewpager2.widget.ViewPager2
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkPinMenuItemHasTitle
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkViewPagerHasItemCount
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkWeatherIsDisplayedForCurrentLocation
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkWeatherIsDisplayedForLocation
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.scrollViewPagerToPosition
import com.gerardbradshaw.whetherweather.activities.detail.DetailActivity
import com.gerardbradshaw.whetherweather.activities.saved.SavedActivity
import com.gerardbradshaw.whetherweather.application.BaseApplication
import com.gerardbradshaw.whetherweather.util.MyMockServer
import com.google.android.libraries.places.widget.AutocompleteActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.Assert.fail
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import kotlin.math.max
import kotlin.math.min

@RunWith(Enclosed::class)
class DetailActivityAndroidTests {
// done
  @RunWith(AndroidJUnit4::class)
  class FirstLaunchTests {
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity
    lateinit var mockWebServer: MyMockServer

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setup() {
      mockWebServer = MyMockServer()
      mockWebServer.start()
      ApplicationProvider.getApplicationContext<BaseApplication>()
        .prepareForTests(mockWebServer.url())

      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @After
    fun tearDown() {
      mockWebServer.shutdown()
    }

    // -------------------------------- TESTS --------------------------------
    @Test
    fun should_showInstructions_when_firstEntering() {
      onView(withId(R.id.instructions_text_view))
        .check(matches(isDisplayed()))
    }

    @Test
    fun should_displayOpenWeatherCredit_when_firstEntering() {
      onView(withId(R.id.open_weather_credit_view))
        .check(matches(isDisplayed()))
    }
  }

// done
  @RunWith(AndroidJUnit4::class)
  class ActionBarTests {
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity
    lateinit var mockWebServer: MyMockServer

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setup() {
      mockWebServer = MyMockServer()
      mockWebServer.start()
      ApplicationProvider.getApplicationContext<BaseApplication>()
        .prepareForTests(mockWebServer.url())

      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @After
    fun tearDown() {
      mockWebServer.shutdown()
    }

    // -------------------------------- TESTS --------------------------------

    @Test
    fun should_displayLocationOffPinButton_when_firstEntering() {
      checkPinMenuItemHasTitle(activity.getString(R.string.string_enable_location_services))
    }

    @Test
    fun should_displayLocationOnPinButton_when_pinFirstClicked() {
      onView(withId(R.id.action_pin)).perform(click())
      checkPinMenuItemHasTitle(activity.getString(R.string.string_disable_location_services))
    }

    @Test
    fun should_displayLocationOffPinButton_when_pinFirstClickedTwice() {
      onView(withId(R.id.action_pin)).perform(click())
      onView(withId(R.id.action_pin)).perform(click())
      checkPinMenuItemHasTitle(activity.getString(R.string.string_enable_location_services))
    }

    @Test
    fun should_displayListButton_when_firstEntering() {
      onView(withId(R.id.action_list))
        .check(matches(isDisplayed()))
    }

    @Test
    fun should_launchSavedLocationsActivity_when_listButtonClicked() {
      Intents.init()
      onView(withId(R.id.action_list)).perform(click())
      intended(hasComponent(SavedActivity::class.java.name))
      Intents.release()
    }

    @Test
    fun should_displayAddButton_when_firstEntering() {
      onView(withId(R.id.action_add))
        .check(matches(isDisplayed()))
    }

    @Test
    fun should_openAutocompleteFragment_when_addButtonClicked() {
      Intents.init()
      onView(withId(R.id.action_add)).perform(click())
      intended(hasComponent(AutocompleteActivity::class.java.name))
      Intents.release()
    }
  }


  @RunWith(AndroidJUnit4::class)
  class PagerTests {
    lateinit var mockWebServer: MyMockServer
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setup() {
      mockWebServer = MyMockServer()
      mockWebServer.start()

      ApplicationProvider.getApplicationContext<BaseApplication>()
        .prepareForTests(mockWebServer.url())

      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @After
    fun tearDown() {
      mockWebServer.shutdown()
    }

    // -------------------------------- TESTS --------------------------------
    @Test
    fun should_haveSingleItemInPager_when_firstLaunched() {
      checkViewPagerHasItemCount(1)
    }

    @Test
    fun should_addLocationToViewPager_when_newLocationAddedToDb() {
      mockWebServer.addLocation(0)
      checkViewPagerHasItemCount(2)
    }

    @Test
    fun should_showLoadingMessageAsLocationName_when_newLocationAdded() {
      onView(allOf(withId(R.id.location_text_view), isDisplayed()))
        .check((matches(withText(containsString(activity.resources.getString(R.string.string_loading))))))
    }

    @Test
    fun should_displayNoDuplicates_when_manyLocationsLoaded() {
      mockWebServer.addAllLocations()
      checkViewPagerHasItemCount(4)
    }

    @Test
    fun should_displayWeatherForAnotherLocation_when_pageChanged() {
      mockWebServer.addAllLocations()

      scrollViewPagerToPosition(0)
      scrollViewPagerToPosition(2)
      checkWeatherIsDisplayedForLocation(2)
    }

    @Test
    fun should_displayWeatherForCurrentLocation_when_weatherAtCurrentLocationEnabled() {
      checkWeatherIsDisplayedForCurrentLocation()
    }
  }



  @Ignore("Helper class")
  private abstract class DetailActivityAndroidTestUtil {
    companion object {
      @JvmStatic
      fun checkViewPagerHasItemCount(n: Int) {
        onViewPager().check(matches(hasItemCount(n)))
      }

      @JvmStatic
      fun checkWeatherIsDisplayedForLocation(n: Int) {
        onView(allOf(withId(R.id.location_text_view), isDisplayed()))
          .check(matches(withText(containsString("Location$n"))))
      }

      @JvmStatic
      fun checkWeatherIsDisplayedForCurrentLocation() {
        onView(allOf(withId(R.id.location_pin_icon), isDisplayed()))
          .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.location_text_view), isDisplayed()))
          .check(matches(withText(containsString("Santa Clara"))))
      }

      @JvmStatic
      fun checkPinMenuItemHasTitle(title: String) {
        onView(allOf(withId(R.id.action_pin), isDisplayed()))
          .check(matches(hasTitle(title)))
      }

      @JvmStatic
      fun scrollViewPagerToPosition(p: Int): ViewInteraction {
        return onViewPager().perform(scrollToPosition(p))
      }

      private fun onViewPager(): ViewInteraction {
        return onView(allOf(withId(R.id.view_pager), isDisplayed()))
      }

      private fun hasItemCount(count: Int): Matcher<View?> {
        return object : BoundedMatcher<View?, ViewPager2>(ViewPager2::class.java) {
          override fun matchesSafely(view: ViewPager2): Boolean {
            return count == view.adapter?.itemCount ?: -1
          }

          override fun describeTo(description: Description) {
            description.appendText("Matches on ViewPager2 with same item count.")
          }
        }
      }

      private fun scrollToPosition(p: Int): ViewAction {
        return object : ViewAction {
          override fun getConstraints() = isDisplayed()

          override fun getDescription() = "Position update"

          override fun perform(uiController: UiController?, view: View?) {
            uiController?.loopMainThreadForAtLeast(1000)

            val pager = view as ViewPager2

            val itemCount = pager.adapter?.itemCount ?: return
            val maxPos = itemCount - if(itemCount > 0) 1 else 0

            pager.currentItem = max(0, min(maxPos, p))
          }
        }
      }

      private fun hasTitle(expected: String): Matcher<View?> {
        return object : BoundedMatcher<View?, ActionMenuItemView>(ActionMenuItemView::class.java) {
          override fun matchesSafely(view: ActionMenuItemView): Boolean {
            return view.itemData.title == expected
          }

          override fun describeTo(description: Description) {
            description.appendText("has title $expected.")
          }
        }
      }
    }
  }
}