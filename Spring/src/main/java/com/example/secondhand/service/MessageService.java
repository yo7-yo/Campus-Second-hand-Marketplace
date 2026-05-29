package com.example.secondhand.service;

import com.example.secondhand.entity.Message;
import com.example.secondhand.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {
    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public List<Message> findByUsers(Integer user1, Integer user2) {
        return messageMapper.findByUsers(user1, user2);
    }

    public List<Message> findConversations(Integer userId) {
        return messageMapper.findConversations(userId);
    }

    public Message send(Message message) {
        messageMapper.insert(message);
        // 重新查询以获取数据库生成的 createdTime
        return messageMapper.findById(message.getId());
    }
}
