package com.hisrah.accounting.dto.response;

import java.time.OffsetDateTime;

public class ApiResponse<T> {

	private final boolean success;
	private final String message;
	private final T data;
	private final OffsetDateTime timestamp;

	private ApiResponse(boolean success, String message, T data) {
		this.success = success;
		this.message = message;
		this.data = data;
		this.timestamp = OffsetDateTime.now();
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data);
	}

	public static <T> ApiResponse<T> error(String message, T data) {
		return new ApiResponse<>(false, message, data);
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}
}
