package com.example.feature.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aroom.R
import com.example.aroom.databinding.FragmentCartBinding
import com.example.firebase.FirebaseCommon
import com.example.helper.PriceCalculator
import com.example.util.Resource
import com.example.util.SoundHelper
import com.example.util.VerticalItemDecoration
import kotlinx.coroutines.flow.collectLatest

class CartFragment :Fragment(R.layout.fragment_cart) {
    lateinit var binding : FragmentCartBinding
    private val cartAdapter by lazy {CartProductAdapter()}
    private val viewModel by activityViewModels<CartViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRv()
        var totalPrice =0f


        lifecycleScope.launchWhenStarted {
            viewModel.productPrice.collectLatest { price->
            price?.let {
                totalPrice = it
                binding.tvTotalPrice.text = PriceCalculator.formatPriceVnd(it)
            }

            }
        }

        cartAdapter.onProductClick = { cartProduct ->
            SoundHelper.playClickSound()
            val effectivePrice = if (cartProduct.product.offerPercentage != null && cartProduct.product.offerPercentage > 0) {
                cartProduct.product.price * (1f - cartProduct.product.offerPercentage)
            } else {
                cartProduct.product.price
            }
            val singleItemTotal = effectivePrice * cartProduct.quantity
            val action = CartFragmentDirections.actionCartFragmentToBillingFragment(
                singleItemTotal,
                arrayOf(cartProduct),
                true
            )
            findNavController().navigate(action)
        }

        cartAdapter.onPlusClick ={
            viewModel.changeQuantity(it,FirebaseCommon.QuantityChanging.INCREASE)
        }

        cartAdapter.onMinusClick ={
            viewModel.changeQuantity(it,FirebaseCommon.QuantityChanging.DECREASE)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.deleteDialog.collectLatest { cartProduct ->
                val dialog = android.app.Dialog(requireContext())
                val view = layoutInflater.inflate(R.layout.dialog_delete_cart_item, null)
                dialog.setContentView(view)
                dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

                view.findViewById<View>(R.id.btn_dialog_cancel).setOnClickListener {
                    dialog.dismiss()
                }

                view.findViewById<View>(R.id.btn_dialog_delete).setOnClickListener {
                    viewModel.deleteCartProduct(cartProduct)
                    dialog.dismiss()
                }

                dialog.show()
                dialog.window?.setLayout(
                    (resources.displayMetrics.widthPixels * 0.90).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.cartProducts.collectLatest {
                when(it){
                    is Resource.Loading ->{
                      showProgressBar()
                    }
                    is Resource.Success ->{
                        hideProgressBar()
                        if(it.data!!.isEmpty()){
                            showEmptyCart()
                            hideOtherViews()
                        } else {
                            hideEmptyCart()
                            showOtherViews()
                            cartAdapter.differ.submitList(it.data)
                        }
                    }
                    is Resource.Error ->{
                        hideProgressBar()
                        Toast.makeText(requireContext() , it.message , Toast.LENGTH_SHORT)
                    }
                    else -> Unit
                }
            }
        }

        binding.apply {
            imgCloseCart.setOnClickListener {
                findNavController().navigateUp()
            }

            btnCheckoutCart.setOnClickListener{
                SoundHelper.playClickSound(it)
                val action = CartFragmentDirections.actionCartFragmentToBillingFragment(totalPrice,cartAdapter.differ.currentList.toTypedArray(),true)
                findNavController().navigate(action)
            }
        }
    }

    private fun hideOtherViews() {
        binding.apply {
            rvCart.visibility = View.GONE
            toolbarCart.visibility = View.GONE
            btnCheckoutCart.visibility = View.GONE
            totalBoxContainer.visibility = View.GONE
        }
    }

    private fun showOtherViews() {
        binding.apply {
            rvCart.visibility = View.VISIBLE
            toolbarCart.visibility = View.VISIBLE
            btnCheckoutCart.visibility = View.VISIBLE
            totalBoxContainer.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyCart() {
        binding.apply {
            layoutEmptyCart.visibility = View.GONE
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            layoutEmptyCart.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        binding.progressBarCart.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBarCart.visibility = View.VISIBLE
    }

    private fun setupCartRv() {
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext(),RecyclerView.VERTICAL , false)
            adapter = cartAdapter
            addItemDecoration(VerticalItemDecoration())
        }
    }
}