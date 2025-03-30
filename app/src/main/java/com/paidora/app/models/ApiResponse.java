package com.paidora.app.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class ApiResponse implements Serializable {
    private boolean isSuccess;
    private String error;

    protected ApiResponse() {
    }

    public ApiResponse(boolean isSuccess, String error) {
        this.isSuccess = isSuccess;
        this.error = error;
    }

    public static ApiResponse successResponse() {
        return new ApiResponse(true, null);
    }

    public static ApiResponse errorResponse(String error) {
        return new ApiResponse(false, error);
    }
}
