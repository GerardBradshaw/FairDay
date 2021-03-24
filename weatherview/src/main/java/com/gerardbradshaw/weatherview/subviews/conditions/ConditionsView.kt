package com.gerardbradshaw.weatherview.subviews.conditions

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gerardbradshaw.weatherview.R

internal class ConditionsView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var conditionIcon: ImageView
  private var descriptionTextView: TextView

  init {
    View.inflate(context, R.layout.view_conditions_info, this)

    conditionIcon = findViewById(R.id.condition_icon)
    descriptionTextView = findViewById(R.id.description_text_view)
  }

  fun setConditions(condition: String?, description: String?, conditionIconId: String?) {
    if (condition != null && description != null) {
      visibility = View.VISIBLE

      if (conditionIconId != null) {
        val url = context.getString(R.string.condition_url_prefix) +
            conditionIconId + context.getString(R.string.condition_url_suffix)

        Glide
          .with(context)
          .load(url)
          .into(conditionIcon)

        conditionIcon.contentDescription = context.getString(R.string.cd_condition_description_prefix) +
            description + context.getString(R.string.cd_condition_description_suffix)
      }

      descriptionTextView.text = description
    }
    else {
      visibility = View.INVISIBLE
    }
  }
}