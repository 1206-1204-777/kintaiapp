//package com.example.kinntai.controller;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.web.servlet.MockMvc;
//
//import com.example.kinntai.dto.AdminRegister;
//import com.example.kinntai.dto.AttendanceResponse;
//import com.example.kinntai.service.AdminService;
//import com.example.kinntai.service.impl.AttendanceService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@WebMvcTest(AdminController.class)
//class AdminControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Mock
//    private AdminService adminService;
//
//    @Mock
//    private AttendanceService attendanceService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    @DisplayName("POST /api/auth/admin/signup - 成功")
//    void testRegisterAdmin() throws Exception {
//        AdminRegister mockRegister = new AdminRegister();
//        mockRegister.setEmail("admin@example.com");
//        mockRegister.setPassword("securepass");
//
//        when(adminService.adminRegister(any(AdminRegister.class)))
//            .thenReturn(ResponseEntity.ok("Success"));
//
//        mockMvc.perform(post("/api/auth/admin/signup")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(mockRegister)))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Success"));
//    }
//
//    @Test
//    @DisplayName("GET /api/admin/attendance/list - 成功")
//    void testGetAllAttendance() throws Exception {
//        AttendanceResponse response1 = new AttendanceResponse();
//        response1.setName("user1");
//
//        AttendanceResponse response2 = new AttendanceResponse();
//        response2.setName("user2");
//
//        List<AttendanceResponse> attendanceList = Arrays.asList(response1, response2);
//
//        when(attendanceService.getAllAttendance()).thenReturn(attendanceList);
//
//        mockMvc.perform(get("/api/admin/attendance/list"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].username").value("user1"))
//                .andExpect(jsonPath("$[1].username").value("user2"));
//    }
//}
