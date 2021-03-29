package com.gerardbradshaw.weatherview.children

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.gerardbradshaw.weatherview.R

internal class StubView  : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val titleOneTextView: TextView
  private val bodyOneTextView: TextView
  private val titleTwoTextView: TextView
  private val bodyTwoTextView: TextView

  init {
    View.inflate(context, R.layout.view_stub, this)

    titleOneTextView = findViewById(R.id.stub_title_one)
    bodyOneTextView = findViewById(R.id.stub_body_one)

    titleTwoTextView = findViewById(R.id.stub_title_two)
    bodyTwoTextView = findViewById(R.id.stub_body_two)
  }

  fun setBoth(titleOne: String?, bodyOne: String?, titleTwo: String, bodyTwo: String?) {
    setFirst(titleOne, bodyOne)
    setSecond(titleTwo, bodyTwo)
  }

  fun setFirst(title: String?, body: String?) {
    this.titleOneTextView.text = title ?: ""
    this.bodyOneTextView.text = body ?: ""
  }

  fun setSecond(title: String?, body: String?) {
    this.titleTwoTextView.text = title ?: ""
    this.bodyTwoTextView.text = body ?: ""
  }
}