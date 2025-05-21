package com.example.kinntai.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kinntai.entity.Break; // Break エンティティをインポート
import com.example.kinntai.service.BreakService; // BreakService をインポート

@RestController
@RequestMapping("/api/break") // /api/break 配下のパスを扱う
@CrossOrigin(origins = "*") // CORS設定 (開発中は "*" で許可。本番環境では制限を推奨)
public class BreakController {

    private final BreakService breakService;

    @Autowired
    public BreakController(BreakService breakService) {
        this.breakService = breakService;
    }

    /**
     * 休憩状態を取得するAPI (進行中の休憩があるか)
     * GET /api/break/status/{userId}/today
     * フロントエンドの checkAttendanceStatus() から呼び出される
     */
    @GetMapping("/status/{userId}/today")
    public ResponseEntity<?> getBreakStatusToday(@PathVariable Long userId) {
        try {
            // BreakService に boolean isOngoingBreakToday(Long userId) メソッドが実装されていることを前提
            boolean ongoingBreak = breakService.isOngoingBreakToday(userId);

            Map<String, Boolean> response = new HashMap<>();
            response.put("ongoingBreak", ongoingBreak);

            System.out.println("GET /api/break/status/" + userId + "/today -> ongoingBreak: " + ongoingBreak);

            return ResponseEntity.ok(response); // 200 OK
        } catch (RuntimeException e) {
             System.err.println("休憩状態取得エラー (サービス例外): userId=" + userId + ", message=" + e.getMessage());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (Exception e) {
            System.err.println("休憩状態取得エラー (予期せぬ例外): userId=" + userId + ", message=" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("サーバーエラーが発生しました"); // 500 Internal Server Error
        }
    }

    /**
     * 今日の休憩時間合計を取得するAPI
     * GET /api/break/total/{userId}/today
     * フロントエンドの loadTodayAttendance() から呼び出される
     */
    @GetMapping("/total/{userId}/today")
    public ResponseEntity<?> getTotalBreakMinutesToday(@PathVariable Long userId) {
         try {
             // BreakService に Long getTotalBreakMinutesForToday(Long userId) メソッドが実装されていることを前提
             Long totalMinutes = breakService.getTotalBreakMinutesForToday(userId);

             Map<String, Long> response = new HashMap<>();
             response.put("totalMinutes", totalMinutes);

             System.out.println("GET /api/break/total/" + userId + "/today -> totalMinutes: " + totalMinutes);

             return ResponseEntity.ok(response); // 200 OK
         } catch (RuntimeException e) {
             System.err.println("今日の休憩時間合計取得エラー (サービス例外): userId=" + userId + ", message=" + e.getMessage());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
         } catch (Exception e) {
             System.err.println("今日の休憩時間合計取得エラー (予期せぬ例外): userId=" + userId + ", message=" + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("サーバーエラーが発生しました");
         }
    }

    /**
     * 休憩開始API
     * POST /api/break/start/{userId}
     * フロントエンドの startBreak() から呼び出される
     */
    @PostMapping("/start/{userId}")
    public ResponseEntity<?> startBreak(@PathVariable Long userId) {
        try {
            System.out.println("POST /api/break/start/" + userId);
            // BreakService.startBreak(userId) を呼び出す
            Break startedBreak = breakService.startBreak(userId); // サービスメソッドを実行
            return ResponseEntity.ok(startedBreak); // 成功したら作成されたBreakエンティティをJSONで返す
        } catch (RuntimeException e) {
            System.err.println("休憩開始エラー: userId=" + userId + ", message=" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400 Bad Request
        } catch (Exception e) {
            System.err.println("休憩開始エラー (予期せぬ例外): userId=" + userId + ", message=" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("サーバーエラーが発生しました"); // 500 Internal Server Error
        }
    }

    /**
     * 休憩終了API
     * POST /api/break/end/{userId}
     * フロントエンドの endBreak() から呼び出される
     */
    @PostMapping("/end/{userId}")
    public ResponseEntity<?> endBreak(@PathVariable Long userId) {
        try {
            System.out.println("POST /api/break/end/" + userId);
            // BreakService.endBreak(userId) を呼び出す
            Break endedBreak = breakService.endBreak(userId); // サービスメソッドを実行
            return ResponseEntity.ok(endedBreak); // 成功したら更新されたBreakエンティティをJSONで返す
        } catch (RuntimeException e) {
            System.err.println("休憩終了エラー: userId=" + userId + ", message=" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("休憩終了エラー (予期せぬ例外): userId=" + userId + ", message=" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("サーバーエラーが発生しました");
        }
    }
}
