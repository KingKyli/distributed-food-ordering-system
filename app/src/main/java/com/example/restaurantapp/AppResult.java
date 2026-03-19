package com.example.restaurantapp;

public final class AppResult<T> {
    private final boolean success;
    private final T data;
    private final String message;

    private AppResult(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> AppResult<T> success(T data) {
        return new AppResult<>(true, data, null);
    }

    public static <T> AppResult<T> error(String message) {
        return new AppResult<>(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}

