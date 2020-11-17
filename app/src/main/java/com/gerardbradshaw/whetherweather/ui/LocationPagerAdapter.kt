package com.gerardbradshaw.whetherweather.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.util.WeatherData

class LocationPagerAdapter(
  fragmentActivity: FragmentActivity,
  private var internalLocations: List<WeatherData>
) : FragmentStateAdapter(fragmentActivity) {

  var locations: List<WeatherData> = internalLocations
    set(value) {
      field = value
      internalLocations = locations
      notifyDataSetChanged()
    }

  override fun createFragment(position: Int): Fragment {
    val location =
      if (position == internalLocations.size) null
      else internalLocations[position]

    return LocationFragment(location)
  }

  override fun getItemCount(): Int = internalLocations.size + 1

  override fun onBindViewHolder(
    holder: FragmentViewHolder,
    position: Int,
    payloads: MutableList<Any>
  ) {

    super.onBindViewHolder(holder, position, payloads)
  }



  class LocationFragment(private val data: WeatherData?) : Fragment() {
    private lateinit var locationPagerAdapter: LocationPagerAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
    ): View {
      return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
    }
  }
}