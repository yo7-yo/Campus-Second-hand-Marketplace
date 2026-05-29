package com.example.secondhand.service;

import com.example.secondhand.entity.Complaint;
import com.example.secondhand.mapper.ComplaintMapper;
import com.example.secondhand.mapper.UserMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {
    private final ComplaintMapper complaintMapper;
    private final UserMapper userMapper;

    public ComplaintService(ComplaintMapper complaintMapper, UserMapper userMapper) {
        this.complaintMapper = complaintMapper;
        this.userMapper = userMapper;
    }

    public Complaint file(Complaint complaint) {
        complaint.setStatus("pending");
        complaintMapper.insert(complaint);
        return complaint;
    }

    public List<Complaint> findAll() {
        return complaintMapper.findAll();
    }

    public List<Complaint> findPending() {
        return complaintMapper.findPending();
    }

    public void respondentReply(Integer id, String respondentReply) {
        complaintMapper.updateRespondentReply(id, respondentReply);
    }

    public void resolve(Integer id, boolean approved, String adminReply) {
        Complaint complaint = complaintMapper.findAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("投诉不存在"));

        if (approved) {
            complaintMapper.updateStatus(id, "resolved", adminReply, LocalDateTime.now());
            // 扣被投诉人信用分20
            var respondent = userMapper.findById(complaint.getRespondentId());
            if (respondent != null) {
                int newScore = Math.max(0, (respondent.getCreditScore() != null ? respondent.getCreditScore() : 100) - 20);
                userMapper.updateCreditScore(complaint.getRespondentId(), newScore);
            }
        } else {
            complaintMapper.updateStatus(id, "rejected", adminReply, LocalDateTime.now());
        }
    }
}
