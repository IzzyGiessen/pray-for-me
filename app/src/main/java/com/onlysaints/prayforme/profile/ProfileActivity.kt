package com.onlysaints.prayforme.profile

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.onlysaints.prayforme.R
import java.util.logging.Logger

class ProfileActivity : FragmentActivity() {

    lateinit var profileLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        viewPager.adapter = ProfileStateAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabNames[position]
        }.attach()

        profileLayout = findViewById(R.id.profile_layout)
    }

    fun exit(v: View) {
        finish()
    }

    companion object {
        val LOG = Logger.getLogger(ProfileActivity::class.java.name)
        val tabNames = listOf("My Prayers", "Saved")
    }
}