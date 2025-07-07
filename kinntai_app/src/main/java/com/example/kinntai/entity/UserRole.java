package com.example.kinntai.entity;

/**ユーザーのロールを定義*/
public enum UserRole {
	GENERAL_USER("一般ユーザー"), 
	ADMIN("管理者");

	private final String displayName;

	UserRole(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
