package com.example.secondhand.mapper;

import com.example.secondhand.entity.Complaint;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ComplaintMapper {
    @Insert("INSERT INTO complaint(order_id, complainant_id, respondent_id, reason, status) VALUES(#{orderId}, #{complainantId}, #{respondentId}, #{reason}, 'pending')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Complaint complaint);

    @Select("""
            SELECT c.id, c.order_id AS orderId, c.complainant_id AS complainantId,
                   c.respondent_id AS respondentId, c.reason, c.respondent_reply AS respondentReply, c.status,
                   c.admin_reply AS adminReply, c.created_time AS createdTime, c.resolved_time AS resolvedTime,
                   cu.username AS complainantName, ru.username AS respondentName,
                   p.title AS productTitle
            FROM complaint c
            LEFT JOIN [user] cu ON c.complainant_id = cu.id
            LEFT JOIN [user] ru ON c.respondent_id = ru.id
            LEFT JOIN tradeorder o ON c.order_id = o.id
            LEFT JOIN product p ON o.productid = p.id
            ORDER BY c.id DESC
            """)
    List<Complaint> findAll();

    @Select("""
            SELECT c.id, c.order_id AS orderId, c.complainant_id AS complainantId,
                   c.respondent_id AS respondentId, c.reason, c.respondent_reply AS respondentReply, c.status,
                   c.admin_reply AS adminReply, c.created_time AS createdTime, c.resolved_time AS resolvedTime,
                   cu.username AS complainantName, ru.username AS respondentName,
                   p.title AS productTitle
            FROM complaint c
            LEFT JOIN [user] cu ON c.complainant_id = cu.id
            LEFT JOIN [user] ru ON c.respondent_id = ru.id
            LEFT JOIN tradeorder o ON c.order_id = o.id
            LEFT JOIN product p ON o.productid = p.id
            WHERE c.status = 'pending'
            ORDER BY c.id DESC
            """)
    List<Complaint> findPending();

    @Update("UPDATE complaint SET status = #{status}, admin_reply = #{adminReply}, resolved_time = #{resolvedTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") String status, @Param("adminReply") String adminReply, @Param("resolvedTime") LocalDateTime resolvedTime);

    @Update("UPDATE complaint SET respondent_reply = #{respondentReply} WHERE id = #{id}")
    int updateRespondentReply(@Param("id") Integer id, @Param("respondentReply") String respondentReply);
}
