package com.gonodono.hexgrid.demo.internal

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gonodono.hexgrid.demo.databinding.ActivityBaseBinding
import com.gonodono.hexgrid.demo.databinding.DialogWelcomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator

abstract class BaseActivity(
    private val pages: List<Pair<Class<out Fragment>, String>>,
    private val headerResId: Int,
    private val textResId: Int
) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ui = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.viewPager.adapter = PageAdapter()
        TabLayoutMediator(ui.tabLayout, ui.viewPager) { tab, position ->
            tab.text = pages[position].second
        }.attach()

        if (savedInstanceState == null) {
            showWelcomeDialog(headerResId, textResId)
        }
    }

    private inner class PageAdapter : FragmentStateAdapter(this) {

        override fun getItemCount() = pages.size

        override fun createFragment(position: Int): Fragment =
            pages[position].first.getConstructor().newInstance()
    }
}

private fun Activity.showWelcomeDialog(headerResId: Int, textResId: Int) {
    val hideWelcome = getPreferences(AppCompatActivity.MODE_PRIVATE)
        .getBoolean(PREF_HIDE_WELCOME, false)
    if (!hideWelcome) {
        val ui = DialogWelcomeBinding.inflate(layoutInflater)
        ui.header.setText(headerResId)
        ui.text.setText(textResId)
        ui.hideWelcome.setOnCheckedChangeListener { _, isChecked ->
            getPreferences(AppCompatActivity.MODE_PRIVATE).edit()
                .putBoolean(PREF_HIDE_WELCOME, isChecked)
                .apply()
        }
        MaterialAlertDialogBuilder(this)
            .setView(ui.root)
            .setPositiveButton("Close", null)
            .show()
    }
}

private const val PREF_HIDE_WELCOME = "hide_welcome"