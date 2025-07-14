package com.example.kinntai.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*トランザクションの判定とメッセージを出力するのに使用*/

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse {
	private String message;
	private boolean success;
}
