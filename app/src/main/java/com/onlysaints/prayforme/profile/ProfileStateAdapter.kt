package com.onlysaints.prayforme.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfileStateAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    val myPrayersFragment = MyPrayersFragment()
    val savedFragment = SavedFragment()
    val fragments = listOf(myPrayersFragment, savedFragment)

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}