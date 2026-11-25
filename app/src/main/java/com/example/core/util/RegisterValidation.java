package com.example.core.util;

import java.util.Objects;

public abstract class RegisterValidation {

    public static final class Success extends RegisterValidation {
        public Success() {}

        @Override
        public boolean equals(Object o) {
            return o instanceof Success;
        }

        @Override
        public int hashCode() {
            return Success.class.hashCode();
        }
    }

    public static final class Failed extends RegisterValidation {
        private final String message;

        public Failed(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Failed failed = (Failed) o;
            return Objects.equals(message, failed.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }
    }
}
