package com.example.feature.search

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.aroom.R
import com.example.aroom.databinding.FragmentSearchBinding
import com.example.feature.home.BestProductsAdapter
import com.example.util.Resource
import com.example.util.SoundHelper
import com.example.util.showBottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private val viewModel by viewModels<SearchViewModel>()
    private val searchAdapter: BestProductsAdapter by lazy { BestProductsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRv()
        setupSortChips()

        searchAdapter.onClick = {
            com.example.helper.RecentSearchHelper.saveSearchQuery(requireContext(), it.name)
            val b = Bundle().apply { putParcelable("product", it) }
            findNavController().navigate(R.id.action_searchFragment_to_productDetailsFragment, b)
        }

        binding.apply {
            searchIcon.setOnClickListener {
                val query = edSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    com.example.helper.RecentSearchHelper.saveSearchQuery(requireContext(), query)
                }
                viewModel.searchProduct(query)
            }

            edSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = edSearch.text.toString().trim()
                    if (query.isNotEmpty()) {
                        com.example.helper.RecentSearchHelper.saveSearchQuery(requireContext(), query)
                    }
                    viewModel.searchProduct(query)
                    true
                } else {
                    false
                }
            }

            edSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.searchProduct(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        lifecycleScope.launchWhenStarted {
            viewModel.search.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        showLoading()
                    }
                    is Resource.Success -> {
                        hideLoading()
                        val list = it.data ?: emptyList()
                        searchAdapter.differ.submitList(list)
                        if (list.isEmpty()) {
                            showEmptyView()
                        } else {
                            hideEmptyView()
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showEmptyView()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setupSortChips() {
        val chips = listOf(
            binding.chipSortSold to SearchViewModel.SortType.SOLD_DESC,
            binding.chipSortPriceAsc to SearchViewModel.SortType.PRICE_ASC,
            binding.chipSortPriceDesc to SearchViewModel.SortType.PRICE_DESC,
            binding.chipSortRating to SearchViewModel.SortType.RATING_DESC,
            binding.chipSortDiscount to SearchViewModel.SortType.OFFER_DESC
        )

        fun updateChipStyles(selectedChip: TextView) {
            val selectedBg = ContextCompat.getColor(requireContext(), R.color.brand_primary)
            val unselectedBg = Color.parseColor("#FAF3E8")
            val textDark = ContextCompat.getColor(requireContext(), R.color.brand_text_dark)

            val chipTypeface = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.sf_pro_display)
            chips.forEach { (chip, _) ->
                if (chip == selectedChip) {
                    chip.backgroundTintList = ColorStateList.valueOf(selectedBg)
                    chip.setTextColor(Color.WHITE)
                    chip.setTypeface(chipTypeface, android.graphics.Typeface.BOLD)
                } else {
                    chip.backgroundTintList = ColorStateList.valueOf(unselectedBg)
                    chip.setTextColor(textDark)
                    chip.setTypeface(chipTypeface, android.graphics.Typeface.NORMAL)
                }
            }
        }

        chips.forEach { (chip, sortType) ->
            chip.setOnClickListener {
                SoundHelper.playClickSound()
                updateChipStyles(chip)
                viewModel.setSortType(sortType)
            }
        }
    }

    private fun showEmptyView() {
        binding.layoutEmptySearch.visibility = View.VISIBLE
    }

    private fun hideEmptyView() {
        binding.layoutEmptySearch.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBarSearch.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        binding.progressBarSearch.visibility = View.VISIBLE
    }

    private fun setupRv() {
        binding.searchRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = searchAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}
