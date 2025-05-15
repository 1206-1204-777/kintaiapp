package com.example.kinntai.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AdminRegister {
	@NotNull(message = "名前が入力されていません。")
	private String username;
	@NotNull(message = "パスワードが入力されていません。")

	private String password;
	@NotNull(message = "メールアドレスが入力されていません。")

	private String email;
}
