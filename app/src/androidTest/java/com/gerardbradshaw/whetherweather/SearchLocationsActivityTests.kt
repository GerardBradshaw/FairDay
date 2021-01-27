package com.gerardbradshaw.whetherweather

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gerardbradshaw.whetherweather.ui.search.SearchActivity
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchLocationsActivityTests {

  @Rule
  @JvmField
  val asr = ActivityScenarioRule(SearchActivity::class.java)

  @Test
  fun should_addLocationToDb_when_locationClicked() {
    fail("Not implemented")
  }

  @Test
  fun should_finishActivity_when_locationClicked() {
    fail("Not implemented")
  }
}