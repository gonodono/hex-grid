package com.gonodono.hexgrid.demo

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gonodono.hexgrid.demo.databinding.ActivityMainBinding
import com.gonodono.hexgrid.demo.databinding.DialogWelcomeBinding
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

        if (savedInstanceState == null) showWelcomeDialog()
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

private fun Activity.showWelcomeDialog() {
    val hideWelcome = getPreferences(AppCompatActivity.MODE_PRIVATE)
        .getBoolean(PREF_HIDE_WELCOME, false)
    if (!hideWelcome) {
        val ui = DialogWelcomeBinding.inflate(layoutInflater)
        ui.hideWelcome.setOnCheckedChangeListener { _, isChecked ->
            getPreferences(AppCompatActivity.MODE_PRIVATE).edit()
                .putBoolean(PREF_HIDE_WELCOME, isChecked)
                .apply()
        }
        AlertDialog.Builder(this)
            .setView(ui.root)
            .setPositiveButton("Close", null)
            .show()
    }
}

private const val PREF_HIDE_WELCOME = "hide_welcome"