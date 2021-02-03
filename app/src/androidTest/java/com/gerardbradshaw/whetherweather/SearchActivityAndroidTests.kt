package com.gerardbradshaw.whetherweather

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.viewpager2.widget.ViewPager2
import com.gerardbradshaw.whetherweather.activities.search.SearchActivity
import com.gerardbradshaw.whetherweather.application.BaseApplication
import com.gerardbradshaw.whetherweather.util.MyMockServer
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.Assert.fail
import org.junit.runner.RunWith
import kotlin.math.max
import kotlin.math.min

@RunWith(AndroidJUnit4::class)
class SearchActivityAndroidTests {
  lateinit var activityScenario: ActivityScenario<SearchActivity>
  lateinit var activity: SearchActivity
  lateinit var mockWebServer: MyMockServer

  @get:Rule
  val runtimePermissionRule: GrantPermissionRule? =
    GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION)

  @Before
  fun setup() {
    mockWebServer = MyMockServer()
    mockWebServer.start()

    ApplicationProvider.getApplicationContext<BaseApplication>().prepareForTests(mockWebServer.url())

    activityScenario = ActivityScenario.launch(SearchActivity::class.java)
    activityScenario.onActivity { activity = it }
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
  }

  // -------------------------------- TESTS --------------------------------
  @Test
  fun should_addLocationToDb_when_locationClicked() {

    fail("Not implemented")
  }

  @Test
  fun should_finishActivity_when_locationClicked() {
    fail("Not implemented")
  }

  @Ignore("Helper class")
  private abstract class SearchActivityAndroidTestUtil {
    companion object {
      private const val TAG = "GGG SearchActivityAndro"

      @JvmStatic
      fun typeIntoAutocompleteFragment(str: String) {
        onAutocompleteFragment()
          .perform(enterText(str))
      }

      @JvmStatic
      private fun onAutocompleteFragment(): ViewInteraction {
        return onView(allOf(withId(R.id.autocomplete_fragment), isDisplayed()))
      }

      @JvmStatic
      private fun enterText(str: String): ViewAction {
        return object : ViewAction {
          override fun getConstraints() = isDisplayed()

          override fun getDescription() = "Enter text"

          override fun perform(uiController: UiController?, view: View?) {
            TODO()
//            val fragment = view as
//
//            val itemCount = pager.adapter?.itemCount ?: return
//            val maxPos = itemCount - if(itemCount > 0) 1 else 0
//
//            pager.currentItem = max(0, min(maxPos, str))
          }
        }
      }
    }
  }

}