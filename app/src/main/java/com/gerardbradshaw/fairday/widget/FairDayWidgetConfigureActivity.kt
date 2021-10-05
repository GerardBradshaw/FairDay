package com.gerardbradshaw.fairday.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.databinding.FairDayWidgetConfigureBinding

/**
 * The configuration screen for the [FairDayWidgetProvider] AppWidget.
 */
class FairDayWidgetConfigureActivity : Activity() {
  private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
  private lateinit var editText: EditText

  private var doneButtonOnClickListener = View.OnClickListener {
    val context = this@FairDayWidgetConfigureActivity

    // When the button is clicked, store the string locally
    val widgetText = editText.text.toString()
    saveTitleToSharedPref(context, appWidgetId, widgetText)

    // It is the responsibility of the configuration activity to update the app widget
    val appWidgetManager = AppWidgetManager.getInstance(context)
//    updateAppWidget(context, appWidgetManager, appWidgetId)

    // Make sure we pass back the original appWidgetId
    val resultValue = Intent()
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    setResult(RESULT_OK, resultValue)
    finish()
  }

  private lateinit var binding: FairDayWidgetConfigureBinding

  public override fun onCreate(icicle: Bundle?) {
    super.onCreate(icicle)

    // Set the result to CANCELED.  This will cause the widget host to cancel
    // out of the widget placement if the user presses the back button.
    setResult(RESULT_CANCELED)

    binding = FairDayWidgetConfigureBinding.inflate(layoutInflater)
    setContentView(binding.root)

    editText = binding.appwidgetText as EditText
    binding.addButton.setOnClickListener(doneButtonOnClickListener)

    // Find the widget id from the intent.
    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      appWidgetId = extras.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
      )
    }

    // If this activity was started with an intent without an app widget ID, finish with an error.
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    editText.setText(loadTitleSharedPref(this@FairDayWidgetConfigureActivity, appWidgetId))
  }

}

private const val PREFS_NAME = "com.gerardbradshaw.fairday.FairDayWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

internal fun saveTitleToSharedPref(context: Context, appWidgetId: Int, text: String) {
  context.getSharedPreferences(PREFS_NAME, 0).edit().apply {
    putString(PREF_PREFIX_KEY + appWidgetId, text)
    apply()
  }
}

internal fun loadTitleSharedPref(context: Context, appWidgetId: Int): String {
  val prefs = context.getSharedPreferences(PREFS_NAME, 0)
  val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
  return titleValue ?: context.getString(R.string.app_widget_description)
}

internal fun deleteTitleSharedPref(context: Context, appWidgetId: Int) {
  context.getSharedPreferences(PREFS_NAME, 0).edit().apply {
    remove(PREF_PREFIX_KEY + appWidgetId)
    apply()
  }
}