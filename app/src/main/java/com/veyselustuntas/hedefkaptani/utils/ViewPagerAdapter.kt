package com.veyselustuntas.hedefkaptani.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.veyselustuntas.hedefkaptani.view.fragment.UserChoronometer
import com.veyselustuntas.hedefkaptani.view.fragment.UserStepCounter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2 // Toplam sayfa sayısı
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserChoronometer()
            1 -> UserStepCounter()
            else -> UserChoronometer()
        }
    }
}
