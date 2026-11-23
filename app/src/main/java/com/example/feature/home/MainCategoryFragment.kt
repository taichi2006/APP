package com.example.feature.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aroom.R
import com.example.aroom.databinding.FragmentMainCategoryBinding
import com.example.data.CartProduct
import com.example.feature.product.DetailsViewModel
import com.example.util.Resource
import com.example.util.SoundHelper
import com.example.util.showBottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private val TAG = "MainCategoryFragment"
@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private lateinit var binding : FragmentMainCategoryBinding
    private lateinit var specialProductAdapter : SpecialProductsAdapter
    private lateinit var bestDealsProductAdapter: BestDealsAdapter
    private lateinit var bestProductAdapter : BestProductsAdapter
    private val viewModel by viewModels<MainCategoryViewModel>()
    private val cartViewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainCategoryBinding.inflate(inflater)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpecialProductRv()
        setupBestDealsProductRv()
        setupBestProductRv()
        setupRecentSearches()

        binding.cardHome3dGuide.setOnClickListener {
            binding.nestedScrollMainCategory.smoothScrollTo(0, binding.tvBestProducts.top)
            Toast.makeText(requireContext(), "Chọn sản phẩm bất kỳ bên dưới để trải nghiệm mô hình 3D & AR!", Toast.LENGTH_SHORT).show()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchSpecialProducts()
            viewModel.fetchBestDealsProducts()
            viewModel.fetchBestProducts()
            setupRecentSearches()
        }

        specialProductAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product" , it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment,b)
        }
        specialProductAdapter.onBtnClick = {
            // "Xem ngay" button opens product details
            val b = Bundle().apply { putParcelable("product" , it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment,b)
        }

        bestProductAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product" , it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment,b)
        }

        bestDealsProductAdapter.onBtnClick ={
            val b = Bundle().apply { putParcelable("product",it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment,b)
        }

        bestDealsProductAdapter.onClick = {
            val b = Bundle().apply { putParcelable("product" , it) }
            findNavController().navigate(R.id.action_homeFragment_to_productDetailsFragment,b)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.specialProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        specialProductAdapter.differ.submitList(it.data)
                        hideLoading()
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG,it.message.toString())
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()

                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.bestProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.bestProductsProgressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        bestProductAdapter.differ.submitList(it.data)
                        binding.bestProductsProgressBar.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    is Resource.Error -> {

                        Log.e(TAG,it.message.toString())
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                        binding.bestProductsProgressBar.visibility = View.GONE

                    }
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.bestDealsProducts.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        bestDealsProductAdapter.differ.submitList(it.data)
                        hideLoading()
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG,it.message.toString())
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()

                    }
                    else -> Unit
                }
            }
        }

        binding.nestedScrollMainCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{ v,_,scrollY,_,_ ->
            if(v.getChildAt(0).bottom <= v.height + scrollY){
                viewModel.fetchBestProducts()
            }

        })
    }

    private fun setupBestProductRv() {
        bestProductAdapter = BestProductsAdapter()
        binding.rvBestProducts.apply {
            layoutManager = GridLayoutManager(requireContext(),2,GridLayoutManager.VERTICAL,false   )
            adapter = bestProductAdapter
        }
    }

    private fun setupBestDealsProductRv() {
        bestDealsProductAdapter = BestDealsAdapter()
        binding.rvBestDealsProducts.apply {
            layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL,false)
            adapter = bestDealsProductAdapter
        }
    }

    private fun showLoading() {
        binding.mainCategoryProgressBar.visibility = View.VISIBLE
    }
    private fun hideLoading() {
        binding.mainCategoryProgressBar.visibility = View.GONE
    }

    private fun setupSpecialProductRv() {
        specialProductAdapter = SpecialProductsAdapter()
        binding.rvSpecialProducts.apply {
            layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL,false)
            adapter = specialProductAdapter
        }
    }

    private fun setupRecentSearches() {
        val context = context ?: return
        val recentList = com.example.helper.RecentSearchHelper.getRecentSearches(context)
        if (recentList.isEmpty()) {
            binding.headerRecentSearches.visibility = View.GONE
        } else {
            binding.headerRecentSearches.visibility = View.VISIBLE
            binding.btnClearRecent.setOnClickListener {
                com.example.helper.RecentSearchHelper.clearRecentSearches(context)
                setupRecentSearches()
            }
            binding.layoutRecentChips.removeAllViews()
            val density = resources.displayMetrics.density
            val heightPx = (32 * density).toInt()
            val marginEndPx = (8 * density).toInt()
            val paddingStartEndPx = (12 * density).toInt()

            for (query in recentList) {
                val chip = android.widget.TextView(context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        heightPx
                    ).apply {
                        setMargins(0, 0, marginEndPx, 0)
                    }
                    background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_rounded_badge)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(context, R.color.brand_surface)
                    )
                    gravity = android.view.Gravity.CENTER
                    setPadding(paddingStartEndPx, 0, paddingStartEndPx, 0)
                    text = query
                    typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.sf_pro_display)
                    setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.brand_text_dark))
                    textSize = 12f
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        com.example.helper.RecentSearchHelper.saveSearchQuery(context, query)
                        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                        bottomNav?.selectedItemId = R.id.searchFragment
                    }
                }
                binding.layoutRecentChips.addView(chip)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
        setupRecentSearches()
    }
}