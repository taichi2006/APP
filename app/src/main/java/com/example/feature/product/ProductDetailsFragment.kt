package com.example.feature.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.aroom.R
import com.example.aroom.databinding.FragmentProductDetailsBinding
import com.example.data.CartProduct
import com.example.data.Product
import com.example.helper.PriceCalculator
import com.example.util.Resource
import com.example.util.SoundHelper
import com.example.util.dpToPx
import com.example.util.hideBottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductDetailsFragment : Fragment(R.layout.fragment_product_details) {

    private val args by navArgs<ProductDetailsFragmentArgs>()
    lateinit var binding: FragmentProductDetailsBinding
    private val viewPagerAdapter by lazy{ ViewPager2ImagesAdapter()}
    private val sizesAdapter by lazy { SizesAdapter () }
    private val colorsAdapter by lazy { ColorsAdapter () }
    private val reviewAdapter by lazy { ReviewAdapter() }
    private var selectedColor: Int? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        hideBottomNavigationView()

        binding = FragmentProductDetailsBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product

        setupSizesRv()
        setupColorsRv()
        setupViewPager()
        setupReviews(product)

        colorsAdapter.onItemClick = {
            selectedColor = it
            binding.tvProductColor.text = "Màu sắc: Đã chọn"
            val colorIndex = product.colors?.indexOf(it) ?: -1
            if (colorIndex in product.images.indices) {
                binding.viewPagerProductImage.currentItem = colorIndex
            }
        }

        binding.apply {
            tvProductName.text = product.name

            val offer = product.offerPercentage
            if (offer != null && offer > 0) {
                val percentInt = (offer * 100).toInt()
                tvDetailDiscountBadge.text = "-${percentInt}%"
                tvDetailDiscountBadge.visibility = View.VISIBLE

                val remainingPricePercentage = 1f - offer
                val priceAfterOffer = remainingPricePercentage * product.price
                tvProductPrice.text = PriceCalculator.formatPriceVnd(priceAfterOffer)

                tvProductOldPrice.text = PriceCalculator.formatPriceVnd(product.price)
                tvProductOldPrice.paintFlags = android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvProductOldPrice.visibility = View.VISIBLE
            } else {
                tvDetailDiscountBadge.visibility = View.GONE
                tvProductPrice.text = PriceCalculator.formatPriceVnd(product.price)
                tvProductOldPrice.visibility = View.GONE
            }

            tvProductDescription.text = getEnrichedDescription(product)
            bindMaterialSpecifications(product)

            imgClose.setOnClickListener {
                findNavController().navigateUp()
            }

            val openAr = {
                val targetModel = when {
                    !product.model.isNullOrBlank() -> product.model
                    product.category.equals("Sofa", ignoreCase = true) || product.name.contains("sofa", ignoreCase = true) -> "models/sofa_living.glb"
                    product.category.equals("Table", ignoreCase = true) || product.name.contains("bàn", ignoreCase = true) -> "models/table_dining.glb"
                    product.category.equals("Bed", ignoreCase = true) || product.name.contains("giường", ignoreCase = true) -> "models/bed_luxury.glb"
                    product.category.equals("Cupboard", ignoreCase = true) || product.name.contains("tủ", ignoreCase = true) -> "models/cabinet_vintage.glb"
                    else -> "models/chair_armchair.glb"
                }
                val productWithModel = product.copy(targetModel)
                val b = Bundle().apply {
                    putParcelable("product", productWithModel)
                    selectedColor?.let { putInt("selectedColor", it) }
                }
                findNavController().navigate(R.id.action_productDetailsFragment_to_vrFragment, b)
            }

            arIcon.setOnClickListener { openAr() }
            cardArGuide.setOnClickListener { openAr() }

            btnAddToCart.setOnClickListener {
                if (!product.colors.isNullOrEmpty() && selectedColor == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn màu sắc trước khi thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (selectedSize == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn kích thước trước khi thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, selectedSize))
            }

            if (product.colors.isNullOrEmpty()) {
                tvProductColor.visibility = View.GONE
            } else {
                tvProductColor.text = "Màu sắc (Vui lòng chọn)"
            }

            tvProductSize.text = "Kích thước (Vui lòng chọn)"
        }

        lifecycleScope.launchWhenStarted {

            viewModel.addToCart.collectLatest {
                when(it){
                    is Resource.Loading -> {
                        binding.btnAddToCart.startAnimation()
                }
                    is Resource.Success -> {
                        binding.btnAddToCart.revertAnimation()
                        SoundHelper.playSuccessSound()
                        Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        binding.btnAddToCart.stopAnimation()
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()

                    }
                    else -> Unit
            }
            }
        }

        viewPagerAdapter.differ.submitList(product.images)

        product.colors?.let{
            colorsAdapter.differ.submitList(it)
        }

        val rawSizes = if (!product.sizes.isNullOrEmpty()) {
            product.sizes
        } else {
            getDefaultFurnitureSizes(product)
        }
        val formattedSizes = rawSizes.map { formatSizeWithDimensions(it, product.category) }
        sizesAdapter.differ.submitList(formattedSizes)

        sizesAdapter.onItemClick = { size ->
            selectedSize = size
            binding.tvProductSize.text = "Kích thước: $size"
        }
    }

    private fun getDefaultFurnitureSizes(product: Product): List<String> {
        val cat = product.category.lowercase()
        val name = product.name.lowercase()
        return when {
            cat.contains("sofa") || name.contains("sofa") -> listOf(
                "160 × 85 × 85 cm (2 chỗ)",
                "200 × 90 × 85 cm (3 chỗ)",
                "240 × 95 × 85 cm (Góc L)"
            )
            cat.contains("giường") || cat.contains("bed") || name.contains("giường") -> listOf(
                "200 × 160 × 90 cm (Queen)",
                "200 × 180 × 95 cm (King)",
                "220 × 200 × 100 cm (Super)"
            )
            cat.contains("bàn") || cat.contains("table") || name.contains("bàn") -> listOf(
                "140 × 80 × 75 cm (4 ghế)",
                "180 × 90 × 75 cm (6 ghế)",
                "220 × 100 × 76 cm (8 ghế)"
            )
            cat.contains("tủ") || cat.contains("cupboard") || name.contains("tủ") -> listOf(
                "120 × 55 × 200 cm (2 cánh)",
                "160 × 60 × 210 cm (3 cánh)",
                "200 × 60 × 220 cm (4 cánh)"
            )
            else -> listOf(
                "65 × 60 × 85 cm (Vừa)",
                "75 × 70 × 95 cm (Lớn)"
            )
        }
    }

    private fun formatSizeWithDimensions(rawSize: String, category: String?): String {
        val s = rawSize.trim()
        if (s.contains("×") || s.contains("cm", ignoreCase = true)) {
            return s
        }
        val cat = (category ?: "").lowercase()
        return when {
            cat.contains("sofa") || cat.contains("ghế") || cat.contains("chair") -> when {
                s.equals("S", true) -> "90 × 80 × 85 cm"
                s.equals("M", true) -> "160 × 85 × 85 cm"
                s.equals("L", true) || s.equals("XL", true) -> "220 × 90 × 85 cm"
                s.contains("1m6") || s.contains("160") -> "160 × 85 × 85 cm"
                s.contains("1m8") || s.contains("180") -> "180 × 85 × 85 cm"
                s.contains("2m") || s.contains("200") -> "200 × 90 × 85 cm"
                else -> "$s (180 × 85 × 85 cm)"
            }
            cat.contains("giường") || cat.contains("bed") -> when {
                s.contains("1m6") || s.equals("S", true) -> "200 × 160 × 90 cm"
                s.contains("1m8") || s.equals("M", true) -> "200 × 180 × 95 cm"
                s.contains("2m") || s.equals("L", true) -> "220 × 200 × 100 cm"
                else -> "$s (200 × 180 × 95 cm)"
            }
            cat.contains("tủ") || cat.contains("cupboard") -> when {
                s.equals("S", true) -> "120 × 55 × 200 cm"
                s.equals("M", true) -> "160 × 60 × 210 cm"
                s.equals("L", true) -> "200 × 60 × 220 cm"
                else -> "$s (160 × 60 × 210 cm)"
            }
            cat.contains("bàn") || cat.contains("table") -> when {
                s.equals("S", true) -> "120 × 70 × 75 cm"
                s.equals("M", true) -> "160 × 80 × 75 cm"
                s.equals("L", true) -> "180 × 90 × 75 cm"
                else -> "$s (160 × 80 × 75 cm)"
            }
            else -> when {
                s.equals("S", true) -> "120 × 70 × 75 cm"
                s.equals("M", true) -> "160 × 80 × 75 cm"
                s.equals("L", true) -> "200 × 90 × 80 cm"
                else -> "$s (160 × 80 × 75 cm)"
            }
        }
    }

    private fun bindMaterialSpecifications(product: Product) {
        val cat = (product.category ?: "").lowercase()
        val name = (product.name ?: "").lowercase()

        when {
            cat.contains("sofa") || name.contains("sofa") -> {
                binding.tvSpecWood.text = "Khung gỗ sồi tự nhiên Bắc Mỹ (FAS) sấy nhiệt chân không đạt độ ẩm 10-12%, chịu tải trên 450kg, kháng mối mọt và chống cong vênh vĩnh viễn."
                binding.tvSpecFabric.text = if (name.contains("nhung") || name.contains("glam")) {
                    "Vải nhung tuyết Glam Velvet hoàng gia mềm mịn như tơ tằm, kháng bụi tĩnh điện và bền màu chuẩn chất lượng châu Âu."
                } else {
                    "Vải dệt nỉ cao cấp dệt sợi Bỉ thoáng khí kháng khuẩn, mật độ dệt cao chống xù lông và chống cào xước móng vuốt vật nuôi."
                }
                binding.tvSpecCushion.text = "Mút đúc nguyên khối D40 bọt biển tỷ trọng cao chống xẹp lún 10 năm kết hợp hệ lò xo túi độc lập êm ái đàn hồi cao cấp."
                binding.tvSpecFinish.text = "Sơn dầu lau thực vật Inchem Organic nguồn gốc tự nhiên, không phát thải VOC, tuyệt đối an toàn cho trẻ nhỏ."
                binding.tvSpecWarranty.text = "Bảo hành kết cấu khung gỗ 5 năm, bảo hành đệm mút và bề mặt bọc 2 năm. Hỗ trợ vệ sinh, bảo dưỡng tận nhà định kỳ."
            }
            cat.contains("bed") || cat.contains("giường") || name.contains("giường") -> {
                binding.tvSpecWood.text = "Khung gỗ sồi Nga / óc chó tự nhiên FAS nguyên khối tuyển chọn, dát giường nan cong gia cường chịu tải 500kg không gây tiếng cót két khi trở mình."
                binding.tvSpecFabric.text = "Tựa đầu giường bọc da bò Ý cao cấp êm ái hoặc vải lanh Linen Bỉ mộc mạc, dịu nhẹ và thoáng khí cho làn da nhạy cảm."
                binding.tvSpecCushion.text = "Tựa đầu đệm mút cao su nhân tạo công thái học ôm sát đường cong đốt sống cổ, mang lại tư thế tựa lưng thư giãn tuyệt hảo."
                binding.tvSpecFinish.text = "Sơn phủ PU 5 lớp công nghệ Đức chống ẩm mốc, giữ trọn sắc thái ấm áp cùng những đường vân gỗ tự nhiên độc bản."
                binding.tvSpecWarranty.text = "Bảo hành khung sườn 5 năm. Đổi mới 1:1 trong 30 ngày nếu phát hiện lỗi cong vênh hoặc mối mọt."
            }
            cat.contains("table") || cat.contains("bàn") || name.contains("bàn") -> {
                binding.tvSpecWood.text = "100% gỗ óc chó Bắc Mỹ (FAS) / gỗ sồi tự nhiên nguyên tấm, chân bàn gia công mộng ghép mộc truyền thống không dùng đinh kim loại."
                binding.tvSpecFabric.text = if (name.contains("trà") || name.contains("coffee")) {
                    "Mặt đá nung kết vân mây cẩm thạch chống trầy xước, chống bám ố cà phê và chịu nhiệt độ tiếp xúc trực tiếp lên tới 250°C."
                } else {
                    "Mặt gỗ tự nhiên nguyên tấm đánh nhẵn thủ công 800-grit hoặc đá phiến bóng mờ sang trọng, lau chùi sạch dầu mỡ trong tích tắc."
                }
                binding.tvSpecCushion.text = "Khung kết cấu chịu tải tĩnh 300kg, các góc cạnh được bo tròn R-corner an toàn tối đa cho gia đình có trẻ nhỏ."
                binding.tvSpecFinish.text = "Dầu lau thực vật Rubio Monocoat nhập khẩu Bỉ thẩm thấu sâu bảo vệ từng thớ gỗ, kháng nước và chống ố vĩnh viễn."
                binding.tvSpecWarranty.text = "Bảo hành 5 năm chống nứt nẻ và kết cấu. Tặng kèm gói bảo dưỡng dầu dưỡng bóng gỗ hàng năm."
            }
            cat.contains("cupboard") || cat.contains("tủ") || cat.contains("kệ") || name.contains("tủ") || name.contains("kệ") -> {
                binding.tvSpecWood.text = "Gỗ óc chó tự nhiên kết hợp gỗ công nghiệp HDF lõi xanh chống ẩm tiêu chuẩn E1 châu Âu, không mùi và không phát thải Formaldehyde."
                binding.tvSpecFabric.text = "Cửa kính cường lực sọc gân nghệ thuật Moru hoặc cánh gỗ phay rãnh CNC tinh xảo, tay nắm hợp kim kẽm mạ vàng Champagne PVD."
                binding.tvSpecCushion.text = "Hệ thống bản lề và ray trượt giảm chấn Blum & Hafele tiêu chuẩn Đức, vận hành êm ái trên 100.000 lần đóng mở."
                binding.tvSpecFinish.text = "Sơn phủ bóng mờ chống trầy xước Nano Ceramic cao cấp, giữ bề mặt luôn mới mẻ và chống bám dấu vân tay."
                binding.tvSpecWarranty.text = "Bảo hành chính hãng 5 năm toàn bộ phụ kiện ray trượt, bản lề giảm chấn và khung sườn tủ."
            }
            else -> {
                binding.tvSpecWood.text = "Khung gỗ tần bì / sồi tự nhiên Bắc Mỹ uốn nhiệt đa chiều, kết hợp khung chịu lực kim loại phủ sơn tĩnh điện chịu lực 250kg."
                binding.tvSpecFabric.text = "Bề mặt bọc vải dạ thô đa sắc cao cấp hoặc da Microfiber kháng trầy xước móng vuốt, đạt chứng chỉ an toàn quốc tế OEKO-TEX."
                binding.tvSpecCushion.text = "Đệm mút đúc tỷ trọng cao nâng đỡ hông và lưng theo chuẩn công thái học, xua tan căng thẳng sau ngày làm việc dài."
                binding.tvSpecFinish.text = "Sơn mạ tĩnh điện Anode chống oxy hóa và sơn phủ bóng mờ bảo vệ vân gỗ tự nhiên Inchem Organic."
                binding.tvSpecWarranty.text = "Bảo hành 3 năm chính hãng TCFurnish, cam kết hỗ trợ thay thế linh kiện và bảo trì trọn đời sản phẩm."
            }
        }
    }

    private fun getEnrichedDescription(product: Product): String {
        val base = product.description?.trim().orEmpty()
        val cat = (product.category ?: "").lowercase()
        val name = (product.name ?: "").lowercase()

        val story = when {
            cat.contains("sofa") || name.contains("sofa") ->
                "Thuộc bộ sưu tập đương đại TCFurnish, mẫu sofa định hình đẳng cấp cho không gian phòng khách của gia chủ. Từng đường kim mũi chỉ được chế tác chuẩn mực bởi nghệ nhân bậc thầy, mang lại cảm giác nâng niu êm ái và tôn vinh nét thẩm mỹ trang nhã."
            cat.contains("bed") || cat.contains("giường") || name.contains("giường") ->
                "Tạo tác hoàn mỹ mang đến trải nghiệm nghỉ dưỡng 5 sao ngay tại tư gia. Thiết kế tỷ lệ vàng kết hợp cùng cấu trúc nâng đỡ công thái học xua tan mệt mỏi, tái tạo năng lượng cho một ngày mới ngập tràn hứng khởi."
            cat.contains("table") || cat.contains("bàn") || name.contains("bàn") ->
                "Tâm điểm kết nối mọi khoảnh khắc sum vầy của gia đình và khẳng định gu thẩm mỹ chuẩn mực. Từng góc cạnh bo cong mượt mà cùng thớ vân gỗ tự nhiên đem lại hơi thở ấm cúng, sang trọng và trường tồn cùng thời gian."
            cat.contains("cupboard") || cat.contains("tủ") || cat.contains("kệ") || name.contains("tủ") || name.contains("kệ") ->
                "Sự giao thoa hoàn hảo giữa công năng lưu trữ khoa học và nghệ thuật bài trí không gian. Tủ được tính toán tỉ mỉ các ngăn chứa tiện ích, giúp gian phòng luôn gọn gàng, thanh thoát và tràn đầy cảm hứng sống."
            else ->
                "Mẫu ghế thư giãn biểu tượng với tỷ lệ công thái học hoàn hảo, đem đến trải nghiệm đọc sách và tĩnh tại tuyệt vời. Điểm nhấn nghệ thuật không thể thiếu cho không gian sống hiện đại và tinh tế."
        }

        return if (base.isEmpty()) {
            story
        } else if (base.length < 90) {
            "$base\n\n$story"
        } else {
            base
        }
    }

    private fun setupViewPager() {
        binding.apply {
            viewPagerProductImage.adapter = viewPagerAdapter

            TabLayoutMediator(tabLayout, viewPagerProductImage) { tab, position ->
                val customView = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx()).apply {
                    }
                    setBackgroundResource(R.drawable.tab_indicator)
                }
                tab.customView = customView
            }.attach()

                // Set the selected tab indicator drawable
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.customView?.setBackgroundResource(R.drawable.selected_dot)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.customView?.setBackgroundResource(R.drawable.tab_indicator)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // Do nothing
                }
            })
        }

    }

    private fun setupColorsRv() {
        binding.rvColors.apply {
           adapter = colorsAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        }
    }

    private fun setupSizesRv() {
        binding.rvSizes.apply {
            adapter = sizesAdapter
            layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.HORIZONTAL,false)
        }
    }

    private fun setupReviews(product: Product) {
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }

        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("reviews")
            .whereEqualTo("productId", product.id)
            .get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.toObjects(com.example.data.Review::class.java)
                if (reviews.isNotEmpty()) {
                    val avg = reviews.map { it.rating }.average().toFloat()
                    binding.tvAvgRatingScore.text = String.format(java.util.Locale.US, "%.1f", avg)
                    binding.ratingBarSummary.rating = avg
                    binding.tvReviewsCountBadge.text = "${reviews.size} nhận xét"
                    reviewAdapter.differ.submitList(reviews)
                    binding.rvReviews.visibility = View.VISIBLE
                    binding.tvEmptyReviews.visibility = View.GONE
                } else {
                    val sampleReviews = listOf(
                        com.example.data.Review("r1", product.id, "1001", "Trần Thu Trang", "", 5f, "Sản phẩm thực tế đẹp hơn ảnh, chất liệu hoàn thiện tỉ mỉ và rất sang trọng!", "18/11/2026"),
                        com.example.data.Review("r2", product.id, "1002", "Nguyễn Minh Đức", "", 5f, "Giao hàng chuẩn giờ, thợ hỗ trợ bê lên tận lầu và lắp đặt rất chu đáo.", "15/11/2026"),
                        com.example.data.Review("r3", product.id, "1003", "Phạm Quốc Bảo", "", 4.8f, "Đệm êm, màu sắc chuẩn nội thất cao cấp. Rất ưng ý!", "10/11/2026")
                    )
                    binding.tvAvgRatingScore.text = String.format(java.util.Locale.US, "%.1f", product.rating)
                    binding.ratingBarSummary.rating = product.rating
                    binding.tvReviewsCountBadge.text = "${product.reviewCount} nhận xét"
                    reviewAdapter.differ.submitList(sampleReviews)
                    binding.rvReviews.visibility = View.VISIBLE
                    binding.tvEmptyReviews.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                binding.tvEmptyReviews.visibility = View.VISIBLE
            }
    }
}