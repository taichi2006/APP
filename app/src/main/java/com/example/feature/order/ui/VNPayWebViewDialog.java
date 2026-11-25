package com.example.feature.order.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.aroom.databinding.DialogVnpayConfigBinding;
import com.example.aroom.databinding.DialogVnpayWebviewBinding;
import com.example.core.util.SoundHelper;
import com.example.core.util.VNPayHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class VNPayWebViewDialog {

    public interface OnVNPayPaymentListener {
        void onPaymentSuccess(String paymentMethod, String paymentStatus);
        void onPaymentFailed(String errorCode);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void show(Context context, long orderId, float totalAmount, OnVNPayPaymentListener listener) {
        if (context == null) return;

        Dialog dialog = new Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        DialogVnpayWebviewBinding binding = DialogVnpayWebviewBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        // Cấu hình WebView
        WebSettings webSettings = binding.vnpayWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        binding.vnpayWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                binding.vnpayProgressBar.setVisibility(View.VISIBLE);
                checkPaymentResult(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.vnpayProgressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request != null && request.getUrl() != null ? request.getUrl().toString() : "";
                if (checkPaymentResult(url)) return true;
                return super.shouldOverrideUrlLoading(view, request);
            }

            private boolean checkPaymentResult(String url) {
                if (url == null || !url.contains("vnp_ResponseCode=")) return false;

                Uri uri = Uri.parse(url);
                String responseCode = uri.getQueryParameter("vnp_ResponseCode");
                String txnNo = uri.getQueryParameter("vnp_TransactionNo");
                if (txnNo == null || txnNo.isEmpty()) txnNo = uri.getQueryParameter("vnp_TxnRef");
                if (txnNo == null || txnNo.isEmpty()) txnNo = String.valueOf(orderId);

                if ("00".equals(responseCode)) {
                    SoundHelper.playSuccessSound();
                    Toast.makeText(context, "Thanh toán VNPAY Sandbox thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onPaymentSuccess(
                                "Cổng VNPAY Sandbox (Trực tuyến)",
                                "Đã thanh toán (Mã GD VNPAY: " + txnNo + ")"
                        );
                    }
                    return true;
                } else {
                    Toast.makeText(context, "Giao dịch VNPAY đã bị hủy hoặc không thành công (Mã: " + responseCode + ")", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    if (listener != null) listener.onPaymentFailed(responseCode);
                    return true;
                }
            }
        });

        // Tải trang thanh toán Sandbox thật của VNPAY
        String paymentUrl = VNPayHelper.createPaymentUrl(context, orderId, totalAmount, "Thanh toan don hang #" + orderId);
        binding.vnpayWebView.loadUrl(paymentUrl);

        // Nút đóng
        binding.btnVnpayClose.setOnClickListener(v -> dialog.dismiss());

        // Nút cấu hình Merchant VNPAY
        binding.btnVnpayConfig.setOnClickListener(v -> showConfigDialog(context, binding.vnpayWebView, orderId, totalAmount));

        dialog.show();
    }

    private static void showConfigDialog(Context context, WebView webView, long orderId, float totalAmount) {
        DialogVnpayConfigBinding configBinding = DialogVnpayConfigBinding.inflate(LayoutInflater.from(context));
        Dialog configDialog = new MaterialAlertDialogBuilder(context)
                .setView(configBinding.getRoot())
                .create();

        configBinding.etConfigTmnCode.setText(VNPayHelper.getTmnCode(context));
        configBinding.etConfigHashSecret.setText(VNPayHelper.getHashSecret(context));

        configBinding.btnSaveConfig.setOnClickListener(v -> {
            String tmn = configBinding.etConfigTmnCode.getText() != null ? configBinding.etConfigTmnCode.getText().toString().trim() : "";
            String secret = configBinding.etConfigHashSecret.getText() != null ? configBinding.etConfigHashSecret.getText().toString().trim() : "";
            if (tmn.isEmpty() || secret.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ TmnCode và HashSecret!", Toast.LENGTH_SHORT).show();
                return;
            }
            VNPayHelper.saveConfig(context, tmn, secret);
            Toast.makeText(context, "Đã lưu cấu hình VNPAY Sandbox!", Toast.LENGTH_SHORT).show();
            configDialog.dismiss();

            String newUrl = VNPayHelper.createPaymentUrl(context, orderId, totalAmount, "Thanh toan don hang #" + orderId);
            webView.loadUrl(newUrl);
        });

        configBinding.btnCancelConfig.setOnClickListener(v -> configDialog.dismiss());
        configDialog.show();
    }
}
