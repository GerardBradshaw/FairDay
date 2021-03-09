package com.gerardbradshaw.fairday

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gerardbradshaw.fairday.activities.saved.SavedActivity
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedActivityAndroidTests {
  @Rule
  @JvmField
  val asr = ActivityScenarioRule(SavedActivity::class.java)

  @Test
  fun should_showInstructions_when_noLocationsAddedToDb() {
    fail("Not implemented")
  }

  @Test
  fun should_haveTransparentActionBar_when_firstEntering() {
    fail("Not implemented")
  }

  @Test
  fun should_haveFab_when_firstEntering() {
    fail("Not implemented")
  }

  @Test
  fun should_launchAddLocationActivity_when_fabClicked() {
    fail("Not implemented")
  }

  @Test
  fun should_launchDetailActivity_when_locationClicked() {
    fail("Not implemented")
  }

  @Test
  fun should_scrollToCorrectLocationInDetailActivityAdapter_when_locationClicked() {
    fail("Not implemented")
  }

  @Test
  fun should_showLocationInRecycler_when_newLocationAddedInSearchActivity() {
    fail("Not implemented")
  }
}