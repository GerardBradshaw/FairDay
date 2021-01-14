package com.gerardbradshaw.whetherweather.ui.savedlocations

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.ui.detail.DetailViewModel

class SavedLocationsActivity : AppCompatActivity() {
  private lateinit var viewModel: DetailViewModel
  private lateinit var messageView: TextView
  private lateinit var recyclerView: RecyclerView
  
  
  
  // ------------------------ INIT ------------------------
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_saved_locations)
    
    supportActionBar?.hide()
    viewModel = ViewModelProvider(this).get(DetailViewModel::class.java)
    
    locateViews()
    initFab()
    initRecycler()
  }
  
  private fun locateViews() {
    messageView = findViewById(R.id.saved_locations_message)
    recyclerView = findViewById(R.id.saved_locations_recycler)
  }
  
  private fun initFab() {
    findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
      Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show()
    }
  }
  
  private fun initRecycler() {
    val savedLocations = viewModel.locationDataSet
    showNoLocationsMessage(savedLocations.isEmpty())
    
    val adapter = LocationsListAdapter(this)
    adapter.setLocations(savedLocations)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)
  }
  
  private fun showNoLocationsMessage(boolean: Boolean) {
    if (boolean) {
      messageView.visibility = View.VISIBLE
      recyclerView.visibility = View.GONE
    } else {
      messageView.visibility = View.GONE
      recyclerView.visibility = View.VISIBLE
    }
  }
}