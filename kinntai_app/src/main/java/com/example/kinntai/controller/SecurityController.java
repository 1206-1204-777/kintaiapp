//package com.example.kinntai.controller;
//
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
///*ユーザー情報修正用のコントローラ
// * ログイン済のユーザーだけが修正用ページに遷移できる*/
//@Controller
//public class SecurityController {
//	
//	@GetMapping("/profile_edit")
//	@PreAuthorize("isAuthenticated()")
//	private String showProfileEditPage() {
//		return "profile_edit";
//
//	}
//}
