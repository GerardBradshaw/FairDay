package com.gerardbradshaw.whetherweather.ui.find

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.ui.detail.DetailViewModel

class FindActivity : AppCompatActivity() {
  private lateinit var viewModel: DetailViewModel
  private lateinit var recyclerView: RecyclerView
  
  
  
  // ------------------------ INIT ------------------------
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_find)
  
    supportActionBar?.hide()
    viewModel = ViewModelProvider(this).get(DetailViewModel::class.java)
  
    locateViews()
    initRecycler()
  }
  
  private fun locateViews() {
    recyclerView = findViewById(R.id.find_results_recycler)
  }
  
  private fun initRecycler() {
    val fakeResults = listOf("result0", "result1", "result2", "result3", "result4")
    // TODO use real results
    
    val adapter = ResultListAdapter(this)
    adapter.setResults(fakeResults)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)
  }
}