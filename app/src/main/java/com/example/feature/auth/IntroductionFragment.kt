package com.example.feature.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.feature.main.ShoppingActivity
import com.example.aroom.R
import com.example.aroom.databinding.FragmentIntroductionBinding
import com.example.feature.auth.IntroductionViewModel.ACCOUNT_OPTIONS_FRAGMENT
import com.example.feature.auth.IntroductionViewModel.SHOPPING_ACTIVITY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroductionFragment : Fragment(R.layout.fragment_introduction) {
    private lateinit var binding:FragmentIntroductionBinding
    private val viewModel by viewModels<IntroductionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntroductionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.navigate.collect{
                when(it){
                    SHOPPING_ACTIVITY -> {
                        Intent(requireActivity(),ShoppingActivity::class.java).also { intent->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    ACCOUNT_OPTIONS_FRAGMENT -> {
                        if (findNavController().currentDestination?.id == R.id.introductionFragment) {
                            try {
                                findNavController().navigate(it)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        binding.btnStartIntroduction.setOnClickListener {
            viewModel.startBtnClick()
            if (findNavController().currentDestination?.id == R.id.introductionFragment) {
                try {
                    findNavController().navigate(R.id.action_introductionFragment_to_accountOptionsFragment)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}