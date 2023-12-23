package com.veyselustuntas.hedefkaptani.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.veyselustuntas.hedefkaptani.R
import com.veyselustuntas.hedefkaptani.databinding.FragmentUserProgressBinding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.veyselustuntas.hedefkaptani.utils.ViewPagerAdapter


class UserProgress : Fragment() {

    private lateinit var _binding : FragmentUserProgressBinding
    private val binding get() = _binding.root

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProgressBinding.inflate(inflater,container,false)
        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        activity?.let {
            val adapter = ViewPagerAdapter(it)
            viewPager.adapter = adapter
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position){
                0 -> tab.text = "Kronometre"
                1 -> tab.text = "Adım Sayacı"
            }
        }.attach()


    }


}