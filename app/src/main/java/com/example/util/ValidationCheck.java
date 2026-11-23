package com.example.util;

import android.util.Patterns;

public class ValidationCheck {

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
}
