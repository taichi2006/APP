package com.example.core.util;

import java.util.Objects;

public class RegisterFieldsState {
    private final RegisterValidation email;
    private final RegisterValidation password;

    public RegisterFieldsState(RegisterValidation email, RegisterValidation password) {
        this.email = email;
        this.password = password;
    }

    public RegisterValidation getEmail() {
        return email;
    }

    public RegisterValidation getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterFieldsState that = (RegisterFieldsState) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }
}
