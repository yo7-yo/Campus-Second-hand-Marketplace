package com.example.secondhand.controller;

import com.example.secondhand.entity.TradeOrder;
import com.example.secondhand.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "查询订单列表")
    @GetMapping
    public List<TradeOrder> list() {
        return orderService.findAll();
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public TradeOrder create(@RequestBody Map<String, Integer> body) {
        return orderService.createOrder(body.get("productId"), body.get("buyerId"));
    }

    @Operation(summary = "完成订单")
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Integer id) {
        try {
            orderService.complete(id);
            return ResponseEntity.ok(Map.of("message", "订单已完成"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "买家申请退款")
    @PutMapping("/{id}/refund")
    public ResponseEntity<?> requestRefund(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            orderService.requestRefund(id, body.get("reason"));
            return ResponseEntity.ok(Map.of("message", "退款申请已提交"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "卖家回应退款")
    @PutMapping("/{id}/refund/respond")
    public ResponseEntity<?> respondRefund(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        try {
            orderService.respondRefund(id, body.get("response"));
            return ResponseEntity.ok(Map.of("message", "已回应退款申请"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "管理员批准退款")
    @PutMapping("/{id}/refund/approve")
    public ResponseEntity<?> approveRefund(@PathVariable Integer id) {
        try {
            orderService.approveRefund(id);
            return ResponseEntity.ok(Map.of("message", "退款已批准"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "管理员驳回退款")
    @PutMapping("/{id}/refund/reject")
    public ResponseEntity<?> rejectRefund(@PathVariable Integer id) {
        try {
            orderService.rejectRefund(id);
            return ResponseEntity.ok(Map.of("message", "退款已驳回"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "查询待审核退款")
    @GetMapping("/refunds/pending")
    public List<TradeOrder> pendingRefunds() {
        return orderService.findPendingRefunds();
    }

    @Operation(summary = "查询买家的购买记录")
    @GetMapping("/buyer/{buyerId}")
    public List<TradeOrder> findByBuyerId(@PathVariable Integer buyerId) {
        return orderService.findByBuyerId(buyerId);
    }

    @Operation(summary = "查询卖家的出售记录")
    @GetMapping("/seller/{sellerId}")
    public List<TradeOrder> findBySellerId(@PathVariable Integer sellerId) {
        return orderService.findBySellerId(sellerId);
    }
}
