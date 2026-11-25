package com.example.feature.product.ui;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentProductDetailsBinding;
import com.example.core.util.PriceCalculator;
import com.example.core.util.Resource;
import com.example.core.util.SoundHelper;
import com.example.core.util.UiUtil;
import com.example.feature.product.viewmodel.DetailsViewModel;
import com.example.model.CartProduct;
import com.example.model.Product;
import com.example.model.Review;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private final ViewPager2ImagesAdapter viewPagerAdapter = new ViewPager2ImagesAdapter();
    private final SizesAdapter sizesAdapter = new SizesAdapter();
    private final ColorsAdapter colorsAdapter = new ColorsAdapter();
    private final ReviewAdapter reviewAdapter = new ReviewAdapter();
    private DetailsViewModel viewModel;

    private Product product;
    private Integer selectedColor = null;
    private String selectedSize = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) product = getArguments().getParcelable("product");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UiUtil.hideBottomNavigationView(this);
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (product == null) return;
        viewModel = new ViewModelProvider(this).get(DetailsViewModel.class);

        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        binding.rvColors.setAdapter(colorsAdapter);
        binding.rvColors.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvSizes.setAdapter(sizesAdapter);
        binding.rvSizes.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        binding.viewPagerProductImage.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPagerProductImage, (tab, position) -> {}).attach();

        binding.tvProductName.setText(product.getName());
        binding.tvProductDescription.setText(getEnrichedDescription(product));
        bindMaterialSpecifications(product);
        setupReviews(product);

        Float offer = product.getOfferPercentage();
        if (offer != null && offer > 0) {
            binding.tvDetailDiscountBadge.setText("-" + (int) (offer * 100) + "%");
            binding.tvDetailDiscountBadge.setVisibility(View.VISIBLE);
            binding.tvProductPrice.setText(PriceCalculator.formatPriceVnd((1f - offer) * product.getPrice()));
            binding.tvProductOldPrice.setText(PriceCalculator.formatPriceVnd(product.getPrice()));
            binding.tvProductOldPrice.setPaintFlags(binding.tvProductOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvProductOldPrice.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailDiscountBadge.setVisibility(View.GONE);
            binding.tvProductPrice.setText(PriceCalculator.formatPriceVnd(product.getPrice()));
            binding.tvProductOldPrice.setVisibility(View.GONE);
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            viewPagerAdapter.differ.submitList(product.getImages());
        }

        if (product.getColors() != null && !product.getColors().isEmpty()) {
            binding.tvProductColor.setVisibility(View.VISIBLE);
            binding.tvProductColor.setText("Màu sắc (Vui lòng chọn)");
            colorsAdapter.differ.submitList(product.getColors());
            colorsAdapter.setOnItemClickListener(color -> {
                selectedColor = color;
                binding.tvProductColor.setText("Màu sắc: Đã chọn");
                int idx = product.getColors().indexOf(color);
                if (idx >= 0 && product.getImages() != null && idx < product.getImages().size()) {
                    binding.viewPagerProductImage.setCurrentItem(idx, true);
                }
            });
        } else {
            binding.tvProductColor.setVisibility(View.GONE);
        }

        List<String> rawSizes = (product.getSizes() != null && !product.getSizes().isEmpty())
                ? product.getSizes() : getDefaultSizes(product);
        List<String> formattedSizes = new ArrayList<>();
        for (String s : rawSizes) formattedSizes.add(formatSizeWithDimensions(s, product.getCategory()));
        sizesAdapter.differ.submitList(formattedSizes);
        binding.tvProductSize.setText("Kích thước (Vui lòng chọn)");
        sizesAdapter.setOnItemClickListener(size -> {
            selectedSize = size;
            binding.tvProductSize.setText("Kích thước: " + size);
        });
    }

    private void setupListeners() {
        binding.imgClose.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        View.OnClickListener openAr = v -> {
            Bundle b = new Bundle();
            Product productWithModel = resolveProductModel(product);
            b.putParcelable("product", productWithModel);
            if (selectedColor != null) b.putInt("selectedColor", selectedColor);
            Navigation.findNavController(requireView()).navigate(R.id.action_productDetailsFragment_to_vrFragment, b);
        };
        binding.arIcon.setOnClickListener(openAr);
        binding.cardArGuide.setOnClickListener(openAr);

        binding.btnAddToCart.setOnClickListener(v -> {
            if (product.getColors() != null && !product.getColors().isEmpty() && selectedColor == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn màu sắc trước khi thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedSize == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn kích thước trước khi thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                return;
            }
            CartProduct cartProduct = new CartProduct(product, 1, selectedColor, selectedSize);
            viewModel.addUpdateProductInCart(cartProduct);
        });

        binding.btnBuyNow.setOnClickListener(v -> {
            if (product.getColors() != null && !product.getColors().isEmpty() && selectedColor == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn màu sắc trước khi mua ngay!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedSize == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn kích thước trước khi mua ngay!", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle b = new Bundle();
            CartProduct cp = new CartProduct(product, 1, selectedColor, selectedSize);
            Float off = product.getOfferPercentage();
            float p = (off != null && off > 0) ? (1f - off) * product.getPrice() : product.getPrice();
            b.putFloat("totalPrice", p);
            b.putParcelableArray("products", new CartProduct[]{cp});
            b.putBoolean("payment", true);
            Navigation.findNavController(requireView()).navigate(R.id.action_productDetailsFragment_to_billingFragment, b);
        });
    }

    private void observeState() {
        viewModel.getAddToCartLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource instanceof Resource.Loading) binding.btnAddToCart.startAnimation();
            else if (resource instanceof Resource.Success) {
                binding.btnAddToCart.revertAnimation();
                SoundHelper.playSuccessSound();
                Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
            } else if (resource instanceof Resource.Error) {
                binding.btnAddToCart.stopAnimation();
                Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupReviews(Product product) {
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReviews.setAdapter(reviewAdapter);

        FirebaseFirestore.getInstance().collection("reviews")
                .whereEqualTo("productId", product.getId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Review> reviews = snapshot.toObjects(Review.class);
                    if (!reviews.isEmpty()) {
                        float sum = 0f;
                        for (Review r : reviews) sum += r.getRating();
                        float avg = sum / reviews.size();
                        binding.tvAvgRatingScore.setText(String.format(Locale.US, "%.1f", avg));
                        binding.ratingBarSummary.setRating(avg);
                        binding.tvReviewsCountBadge.setText(reviews.size() + " nhận xét");
                        reviewAdapter.differ.submitList(reviews);
                        binding.rvReviews.setVisibility(View.VISIBLE);
                        binding.tvEmptyReviews.setVisibility(View.GONE);
                    } else {
                        binding.rvReviews.setVisibility(View.GONE);
                        binding.tvEmptyReviews.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> binding.tvEmptyReviews.setVisibility(View.VISIBLE));
    }

    private void bindMaterialSpecifications(Product product) {
        String cat = (product.getCategory() != null ? product.getCategory() : "").toLowerCase(Locale.ROOT);
        String name = (product.getName() != null ? product.getName() : "").toLowerCase(Locale.ROOT);

        if (cat.contains("sofa") || name.contains("sofa")) {
            binding.tvSpecWood.setText("Khung gỗ sồi tự nhiên Bắc Mỹ (FAS) sấy nhiệt chân không đạt độ ẩm 10-12%, chịu tải 450kg, kháng mối mọt.");
            binding.tvSpecFabric.setText(name.contains("nhung") ? "Vải nhung Glam Velvet hoàng gia mềm mịn, kháng bụi tĩnh điện." : "Vải dệt nỉ cao cấp dệt sợi Bỉ thoáng khí kháng khuẩn, chống xù lông.");
            binding.tvSpecCushion.setText("Mút đúc nguyên khối D40 bọt biển tỷ trọng cao chống xẹp lún 10 năm kết hợp lò xo túi độc lập.");
            binding.tvSpecFinish.setText("Sơn dầu lau thực vật Inchem Organic nguồn gốc tự nhiên, an toàn cho trẻ nhỏ.");
            binding.tvSpecWarranty.setText("Bảo hành khung gỗ 5 năm, bảo hành đệm mút 2 năm. Hỗ trợ bảo dưỡng tận nhà.");
        } else if (cat.contains("bed") || cat.contains("giường") || name.contains("giường")) {
            binding.tvSpecWood.setText("Khung gỗ sồi Nga / óc chó tự nhiên FAS nguyên khối, dát giường nan cong gia cường chịu tải 500kg.");
            binding.tvSpecFabric.setText("Tựa đầu giường bọc da bò Ý cao cấp hoặc vải lanh Linen Bỉ mộc mạc dịu nhẹ thoáng khí.");
            binding.tvSpecCushion.setText("Tựa đầu đệm mút cao su nhân tạo công thái học ôm sát đốt sống cổ.");
            binding.tvSpecFinish.setText("Sơn phủ PU 5 lớp công nghệ Đức chống ẩm mốc, giữ trọn sắc thái vân gỗ độc bản.");
            binding.tvSpecWarranty.setText("Bảo hành khung sườn 5 năm. Đổi mới 1:1 trong 30 ngày nếu lỗi mối mọt cong vênh.");
        } else if (cat.contains("table") || cat.contains("bàn") || name.contains("bàn")) {
            binding.tvSpecWood.setText("100% gỗ óc chó Bắc Mỹ (FAS) / gỗ sồi nguyên tấm, chân gia công mộng ghép mộc truyền thống.");
            binding.tvSpecFabric.setText(name.contains("trà") ? "Mặt đá nung kết vân mây cẩm thạch chống trầy, chống ố cà phê, chịu nhiệt 250°C." : "Mặt gỗ nguyên tấm đánh nhẵn 800-grit hoặc đá phiến bóng mờ sang trọng.");
            binding.tvSpecCushion.setText("Khung kết cấu chịu tải tĩnh 300kg, các góc cạnh được bo tròn R-corner an toàn.");
            binding.tvSpecFinish.setText("Dầu lau thực vật Rubio Monocoat nhập khẩu Bỉ thẩm thấu sâu bảo vệ từng thớ gỗ.");
            binding.tvSpecWarranty.setText("Bảo hành 5 năm chống nứt nẻ và kết cấu. Tặng kèm gói lau dưỡng gỗ hàng năm.");
        } else if (cat.contains("cupboard") || cat.contains("tủ") || name.contains("tủ")) {
            binding.tvSpecWood.setText("Gỗ óc chó kết hợp HDF lõi xanh chống ẩm tiêu chuẩn E1 châu Âu, không phát thải Formaldehyde.");
            binding.tvSpecFabric.setText("Cửa kính cường lực sọc gân nghệ thuật Moru, tay nắm hợp kim kẽm mạ vàng Champagne PVD.");
            binding.tvSpecCushion.setText("Hệ thống bản lề và ray trượt giảm chấn Blum & Hafele tiêu chuẩn Đức trên 100.000 lần mở.");
            binding.tvSpecFinish.setText("Sơn phủ bóng mờ chống trầy xước Nano Ceramic cao cấp, chống bám dấu vân tay.");
            binding.tvSpecWarranty.setText("Bảo hành chính hãng 5 năm toàn bộ phụ kiện ray trượt, bản lề giảm chấn và khung tủ.");
        } else {
            binding.tvSpecWood.setText("Khung gỗ tần bì / sồi Bắc Mỹ uốn nhiệt đa chiều, chịu lực tải trên 250kg.");
            binding.tvSpecFabric.setText("Bề mặt bọc vải dạ thô đa sắc cao cấp hoặc da Microfiber kháng trầy xước móng vuốt.");
            binding.tvSpecCushion.setText("Đệm mút đúc tỷ trọng cao nâng đỡ hông và lưng theo chuẩn công thái học.");
            binding.tvSpecFinish.setText("Sơn mạ tĩnh điện Anode chống oxy hóa và sơn phủ bóng mờ bảo vệ vân gỗ tự nhiên.");
            binding.tvSpecWarranty.setText("Bảo hành 3 năm chính hãng TCFurnish, hỗ trợ thay thế linh kiện trọn đời.");
        }
    }

    private String getEnrichedDescription(Product product) {
        String base = product.getDescription() != null ? product.getDescription().trim() : "";
        String cat = (product.getCategory() != null ? product.getCategory() : "").toLowerCase(Locale.ROOT);
        String name = (product.getName() != null ? product.getName() : "").toLowerCase(Locale.ROOT);

        String story;
        if (cat.contains("sofa") || name.contains("sofa")) {
            story = "Thuộc bộ sưu tập đương đại TCFurnish, mẫu sofa định hình đẳng cấp cho không gian phòng khách của gia chủ. Từng đường kim mũi chỉ được chế tác chuẩn mực bởi nghệ nhân bậc thầy, mang lại cảm giác êm ái và sang trọng.";
        } else if (cat.contains("bed") || cat.contains("giường") || name.contains("giường")) {
            story = "Tạo tác hoàn mỹ mang đến trải nghiệm nghỉ dưỡng 5 sao ngay tại tư gia. Thiết kế tỷ lệ vàng kết hợp cùng cấu trúc nâng đỡ công thái học xua tan mệt mỏi, tái tạo năng lượng cho ngày mới.";
        } else if (cat.contains("table") || cat.contains("bàn") || name.contains("bàn")) {
            story = "Tâm điểm kết nối mọi khoảnh khắc sum vầy của gia đình và khẳng định gu thẩm mỹ chuẩn mực. Từng góc cạnh bo cong mượt mà cùng thớ vân gỗ đem lại hơi thở ấm cúng trường tồn.";
        } else if (cat.contains("cupboard") || cat.contains("tủ") || name.contains("tủ")) {
            story = "Sự giao thoa hoàn hảo giữa công năng lưu trữ khoa học và nghệ thuật bài trí không gian. Tủ được tính toán tỉ mỉ các ngăn chứa tiện ích, giúp gian phòng luôn thanh thoát.";
        } else {
            story = "Mẫu ghế thư giãn biểu tượng với tỷ lệ công thái học hoàn hảo, đem đến trải nghiệm đọc sách và tĩnh tại tuyệt vời. Điểm nhấn không thể thiếu cho không gian sống hiện đại.";
        }

        if (base.isEmpty()) return story;
        if (base.length() < 90) return base + "\n\n" + story;
        return base;
    }

    private List<String> getDefaultSizes(Product product) {
        String cat = (product.getCategory() != null ? product.getCategory() : "").toLowerCase(Locale.ROOT);
        String name = (product.getName() != null ? product.getName() : "").toLowerCase(Locale.ROOT);
        if (cat.contains("sofa") || name.contains("sofa")) return Arrays.asList("160 × 85 × 85 cm (2 chỗ)", "200 × 90 × 85 cm (3 chỗ)", "240 × 95 × 85 cm (Góc L)");
        if (cat.contains("giường") || cat.contains("bed") || name.contains("giường")) return Arrays.asList("200 × 160 × 90 cm (Queen)", "200 × 180 × 95 cm (King)", "220 × 200 × 100 cm (Super)");
        if (cat.contains("bàn") || cat.contains("table") || name.contains("bàn")) return Arrays.asList("140 × 80 × 75 cm (4 ghế)", "180 × 90 × 75 cm (6 ghế)", "220 × 100 × 76 cm (8 ghế)");
        if (cat.contains("tủ") || cat.contains("cupboard") || name.contains("tủ")) return Arrays.asList("120 × 55 × 200 cm (2 cánh)", "160 × 60 × 210 cm (3 cánh)", "200 × 60 × 220 cm (4 cánh)");
        return Arrays.asList("65 × 60 × 85 cm (Vừa)", "75 × 70 × 95 cm (Lớn)");
    }

    private String formatSizeWithDimensions(String rawSize, String category) {
        String s = rawSize.trim();
        if (s.contains("×") || s.contains("cm")) return s;
        String cat = (category != null ? category : "").toLowerCase(Locale.ROOT);
        if (cat.contains("sofa")) return s + " (180 × 85 × 85 cm)";
        if (cat.contains("giường") || cat.contains("bed")) return s + " (200 × 180 × 95 cm)";
        if (cat.contains("bàn") || cat.contains("table")) return s + " (160 × 80 × 75 cm)";
        if (cat.contains("tủ") || cat.contains("cupboard")) return s + " (160 × 60 × 210 cm)";
        return s + " (75 × 70 × 95 cm)";
    }

    private Product resolveProductModel(Product prod) {
        if (prod.getModel() != null && !prod.getModel().trim().isEmpty()) return prod;
        String cat = (prod.getCategory() != null ? prod.getCategory() : "").toLowerCase(Locale.ROOT);
        String name = (prod.getName() != null ? prod.getName() : "").toLowerCase(Locale.ROOT);
        String model;
        if (cat.contains("sofa") || name.contains("sofa")) model = "models/sofa_living.glb";
        else if (cat.contains("table") || name.contains("bàn")) model = "models/table_dining.glb";
        else if (cat.contains("bed") || name.contains("giường")) model = "models/bed_luxury.glb";
        else if (cat.contains("cupboard") || name.contains("tủ")) model = "models/cabinet_vintage.glb";
        else model = "models/chair_armchair.glb";
        return prod.copy(model);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
