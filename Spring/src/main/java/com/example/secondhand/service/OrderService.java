package com.example.secondhand.service;

import com.example.secondhand.entity.Product;
import com.example.secondhand.entity.TradeOrder;
import com.example.secondhand.mapper.ProductMapper;
import com.example.secondhand.mapper.TradeOrderMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {
    private final TradeOrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderService(TradeOrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    public List<TradeOrder> findAll() {
        return orderMapper.findAll();
    }

    public TradeOrder createOrder(Integer productId, Integer buyerId) {
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (!"available".equals(product.getStatus())) {
            throw new IllegalArgumentException("商品已不可购买");
        }
        TradeOrder order = new TradeOrder();
        order.setProductId(productId);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getOwnerId());
        order.setStatus("pending");
        orderMapper.insert(order);
        productMapper.updateStatus(productId, "reserved");
        return order;
    }

    public void complete(Integer id) {
        TradeOrder order = orderMapper.findById(id);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        orderMapper.updateStatus(id, "completed");
        productMapper.updateStatus(order.getProductId(), "sold");
    }

    public void requestRefund(Integer id, String reason) {
        TradeOrder order = orderMapper.findById(id);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if ("refunded".equals(order.getStatus())) throw new IllegalArgumentException("订单已退款");
        if ("completed".equals(order.getStatus())) throw new IllegalArgumentException("订单已完成，无法退款");
        orderMapper.requestRefund(id, reason);
    }

    public void respondRefund(Integer id, String response) {
        TradeOrder order = orderMapper.findById(id);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if (!"refund_pending".equals(order.getStatus())) throw new IllegalArgumentException("该订单无退款申请");
        orderMapper.respondRefund(id, response);
    }

    public void approveRefund(Integer id) {
        TradeOrder order = orderMapper.findById(id);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        orderMapper.approveRefund(id);
        productMapper.updateStatus(order.getProductId(), "available");
    }

    public void rejectRefund(Integer id) {
        TradeOrder order = orderMapper.findById(id);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        orderMapper.rejectRefund(id);
    }

    public List<TradeOrder> findPendingRefunds() {
        return orderMapper.findPendingRefunds();
    }

    public List<TradeOrder> findByBuyerId(Integer buyerId) {
        return orderMapper.findByBuyerId(buyerId);
    }

    public List<TradeOrder> findBySellerId(Integer sellerId) {
        return orderMapper.findBySellerId(sellerId);
    }
}
