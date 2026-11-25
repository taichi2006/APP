package com.example.core.util;

import android.util.Patterns;
import com.example.model.Address;

public class ValidationCheck {

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    public static RegisterValidation validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new RegisterValidation.Failed("Email không được để trống");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return new RegisterValidation.Failed("Định dạng email không hợp lệ");
        }
        return new RegisterValidation.Success();
    }

    public static RegisterValidation validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new RegisterValidation.Failed("Mật khẩu không được để trống");
        }
        if (password.length() < 6) {
            return new RegisterValidation.Failed("Mật khẩu phải chứa ít nhất 6 ký tự");
        }
        return new RegisterValidation.Success();
    }

    /**
     * Kiểm tra số điện thoại hợp lệ:
     * - Chấp nhận số điện thoại 10 số (hoặc 9-11 số).
     * - Bỏ qua khoảng trắng, dấu gạch nối, dấu chấm, tự động chuyển +84 thành 0.
     */
    public static boolean isValidVietnamesePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.trim()
                .replace(" ", "")
                .replace("-", "")
                .replace(".", "")
                .replace("+84", "0");
        if (cleanPhone.startsWith("84") && cleanPhone.length() >= 11) {
            cleanPhone = "0" + cleanPhone.substring(2);
        }
        return cleanPhone.matches("^0[0-9]{8,10}$");
    }

    /**
     * Kiểm tra thông tin địa chỉ giao hàng hợp lệ trước khi đặt hàng.
     * Quy tắc thân thiện, không gắt gao: quan trọng là số điện thoại hợp lệ và có địa chỉ nhận hàng.
     */
    public static ValidationResult validateAddress(Address address, String inputPhone) {
        if (address == null) {
            return new ValidationResult(false, "Vui lòng chọn địa chỉ nhận hàng!");
        }

        String effectivePhone = (inputPhone != null && !inputPhone.trim().isEmpty())
                ? inputPhone.trim()
                : (address.getPhoneNumb() != null ? address.getPhoneNumb().trim() : "");

        if (effectivePhone.isEmpty()) {
            return new ValidationResult(false, "Vui lòng điền số điện thoại nhận hàng!");
        }
        if (!isValidVietnamesePhone(effectivePhone)) {
            return new ValidationResult(false, "Số điện thoại không hợp lệ! Vui lòng nhập số điện thoại gồm 10 chữ số (ví dụ: 0901234567).");
        }

        String street = address.getStreet() != null ? address.getStreet().trim() : "";
        String city = address.getCity() != null ? address.getCity().trim() : "";
        if (street.isEmpty() && city.isEmpty()) {
            return new ValidationResult(false, "Vui lòng nhập địa chỉ nhận hàng chi tiết!");
        }

        return new ValidationResult(true, "");
    }
}
