package com.example.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aroom.R
import com.example.aroom.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            MainCategoryFragment(),
            ChairFragment(),
            CupboardFragment(),
            TableFragment(),
            SofaFragment(),
            BedFragment()
        )

        binding.viewPagerHome.isUserInputEnabled = false

        val viewPager2Adapter = HomeViewPagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewPagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Trang chủ"
                1 -> tab.text = "Ghế"
                2 -> tab.text = "Tủ"
                3 -> tab.text = "Bàn"
                4 -> tab.text = "Sofa"
                5 -> tab.text = "Giường"
            }
        }.attach()

        binding.searchBar.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.searchFragment
        }
    }
}
