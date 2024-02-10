package com.gonodono.hexgrid.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gonodono.hexgrid.demo.databinding.ActivityMainBinding
import com.gonodono.hexgrid.demo.page.GridFragment
import com.gonodono.hexgrid.demo.page.LayoutFragment
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.viewPager.adapter = PagerAdapter(this)
        TabLayoutMediator(ui.tabLayout, ui.viewPager) { tab, position ->
            tab.text = Pages[position].second
        }.attach()
    }
}

private class PagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount() = Pages.size

    override fun createFragment(position: Int): Fragment =
        Pages[position].first.getConstructor().newInstance()
}

private val Pages = listOf(
    Pair(LayoutFragment::class.java, "Layout"),
    Pair(GridFragment::class.java, "Grid")
)