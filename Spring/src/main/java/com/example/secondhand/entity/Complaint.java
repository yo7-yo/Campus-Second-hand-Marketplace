package com.example.secondhand.entity;

import java.time.LocalDateTime;

public class Complaint {
    private Integer id;
    private Integer orderId;
    private Integer complainantId;
    private Integer respondentId;
    private String reason;
    private String respondentReply;
    private String status;
    private String adminReply;
    private LocalDateTime createdTime;
    private LocalDateTime resolvedTime;
    private String complainantName;
    private String respondentName;
    private String productTitle;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getComplainantId() { return complainantId; }
    public void setComplainantId(Integer complainantId) { this.complainantId = complainantId; }
    public Integer getRespondentId() { return respondentId; }
    public void setRespondentId(Integer respondentId) { this.respondentId = respondentId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRespondentReply() { return respondentReply; }
    public void setRespondentReply(String respondentReply) { this.respondentReply = respondentReply; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getResolvedTime() { return resolvedTime; }
    public void setResolvedTime(LocalDateTime resolvedTime) { this.resolvedTime = resolvedTime; }
    public String getComplainantName() { return complainantName; }
    public void setComplainantName(String complainantName) { this.complainantName = complainantName; }
    public String getRespondentName() { return respondentName; }
    public void setRespondentName(String respondentName) { this.respondentName = respondentName; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
}
