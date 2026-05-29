package com.example.secondhand.controller;

import com.example.secondhand.entity.User;
import com.example.secondhand.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "查询用户列表")
    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "检查是否管理员")
    @GetMapping("/check-admin/{id}")
    public Map<String, Boolean> checkAdmin(@PathVariable Integer id) {
        return Map.of("isAdmin", userService.isAdmin(id));
    }

    @Operation(summary = "更新信用分")
    @PutMapping("/{id}/credit-score")
    public ResponseEntity<?> updateCreditScore(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        userService.updateCreditScore(id, body.get("creditScore"));
        return ResponseEntity.ok(Map.of("message", "信用分已更新"));
    }
}
