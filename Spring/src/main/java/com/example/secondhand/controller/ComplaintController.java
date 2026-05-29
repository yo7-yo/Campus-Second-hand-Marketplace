package com.example.secondhand.controller;

import com.example.secondhand.entity.Complaint;
import com.example.secondhand.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {
    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @Operation(summary = "提交投诉")
    @PostMapping
    public Complaint file(@RequestBody Complaint complaint) {
        return complaintService.file(complaint);
    }

    @Operation(summary = "查询全部投诉")
    @GetMapping
    public List<Complaint> list() {
        return complaintService.findAll();
    }

    @Operation(summary = "查询待审核投诉")
    @GetMapping("/pending")
    public List<Complaint> pending() {
        return complaintService.findPending();
    }

    @Operation(summary = "被投诉人回复")
    @PutMapping("/{id}/reply")
    public ResponseEntity<?> respondentReply(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        complaintService.respondentReply(id, body.get("respondentReply"));
        return ResponseEntity.ok(Map.of("message", "回复成功"));
    }

    @Operation(summary = "审核投诉")
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        try {
            boolean approved = Boolean.TRUE.equals(body.get("approved"));
            String adminReply = (String) body.get("adminReply");
            complaintService.resolve(id, approved, adminReply);
            return ResponseEntity.ok(Map.of("message", approved ? "投诉已通过，已扣信用分" : "投诉已驳回"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
