package com.example.kinntai.config;

/*トランザクションの判定とメッセージを出力するのに使用*/
public class ApiResponse {
	private String message;
	private boolean success;

	ApiResponse(String message, boolean success) {
		this.message = message;
		this.success = success;

	}

	public String getMessage() {
		return message;

	}

	@SuppressWarnings("unused")
	private boolean isSuccess() {
		return success;
	}
}
