package com.gerardbradshaw.whetherweather.activities.saved

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.activities.detail.DetailActivity
import com.gerardbradshaw.whetherweather.activities.BaseViewModel
import com.gerardbradshaw.whetherweather.activities.search.SearchActivity

class SavedActivity : AppCompatActivity() {
  private lateinit var viewModel: BaseViewModel
  private lateinit var messageView: TextView
  private lateinit var recyclerView: RecyclerView
  

  // ------------------------ ACTIVITY LIFECYCLE ------------------------
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_saved)
    initActivity()
  }


  // ------------------------ INIT ------------------------

  private fun initActivity() {
    supportActionBar?.hide()
    viewModel = ViewModelProvider(this).get(BaseViewModel::class.java)

    initViews()
    initFab()
    initRecycler()
  }

  private fun initViews() {
    messageView = findViewById(R.id.saved_locations_message)
    recyclerView = findViewById(R.id.saved_locations_recycler)
  }
  
  private fun initFab() {
    findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
      startActivity(Intent(this, SearchActivity::class.java))
    }
  }
  
  private fun initRecycler() {
    showEmptyListMessage(true)
    val adapter = LocationListAdapter(this)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)

    adapter.setLocationClickedListener(object : LocationListAdapter.LocationClickedListener {
      override fun onLocationClicked(position: Int) {
        val returnIntent = Intent()
        returnIntent.putExtra(DetailActivity.EXTRA_PAGER_POSITION, position)
        setResult(RESULT_OK, returnIntent)
        finish()
      }
    })

    viewModel.getAllLocations().observe(this) {
      showEmptyListMessage(it.isEmpty())
      adapter.setLocations(it)
    }
  }


  // ------------------------ UI ------------------------

  private fun showEmptyListMessage(boolean: Boolean) {
    messageView.visibility = if (boolean) View.VISIBLE else View.GONE
    recyclerView.visibility = if (boolean) View.GONE else View.VISIBLE
  }

  companion object {
    private const val TAG = "GGG SavedLocationsActivit"
  }
}