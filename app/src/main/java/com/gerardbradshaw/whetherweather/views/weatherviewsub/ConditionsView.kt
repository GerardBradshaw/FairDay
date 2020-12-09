package com.gerardbradshaw.whetherweather.views.weatherviewsub

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gerardbradshaw.whetherweather.R

class ConditionsView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var conditionIcon: ImageView
  private var descriptionTextView: TextView

  init {
    View.inflate(context, R.layout.view_conditions, this)

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

        conditionIcon.contentDescription = context.getString(R.string.condition_content_desc_prefix) +
            description + context.getString(R.string.condition_content_desc_suffix)
      }

      descriptionTextView.text = description
    }
    else {
      visibility = View.INVISIBLE
    }
  }
}