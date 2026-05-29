package com.example.secondhand.mapper;

import com.example.secondhand.entity.Message;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Select("""
            SELECT m.id AS id, m.senderid AS senderId, m.receiverid AS receiverId,
                   m.content AS content, m.createdtime AS createdTime,
                   su.username AS senderName, ru.username AS receiverName
            FROM message m
            LEFT JOIN [user] su ON m.senderid = su.id
            LEFT JOIN [user] ru ON m.receiverid = ru.id
            WHERE (m.senderid = #{user1} AND m.receiverid = #{user2})
               OR (m.senderid = #{user2} AND m.receiverid = #{user1})
            ORDER BY m.createdtime ASC
            """)
    List<Message> findByUsers(@Param("user1") Integer user1, @Param("user2") Integer user2);

    @Select("""
            SELECT m.id AS id, m.senderid AS senderId, m.receiverid AS receiverId,
                   m.content AS content, m.createdtime AS createdTime,
                   su.username AS senderName, ru.username AS receiverName
            FROM message m
            LEFT JOIN [user] su ON m.senderid = su.id
            LEFT JOIN [user] ru ON m.receiverid = ru.id
            WHERE m.id IN (
                SELECT MAX(id) FROM message
                WHERE senderid = #{userId} OR receiverid = #{userId}
                GROUP BY CASE WHEN senderid = #{userId} THEN receiverid ELSE senderid END
            )
            ORDER BY m.createdtime DESC
            """)
    List<Message> findConversations(@Param("userId") Integer userId);

    @Insert("INSERT INTO message(senderid, receiverid, content) VALUES(#{senderId}, #{receiverId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    @Select("""
            SELECT m.id AS id, m.senderid AS senderId, m.receiverid AS receiverId,
                   m.content AS content, m.createdtime AS createdTime,
                   su.username AS senderName, ru.username AS receiverName
            FROM message m
            LEFT JOIN [user] su ON m.senderid = su.id
            LEFT JOIN [user] ru ON m.receiverid = ru.id
            WHERE m.id = #{id}
            """)
    Message findById(@Param("id") Integer id);
}
