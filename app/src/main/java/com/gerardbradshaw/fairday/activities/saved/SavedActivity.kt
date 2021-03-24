package com.gerardbradshaw.fairday.activities.saved

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.activities.utils.AutocompleteUtil
import com.gerardbradshaw.fairday.activities.detail.DetailActivity
import com.gerardbradshaw.fairday.activities.utils.BaseViewModel
import com.gerardbradshaw.fairday.application.BaseApplication
import javax.inject.Inject

class SavedActivity : AppCompatActivity() {
  private lateinit var viewModel: BaseViewModel
  private lateinit var messageView: TextView
  private lateinit var recyclerView: RecyclerView
  private lateinit var locationListAdapter: LocationListAdapter
  @Inject lateinit var autocompleteUtil: AutocompleteUtil
  

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

    injectFields()
    initViews()
    initFab()
    initRecycler()
  }

  private fun injectFields() {
    val component = (application as BaseApplication)
      .getAppComponent()
      .getSavedActivityComponentFactory()
      .create(this, this)

    component.inject(this)
  }

  private fun initViews() {
    messageView = findViewById(R.id.saved_locations_message)
    recyclerView = findViewById(R.id.saved_locations_recycler)
  }
  
  private fun initFab() {
    findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
      autocompleteUtil.getPlaceFromAutocomplete()
    }
  }
  
  private fun initRecycler() {
    showEmptyListMessage(true)
    locationListAdapter = LocationListAdapter(this)
    recyclerView.adapter = locationListAdapter
    recyclerView.layoutManager = LinearLayoutManager(this)

    locationListAdapter.setLocationClickedListener(object :
      LocationListAdapter.LocationClickedListener {
      override fun onLocationClicked(position: Int) {
        val intent = Intent().also {
          it.putExtra(DetailActivity.EXTRA_PAGER_POSITION, position)
        }

        setResult(RESULT_OK, intent)
        finish()
      }
    })

    val swipeDirs = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

    val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, swipeDirs) {
      override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
      ): Boolean {
        return false // Items not movable
      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        displayDeleteLocationDialog(viewHolder.adapterPosition)
      }
    })

    touchHelper.attachToRecyclerView(recyclerView)

    viewModel.getLiveLocations().observe(this) {
      showEmptyListMessage(it.isEmpty())
      locationListAdapter.setLocations(it)
    }
  }

  private fun displayDeleteLocationDialog(position: Int) {
    val entityToDelete = locationListAdapter.getEntityAtPosition(position)

    if (entityToDelete == null) {
      Log.e(TAG, "displayDeleteLocationDialog: no location received")
      return
    }

    val msg = getString(R.string.string_are_you_sure_you_want_to_remove) + "${entityToDelete.locality}?"

    AlertDialog.Builder(this).let {
      it.setMessage(msg)
      it.setPositiveButton(getString(R.string.string_ok)) { _, _ -> viewModel.deleteLocation(entityToDelete)}
      it.setNegativeButton(getString(R.string.string_not_now)) { _, _ -> locationListAdapter.notifyDataSetChanged()}
      it.show()
    }
  }


  // ------------------------ UI ------------------------

  private fun showEmptyListMessage(boolean: Boolean) {
    messageView.visibility = if (boolean) View.VISIBLE else View.GONE
    recyclerView.visibility = if (boolean) View.GONE else View.VISIBLE
  }

  companion object {
    private const val TAG = "GGG SavedActivity"
  }
}