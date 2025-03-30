package com.paidora.app.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class DataResponse<T> extends ApiResponse {
    private T data;

    @Builder
    protected DataResponse(T data, boolean isSuccess, String error) {
        super(isSuccess, error);
        this.data = data;
    }

    public static <T> DataResponse<T> success(T data) {
        return DataResponse.<T>builder()
                .data(data)
                .isSuccess(true)
                .build();
    }

    public static <T> DataResponse<T> error(String error) {
        return DataResponse.<T>builder()
                .error(error)
                .isSuccess(false)
                .build();
    }
}
