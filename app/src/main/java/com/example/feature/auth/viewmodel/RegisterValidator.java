package com.example.feature.auth.viewmodel;

import com.example.core.util.ValidationCheck;
import com.example.model.User;

public class RegisterValidator {

    public static class Result {
        public final boolean isValid;
        public final String emailError;
        public final String passwordError;

        public Result(boolean isValid, String emailError, String passwordError) {
            this.isValid = isValid;
            this.emailError = emailError;
            this.passwordError = passwordError;
        }
    }

    public static Result validate(User user, String password) {
        String emailErr = null;
        String passErr = null;

        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            emailErr = "Email không được để trống";
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(user.getEmail().trim()).matches()) {
            emailErr = "Email không hợp lệ";
        }

        if (password == null || password.trim().isEmpty()) {
            passErr = "Mật khẩu không được để trống";
        } else if (password.length() < 6) {
            passErr = "Mật khẩu phải có ít nhất 6 ký tự";
        }

        boolean valid = (emailErr == null && passErr == null);
        return new Result(valid, emailErr, passErr);
    }
}
