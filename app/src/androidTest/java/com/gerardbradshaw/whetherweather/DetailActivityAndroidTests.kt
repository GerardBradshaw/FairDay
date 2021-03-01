package com.gerardbradshaw.whetherweather

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.viewpager2.widget.ViewPager2
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkPermissionRationaleDisplayed
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkPinMenuItemHasTitle
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkSystemPermissionRequestDisplayed
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkToastIsDisplayedContaining
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkViewPagerHasItemCount
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkWeatherIsDisplayedForCurrentLocation
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.clickDialogButton
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.clickPinMenuItem
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.DialogButton.*
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.checkWeatherIsDisplayedForLocation
import com.gerardbradshaw.whetherweather.DetailActivityAndroidTests.DetailActivityAndroidTestUtil.Companion.scrollViewPagerToPosition
import com.gerardbradshaw.whetherweather.activities.detail.DetailActivity
import com.gerardbradshaw.whetherweather.activities.saved.SavedActivity
import com.gerardbradshaw.whetherweather.application.BaseApplication
import com.gerardbradshaw.whetherweather.util.DataBindingIdlingResourceRule
import com.gerardbradshaw.whetherweather.util.MockRepo
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

  @RunWith(AndroidJUnit4::class)
  class FirstLaunchTests {
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity
    lateinit var mockRepo: MockRepo

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setup() {
      mockRepo = MockRepo()
      mockRepo.start()
      ApplicationProvider.getApplicationContext<BaseApplication>()
        .prepareForTests(mockRepo.url())

      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @After
    fun tearDown() {
      mockRepo.shutdown()
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

  @Ignore("Below tests will fail (unable to prevent permissions from being automatically granted). Manually test if required.")
  class PermissionsTests {
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity
    lateinit var mockRepo: MockRepo

    @Before
    fun setup() {
      mockRepo = MockRepo()
      mockRepo.start()
      ApplicationProvider.getApplicationContext<BaseApplication>()
        .prepareForTests(mockRepo.url())

      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @After
    fun tearDown() {
      mockRepo.shutdown()

      InstrumentationRegistry.getInstrumentation().uiAutomation
        .executeShellCommand(
          "pm revoke ${android.Manifest.permission.ACCESS_FINE_LOCATION}")
    }

    // -------------------------------- TESTS --------------------------------

    @Test
    fun should_displayPermissionRationale_when_weatherAtCurrentLocationRequestedForTheFirstTime() {
      clickPinMenuItem()
      checkPermissionRationaleDisplayed(true)
    }

    @Test
    fun should_displayPermissionRequest_when_permissionRationaleAccepted() {
      clickPinMenuItem()
      clickDialogButton(POSITIVE)
      checkSystemPermissionRequestDisplayed(true)
    }

    @Test
    fun should_notDisplayPermissionRequest_when_permissionRationaleDismissed() {
      clickPinMenuItem()
      clickDialogButton(NEGATIVE)
      checkSystemPermissionRequestDisplayed(false)
    }

    @Test
    fun should_displayPermissionRationale_when_previouslyDismissedAndLocationRequestedAgain() {
      clickPinMenuItem()
      clickDialogButton(NEGATIVE)
      clickPinMenuItem()
      checkPermissionRationaleDisplayed(true)
    }

    @Test
    fun should_notDisplayPermissionRationale_when_previousRationaleAcceptedAndLocationRequested() {
      clickPinMenuItem()
      clickDialogButton(POSITIVE)
      clickDialogButton(NEGATIVE)
      clickPinMenuItem()
      checkSystemPermissionRequestDisplayed(true)
    }

    @Test
    fun should_displayWeatherAtCurrentLocation_when_permissionRequestGranted() {
      clickPinMenuItem()
      clickDialogButton(POSITIVE)
      clickDialogButton(POSITIVE)
      checkWeatherIsDisplayedForCurrentLocation()
    }

    @Test
    fun should_displayErrorToast_when_permissionRequestDenied() {
      clickPinMenuItem()
      clickDialogButton(POSITIVE)
      clickDialogButton(NEGATIVE)
      checkToastIsDisplayedContaining("error", activity)
    }

    @Test
    fun should_notDisplayErrorToast_when_permissionRequestIgnored() {
      clickPinMenuItem()
      clickDialogButton(POSITIVE)
      fail("Need to dismiss permission request") // TODO
    }
  }

  @RunWith(AndroidJUnit4::class)
  class ActionBarTests {
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity
    lateinit var mockRepo: MockRepo

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setup() {
      mockRepo = MockRepo()
      mockRepo.start()
      ApplicationProvider.getApplicationContext<BaseApplication>()
        .prepareForTests(mockRepo.url())

      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @After
    fun tearDown() {
      mockRepo.shutdown()
    }

    // -------------------------------- TESTS --------------------------------

    @Test
    fun should_displayLocationOffPinButton_when_firstEntering() {
      checkPinMenuItemHasTitle(activity.getString(R.string.string_enable_location_services))
    }

    @Test
    fun should_displayLocationOnPinButton_when_pinFirstClicked() {
      clickPinMenuItem()
      checkPinMenuItemHasTitle(activity.getString(R.string.string_disable_location_services))
    }

    @Test
    fun should_displayLocationOffPinButton_when_pinFirstClickedTwice() {
      clickPinMenuItem()
      clickPinMenuItem()
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
    lateinit var mockRepo: MockRepo
    lateinit var activityScenario: ActivityScenario<DetailActivity>
    lateinit var activity: DetailActivity

    @Before
    fun setup() {
      mockRepo = MockRepo()
      mockRepo.start()

      ApplicationProvider.getApplicationContext<BaseApplication>().prepareForTests(mockRepo.url())

      activityScenario = ActivityScenario.launch(DetailActivity::class.java)
      activityScenario.onActivity { activity = it }
    }

    @After
    fun tearDown() {
      ApplicationProvider.getApplicationContext<BaseApplication>().wipeDb()
      mockRepo.shutdown()
    }

    @Test
    fun should_haveEmptyPager_when_firstLaunched() {
      checkViewPagerHasItemCount(0)
    }

    @Test
    fun should_displayInstructions_when_firstLaunched() {
      onView(withId(R.id.instructions_text_view))
        .check(matches(isDisplayed()))
    }

    @Test
    fun should_addAnItemToPager_when_newLocationAddedToDb() {
      mockRepo.addAllLocations()
      checkViewPagerHasItemCount(1)
    }

    @Test
    fun should_displayWeatherAtCurrentLocation_when_locationEnabled() {
      clickPinMenuItem()
      scrollViewPagerToPosition(0)
      checkWeatherIsDisplayedForCurrentLocation()
    }

    @Test
    fun should_displayNoDuplicates_when_manyLocationsLoaded() {
      mockRepo.addAllLocations()
      mockRepo.addLocation(MockRepo.MockLocation.LOCATION_0)
      checkViewPagerHasItemCount(3)
    }

    @Test
    fun should_displayWeatherForAnotherLocation_when_pageChanged() {
      mockRepo.addAllLocations()
      scrollViewPagerToPosition(0)
      scrollViewPagerToPosition(2)
      checkWeatherIsDisplayedForLocation(MockRepo.MockLocation.LOCATION_2)
    }
  }

  @Ignore("Helper class")
  private abstract class DetailActivityAndroidTestUtil {
    companion object {
      private const val TAG = "GGG - DAAndroidTest"

      // -------- VIEW PAGER --------
      private fun onViewPager(): ViewInteraction {
        return onView(allOf(withId(R.id.view_pager), isDisplayed()))
      }

      @JvmStatic
      fun checkViewPagerHasItemCount(n: Int) {
        onViewPager().check(matches(hasItemCount(n)))
      }

      private fun hasItemCount(expected: Int): Matcher<View?> {
        return object : BoundedMatcher<View?, ViewPager2>(ViewPager2::class.java) {
          var actual: Int? = null

          override fun matchesSafely(view: ViewPager2): Boolean {
            actual = view.adapter?.itemCount ?: -1
            Log.d(TAG, "matchesSafely: Expected/Actual = $expected/$actual")
            return expected == actual
          }

          override fun describeTo(description: Description) {
            description.appendText("Matches on ViewPager2 with same item count).")
          }
        }
      }

      @JvmStatic
      fun checkWeatherIsDisplayedForLocation(location: MockRepo.MockLocation) {
        onView(withText(containsString("Location${location.ordinal}")))
          .check(matches(isDisplayed()))
      }

      @JvmStatic
      fun checkWeatherIsDisplayedForCurrentLocation() {
        onView(allOf(withId(R.id.location_pin_icon), isDisplayed()))
          .check(matches(isDisplayed()))
      }

      @JvmStatic
      fun scrollViewPagerToPosition(p: Int): ViewInteraction {
        return onViewPager().perform(scrollToPosition(p))
      }

      private fun scrollToPosition(p: Int): ViewAction {
        return object : ViewAction {
          override fun getConstraints() = isDisplayed()

          override fun getDescription() = "Position update"

          override fun perform(uiController: UiController?, view: View?) {
            with(view as ViewPager2) {
              val itemCount = adapter?.itemCount ?: return
              val maxPos = itemCount - if(itemCount > 0) 1 else 0
              currentItem = max(0, min(maxPos, p))
            }
          }
        }
      }


      // -------- ACTION BAR --------
      @JvmStatic
      fun clickPinMenuItem() {
        onView(withId(R.id.action_pin)).perform(click())
      }

      @JvmStatic
      fun checkPinMenuItemHasTitle(title: String) {
        onView(allOf(withId(R.id.action_pin), isDisplayed()))
          .check(matches(hasTitle(title)))
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


      // -------- DIALOGUES --------
      @JvmStatic
      fun checkPermissionRationaleDisplayed(expected: Boolean) {
        val matchesExpected = if (expected) matches(isDisplayed()) else doesNotExist()

        onView(withText("Permission required"))
          .check(matchesExpected)
      }

      @JvmStatic
      fun clickDialogButton(button: DialogButton) {
        val buttonId = when (button) {
          POSITIVE -> android.R.id.button1
          NEGATIVE -> android.R.id.button2
          NEUTRAL -> android.R.id.button3
        }

        onView(withId(buttonId))
          .perform(click())
      }

      @JvmStatic
      fun checkSystemPermissionRequestDisplayed(expected: Boolean) {
        val matchesExpected = if (expected) matches(isDisplayed()) else doesNotExist()

        onView(withText("would like to"))
          .check(matchesExpected)
      }

      @JvmStatic
      fun checkToastIsDisplayedContaining(string: String, activity: AppCompatActivity) {
        onView(withText(string))
          .inRoot(withDecorView(not(`is`(activity.window.decorView))))
          .check(matches(isDisplayed()))
      }

      enum class DialogButton {
        POSITIVE, NEUTRAL, NEGATIVE
      }
    }
  }
}