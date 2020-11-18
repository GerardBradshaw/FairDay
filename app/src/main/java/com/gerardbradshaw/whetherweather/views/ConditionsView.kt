package com.gerardbradshaw.whetherweather.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.gerardbradshaw.whetherweather.R

class ConditionsView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var conditionTextView: TextView
  private var descriptionTextView: TextView

  init {
    View.inflate(context, R.layout.view_conditions, this)

    conditionTextView = findViewById(R.id.condition_text_view)
    descriptionTextView = findViewById(R.id.description_text_view)
  }

  fun setConditions(condition: String?, description: String?) {
    if (condition != null && description != null) {
      visibility = View.VISIBLE
      conditionTextView.text = condition
      descriptionTextView.text = description
    }
    else {
      visibility = View.INVISIBLE
    }
  }
}