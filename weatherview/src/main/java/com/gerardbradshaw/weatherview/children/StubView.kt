package com.gerardbradshaw.weatherview.children

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.gerardbradshaw.weatherview.R

internal class StubView  : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val title: TextView
  private val detail: TextView

  init {
    View.inflate(context, R.layout.view_stub, this)

    title = findViewById(R.id.small_detail_title)
    detail = findViewById(R.id.small_detail_detail)
  }

  fun setInfo(title: String?, detail: String?) {
    this.title.text = title ?: ""
    this.detail.text = detail ?: ""
  }
}