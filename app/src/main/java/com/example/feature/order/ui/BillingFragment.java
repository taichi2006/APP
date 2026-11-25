package com.example.feature.order.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aroom.R;
import com.example.aroom.databinding.FragmentBillingBinding;
import com.example.core.util.AppNotificationHelper;
import com.example.core.util.PriceCalculator;
import com.example.core.util.Resource;
import com.example.core.util.SoundHelper;
import com.example.core.util.UiUtil;
import com.example.core.util.ValidationCheck;
import com.example.feature.order.viewmodel.BillingViewModel;
import com.example.feature.order.viewmodel.PlaceOrderViewModel;
import com.example.model.Address;
import com.example.model.CartProduct;
import com.example.model.Order;
import com.example.model.OrderStatus;
import com.example.model.Voucher;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BillingFragment extends Fragment {

    private FragmentBillingBinding binding;
    private final AddressAdapter addressAdapter = new AddressAdapter();
    private final BillingProductsAdapter billingProductsAdapter = new BillingProductsAdapter();
    private BillingViewModel billingViewModel;
    private PlaceOrderViewModel orderViewModel;

    private final List<CartProduct> products = new ArrayList<>();
    private float totalPrice = 0f;
    private float discountAmount = 0f;
    private String appliedVoucherCode = null;
    private Address selectedAddress = null;
    private boolean isPayment = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalPrice = getArguments().getFloat("totalPrice", 0f);
            isPayment = getArguments().getBoolean("payment", true);
            Parcelable[] parcelables = getArguments().getParcelableArray("products");
            if (parcelables != null) {
                for (Parcelable p : parcelables) if (p instanceof CartProduct) products.add((CartProduct) p);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBillingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        billingViewModel = new ViewModelProvider(this).get(BillingViewModel.class);
        orderViewModel = new ViewModelProvider(this).get(PlaceOrderViewModel.class);
        setupViews();
        setupListeners();
        observeState();
    }

    private void setupViews() {
        int v = isPayment ? View.VISIBLE : View.INVISIBLE;
        binding.buttonPlaceOrder.setVisibility(v);
        binding.totalBoxContainer.setVisibility(v);
        binding.cardPaymentSection.setVisibility(v);
        binding.cardVoucherSection.setVisibility(v);
        binding.rvAddress.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.rvAddress.setAdapter(addressAdapter);
        binding.rvAddress.addItemDecoration(new UiUtil.HorizontalItemDecoration());
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.rvProducts.setAdapter(billingProductsAdapter);
        billingProductsAdapter.differ.submitList(products);
        updatePriceDisplay();
    }

    private void setupListeners() {
        binding.imageCloseBilling.setOnClickListener(n -> Navigation.findNavController(n).navigateUp());
        binding.imageAddAddress.setOnClickListener(n -> Navigation.findNavController(n).navigate(R.id.action_billingFragment_to_addressFragment));

        addressAdapter.setOnClick(address -> {
            selectedAddress = address;
            bindSelectedAddress(address);
        });

        // Sửa địa chỉ trực tiếp khi đặt hàng
        binding.btnEditBillingAddress.setOnClickListener(v -> showEditAddressDialog());
        binding.layoutBillingAddressDetails.setOnClickListener(v -> showEditAddressDialog());
        binding.layoutOptionCod.setOnClickListener(n -> { binding.radioCod.setChecked(true); binding.radioSandbox.setChecked(false); });
        binding.radioCod.setOnClickListener(n -> binding.layoutOptionCod.performClick());
        binding.layoutOptionSandbox.setOnClickListener(n -> { binding.radioCod.setChecked(false); binding.radioSandbox.setChecked(true); });
        binding.radioSandbox.setOnClickListener(n -> binding.layoutOptionSandbox.performClick());
        setupVoucherSection();

        binding.buttonPlaceOrder.setOnClickListener(n -> {
            String phone = binding.edBillingPhone.getText() != null ? binding.edBillingPhone.getText().toString().trim() : "";
            ValidationCheck.ValidationResult res = ValidationCheck.validateAddress(selectedAddress, phone);
            if (!res.isValid()) {
                Toast.makeText(requireContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedAddress != null && !phone.isEmpty()) selectedAddress.setPhoneNumb(phone);
            SoundHelper.playClickSound();

            long orderId = System.currentTimeMillis() % 1000L;
            if (orderId <= 0) orderId = 1L;
            long finalOrderId = orderId;
            if (binding.radioSandbox.isChecked()) {
                VNPayWebViewDialog.show(requireContext(), finalOrderId, getFinalTotalPrice(), new VNPayWebViewDialog.OnVNPayPaymentListener() {
                    @Override public void onPaymentSuccess(String pm, String ps) { submitOrder(pm, ps, finalOrderId); }
                    @Override public void onPaymentFailed(String c) { Toast.makeText(requireContext(), "Thanh toán chưa hoàn tất. Bạn có thể chọn COD.", Toast.LENGTH_SHORT).show(); }
                });
            } else {
                PaymentConfirmDialog.show(requireContext(), getFinalTotalPrice(), selectedAddress, (pm, ps) -> submitOrder(pm, ps, finalOrderId));
            }
        });
    }

    private void showEditAddressDialog() {
        if (selectedAddress == null) {
            Navigation.findNavController(requireView()).navigate(R.id.action_billingFragment_to_addressFragment);
            return;
        }
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 20, 48, 10);

        EditText etName = new EditText(requireContext());
        etName.setHint("Họ và tên người nhận"); etName.setText(selectedAddress.getFullName()); layout.addView(etName);
        EditText etPhone = new EditText(requireContext());
        etPhone.setHint("Số điện thoại nhận hàng"); etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        String curP = binding.edBillingPhone.getText() != null && !binding.edBillingPhone.getText().toString().trim().isEmpty()
                ? binding.edBillingPhone.getText().toString().trim() : selectedAddress.getPhoneNumb();
        etPhone.setText(curP); layout.addView(etPhone);
        EditText etStreet = new EditText(requireContext());
        etStreet.setHint("Địa chỉ nhận hàng (Số nhà, đường...)"); etStreet.setText(selectedAddress.getStreet()); layout.addView(etStreet);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chỉnh sửa địa chỉ nhận hàng")
                .setView(layout)
                .setPositiveButton("Lưu & Áp dụng", (d, w) -> {
                    String p = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String str = etStreet.getText() != null ? etStreet.getText().toString().trim() : "";
                    if (!p.isEmpty()) selectedAddress.setPhoneNumb(p);
                    if (!name.isEmpty()) selectedAddress.setFullName(name);
                    if (!str.isEmpty()) selectedAddress.setStreet(str);
                    bindSelectedAddress(selectedAddress);
                    binding.edBillingPhone.setText(p);
                    Toast.makeText(requireContext(), "Đã cập nhật địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Sửa chi tiết", (d, w) -> {
                    Bundle b = new Bundle(); b.putParcelable("address", selectedAddress);
                    Navigation.findNavController(requireView()).navigate(R.id.action_billingFragment_to_addressFragment, b);
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void submitOrder(String paymentMethod, String paymentStatus, long orderId) {
        String note = binding.edOrderNote.getText() != null ? binding.edOrderNote.getText().toString().trim() : "";
        String fullNote = note;
        if (appliedVoucherCode != null) {
            String vNote = "Voucher: " + appliedVoucherCode + " (-" + PriceCalculator.formatPriceVnd(discountAmount) + ")";
            fullNote = fullNote.isEmpty() ? vNote : fullNote + " | " + vNote;
        }
        Order order = new Order(OrderStatus.Ordered.status, getFinalTotalPrice(), products, selectedAddress, paymentMethod, paymentStatus, fullNote);
        order.setOrderId(orderId);
        orderViewModel.placeOrder(order);
    }

    private void setupVoucherSection() {
        binding.btnApplyVoucher.setOnClickListener(v -> {
            String code = binding.edVoucherCode.getText() != null ? binding.edVoucherCode.getText().toString().trim() : "";
            if (!code.isEmpty()) applyVoucher(code);
            else Toast.makeText(requireContext(), "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
        });
        binding.chipVoucher10.setOnClickListener(v -> applyVoucher("TCFURNISH10"));
        binding.chipVoucher20.setOnClickListener(v -> applyVoucher("VIP20"));
        binding.chipVoucher100k.setOnClickListener(v -> applyVoucher("NOITHAT100K"));
    }

    private void applyVoucher(String code) {
        String clean = code.trim().toUpperCase();
        FirebaseFirestore.getInstance().collection("vouchers").document(clean).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Voucher v = doc.toObject(Voucher.class);
                        if (v != null) {
                            if (totalPrice < v.getMinOrderAmount()) {
                                Toast.makeText(requireContext(), "Đơn tối thiểu " + PriceCalculator.formatPriceVnd(v.getMinOrderAmount()), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            appliedVoucherCode = v.getCode();
                            float calc = v.getDiscountPercentage() > 0 ? totalPrice * v.getDiscountPercentage() : v.getFixedDiscountAmount();
                            discountAmount = v.getMaxDiscountAmount() > 0 ? Math.min(calc, v.getMaxDiscountAmount()) : calc;
                            setVoucherUi("Đã áp dụng mã " + v.getCode(), "Tiết kiệm: -" + PriceCalculator.formatPriceVnd(discountAmount), v.getCode());
                            Toast.makeText(requireContext(), "Áp dụng mã " + v.getCode() + " thành công!", Toast.LENGTH_SHORT).show();
                            SoundHelper.playSuccessSound();
                            updatePriceDisplay();
                            return;
                        }
                    }
                    applyVoucherFallback(clean);
                })
                .addOnFailureListener(e -> applyVoucherFallback(clean));
    }

    private void applyVoucherFallback(String clean) {
        float disc;
        String status;
        if ("TCFURNISH10".equals(clean) || "AROOM10".equals(clean)) {
            disc = totalPrice * 0.10f; status = "Đã áp dụng mã -10%";
        } else if ("VIP20".equals(clean)) {
            disc = totalPrice * 0.20f; status = "Đã áp dụng mã -20%";
        } else if ("NOITHAT100K".equals(clean) || "AROOM50".equals(clean)) {
            disc = "NOITHAT100K".equals(clean) ? 100000f : 50000f;
            status = "Đã áp dụng voucher giảm " + PriceCalculator.formatPriceVnd(disc);
        } else {
            Toast.makeText(requireContext(), "Mã giảm giá không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }
        appliedVoucherCode = clean;
        discountAmount = Math.min(totalPrice, disc);
        setVoucherUi(status, "Tiết kiệm: -" + PriceCalculator.formatPriceVnd(discountAmount), clean);
        Toast.makeText(requireContext(), "Áp dụng mã " + clean + " thành công!", Toast.LENGTH_SHORT).show();
        SoundHelper.playSuccessSound();
        updatePriceDisplay();
    }

    private void setVoucherUi(String status, String discountInfo, String code) {
        binding.tvVoucherStatus.setText(status);
        binding.tvVoucherStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.brand_success));
        binding.tvVoucherDiscountInfo.setText(discountInfo);
        binding.tvVoucherDiscountInfo.setVisibility(View.VISIBLE);
        binding.edVoucherCode.setText(code);
    }

    private void observeState() {
        billingViewModel.getAddressesLiveData().observe(getViewLifecycleOwner(), res -> {
            if (res instanceof Resource.Loading) binding.progressbarAddress.setVisibility(View.VISIBLE);
            else if (res instanceof Resource.Success) {
                binding.progressbarAddress.setVisibility(View.GONE);
                addressAdapter.differ.submitList(res.getData());
                if (res.getData() != null && !res.getData().isEmpty() && selectedAddress == null) {
                    selectedAddress = res.getData().get(0);
                    bindSelectedAddress(selectedAddress);
                }
            } else if (res instanceof Resource.Error) {
                binding.progressbarAddress.setVisibility(View.GONE);
                Toast.makeText(requireContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        orderViewModel.getOrderLiveData().observe(getViewLifecycleOwner(), res -> {
            if (res instanceof Resource.Loading) binding.buttonPlaceOrder.startAnimation();
            else if (res instanceof Resource.Success) {
                binding.buttonPlaceOrder.revertAnimation();
                SoundHelper.playSuccessSound();
                Order placed = res.getData();
                String orderCode = (placed != null) ? placed.getOrderCode() : "ORD001";
                Context ctx = getContext();
                if (ctx != null) AppNotificationHelper.showOrderNotification(ctx, orderCode, PriceCalculator.formatPriceVnd(getFinalTotalPrice()), "Đơn hàng đã được xác nhận");
                showSuccessDialog(orderCode);
            } else if (res instanceof Resource.Error) {
                binding.buttonPlaceOrder.revertAnimation();
                Toast.makeText(requireContext(), res.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog(String orderId) {
        String addr = selectedAddress != null ? selectedAddress.getStreet() + ", " + selectedAddress.getCity() : "Địa chỉ của bạn";
        String msg = "🎉 Cảm ơn bạn đã mua sắm tại TCFurnish!\n\n• Mã đơn: #" + orderId
                + "\n• Tổng thanh toán: " + PriceCalculator.formatPriceVnd(getFinalTotalPrice())
                + (discountAmount > 0 ? "\n• Giảm voucher: -" + PriceCalculator.formatPriceVnd(discountAmount) : "")
                + "\n• Giao đến: " + addr + "\n\nĐơn hàng đã được lưu và đang chuẩn bị giao.";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đặt hàng thành công!")
                .setMessage(msg)
                .setPositiveButton("Theo dõi đơn hàng", (d, w) -> {
                    d.dismiss();
                    try { Navigation.findNavController(requireView()).navigate(R.id.action_billingFragment_to_ordersFragment); }
                    catch (Exception e) { Navigation.findNavController(requireView()).navigateUp(); }
                })
                .setNegativeButton("Về trang chủ", (d, w) -> {
                    d.dismiss();
                    Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
                })
                .setCancelable(false).show();
    }

    private void bindSelectedAddress(Address address) {
        if (address != null) {
            binding.tvBillingRecipientName.setText(address.getFullName() != null ? address.getFullName() : "Khách hàng");
            binding.tvBillingAddressDetail.setText(address.getStreet() + ", " + address.getCity());
            binding.tvBillingPhoneDisplay.setText(address.getPhoneNumb() != null ? address.getPhoneNumb() : "Chưa có SĐT");
            binding.layoutBillingAddressDetails.setVisibility(View.VISIBLE);
        } else binding.layoutBillingAddressDetails.setVisibility(View.GONE);
    }

    private void updatePriceDisplay() {
        binding.tvSubtotalPrice.setText(PriceCalculator.formatPriceVnd(totalPrice));
        binding.tvTotalPrice.setText(PriceCalculator.formatPriceVnd(getFinalTotalPrice()));
        binding.layoutVoucherDiscount.setVisibility(discountAmount > 0 ? View.VISIBLE : View.GONE);
        if (discountAmount > 0) binding.tvVoucherDiscount.setText("-" + PriceCalculator.formatPriceVnd(discountAmount));
    }

    private float getFinalTotalPrice() {
        return Math.max(0f, totalPrice - discountAmount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
