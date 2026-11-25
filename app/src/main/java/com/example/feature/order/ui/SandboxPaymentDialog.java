package com.example.feature.order.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.aroom.databinding.DialogSandboxPaymentGatewayBinding;
import com.example.core.util.PriceCalculator;
import com.example.core.util.SoundHelper;
import com.example.model.Address;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;
import java.util.Random;

public class SandboxPaymentDialog {

    public interface OnPaymentGatewayListener {
        void onPaymentSuccess(String paymentMethod, String paymentStatus);
    }

    public static void show(Context context, float totalAmount, Address address, OnPaymentGatewayListener listener) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogSandboxPaymentGatewayBinding binding = DialogSandboxPaymentGatewayBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        // Mở rộng bottom sheet tối đa
        View parent = (View) binding.getRoot().getParent();
        if (parent != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        // 1. Hiển thị thông tin đơn hàng và số tiền
        long txnId = 100000L + (long) (new Random().nextDouble() * 899999L);
        binding.tvGatewayOrderId.setText("#TXN" + txnId);
        binding.tvGatewayAmount.setText(PriceCalculator.formatPriceVnd(totalAmount));

        // 2. Đếm ngược hết hạn phiên thanh toán 15 phút
        CountDownTimer timer = new CountDownTimer(15 * 60 * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                binding.tvGatewayCountdown.setText(String.format(Locale.getDefault(), "Hết hạn: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                binding.tvGatewayCountdown.setText("Hết hạn: 00:00");
            }
        };
        timer.start();
        dialog.setOnDismissListener(d -> timer.cancel());

        // 3. Chuyển đổi tab ATM Nội Địa và Thẻ Quốc Tế
        binding.btnTabAtm.setOnClickListener(v -> {
            binding.layoutAtmSection.setVisibility(View.VISIBLE);
            binding.layoutVisaSection.setVisibility(View.GONE);
            binding.btnTabAtm.setBackgroundTintList(ColorStateList.valueOf(0xFF005BAA));
            binding.btnTabAtm.setTextColor(0xFFFFFFFF);
            binding.btnTabVisa.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
            binding.btnTabVisa.setTextColor(0xFF475569);
        });

        binding.btnTabVisa.setOnClickListener(v -> {
            binding.layoutAtmSection.setVisibility(View.GONE);
            binding.layoutVisaSection.setVisibility(View.VISIBLE);
            binding.btnTabVisa.setBackgroundTintList(ColorStateList.valueOf(0xFF005BAA));
            binding.btnTabVisa.setTextColor(0xFFFFFFFF);
            binding.btnTabAtm.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
            binding.btnTabAtm.setTextColor(0xFF475569);
        });

        // 4. Chọn ngân hàng test Sandbox
        binding.btnBankNcb.setOnClickListener(v -> selectBank(binding, 0));
        binding.btnBankVcb.setOnClickListener(v -> selectBank(binding, 1));
        binding.btnBankCtg.setOnClickListener(v -> selectBank(binding, 2));
        binding.btnBankBidv.setOnClickListener(v -> selectBank(binding, 3));

        // 5. Tự động điền thông tin test NCB
        binding.btnQuickFillNcb.setOnClickListener(v -> {
            binding.etAtmCardNumber.setText("9704198526191432198");
            binding.etAtmCardHolder.setText("NGUYEN VAN A");
            binding.etAtmIssueDate.setText("07/15");
            Toast.makeText(context, "Đã điền thẻ test NCB VNPAY Sandbox!", Toast.LENGTH_SHORT).show();
        });

        // 6. Tự động điền thông tin test Visa
        binding.btnQuickFillVisa.setOnClickListener(v -> {
            binding.etVisaCardNumber.setText("4111 1111 1111 1111");
            binding.etVisaCardHolder.setText("NGUYEN VAN A");
            binding.etVisaExpiry.setText("12/28");
            binding.etVisaCvv.setText("123");
            Toast.makeText(context, "Đã điền thẻ test Visa Sandbox!", Toast.LENGTH_SHORT).show();
        });

        // 7. Nút Tiếp tục ở khối ATM -> chuyển sang OTP
        binding.btnAtmContinue.setOnClickListener(v -> {
            String card = binding.etAtmCardNumber.getText() != null ? binding.etAtmCardNumber.getText().toString().trim() : "";
            if (card.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập hoặc nhấn tự động điền số thẻ test!", Toast.LENGTH_SHORT).show();
                return;
            }
            showOtpSection(binding, address);
        });

        // 8. Nút Tiếp tục ở khối Visa -> chuyển sang OTP
        binding.btnVisaContinue.setOnClickListener(v -> {
            String card = binding.etVisaCardNumber.getText() != null ? binding.etVisaCardNumber.getText().toString().trim() : "";
            if (card.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập hoặc nhấn tự động điền số thẻ test!", Toast.LENGTH_SHORT).show();
                return;
            }
            showOtpSection(binding, address);
        });

        // 9. Điền nhanh OTP test 123456
        binding.btnQuickFillOtp.setOnClickListener(v -> binding.etOtpCode.setText("123456"));

        // 10. Xác nhận thanh toán VNPAY OTP
        binding.btnConfirmOtp.setOnClickListener(v -> {
            String otp = binding.etOtpCode.getText() != null ? binding.etOtpCode.getText().toString().trim() : "";
            if (otp.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập mã OTP test 123456!", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.layoutOtpSection.setVisibility(View.GONE);
            binding.layoutGatewayLoading.setVisibility(View.VISIBLE);
            binding.tvGatewayLoadingMessage.setText("Đang kết nối cổng thanh toán VNPAY Sandbox...");

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                binding.tvGatewayLoadingMessage.setText("Xác thực giao dịch thành công! Đang hoàn tất đơn hàng...");
                SoundHelper.playSuccessSound();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onPaymentSuccess(
                                "Cổng Sandbox VNPAY (Trực tuyến)",
                                "Đã thanh toán (Mã GD: VNPAY_" + txnId + ")"
                        );
                    }
                }, 800);
            }, 1200);
        });

        // 11. Hủy giao dịch
        binding.btnCancelGateway.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static void selectBank(DialogSandboxPaymentGatewayBinding binding, int index) {
        int activeColor = 0xFFEBF5FF;
        int defaultColor = 0xFFF8FAFC;
        binding.btnBankNcb.setBackgroundTintList(ColorStateList.valueOf(index == 0 ? activeColor : defaultColor));
        binding.btnBankVcb.setBackgroundTintList(ColorStateList.valueOf(index == 1 ? activeColor : defaultColor));
        binding.btnBankCtg.setBackgroundTintList(ColorStateList.valueOf(index == 2 ? activeColor : defaultColor));
        binding.btnBankBidv.setBackgroundTintList(ColorStateList.valueOf(index == 3 ? activeColor : defaultColor));
    }

    private static void showOtpSection(DialogSandboxPaymentGatewayBinding binding, Address address) {
        binding.layoutAtmSection.setVisibility(View.GONE);
        binding.layoutVisaSection.setVisibility(View.GONE);
        binding.layoutGatewayTabs.setVisibility(View.GONE);
        binding.layoutOtpSection.setVisibility(View.VISIBLE);

        String phone = address != null && address.getPhoneNumb() != null && !address.getPhoneNumb().isEmpty()
                ? address.getPhoneNumb() : "098****123";
        binding.tvOtpDescription.setText("Mã xác thực OTP đã được gửi đến số điện thoại " + phone + " của chủ tài khoản. Vui lòng nhập mã test 123456 để hoàn tất thanh toán.");
    }
}
