package com.example.secondhand.controller;

import com.example.secondhand.entity.Message;
import com.example.secondhand.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "获取两人之间的聊天记录")
    @GetMapping
    public List<Message> findByUsers(@RequestParam Integer user1, @RequestParam Integer user2) {
        return messageService.findByUsers(user1, user2);
    }

    @Operation(summary = "获取用户的会话列表")
    @GetMapping("/conversations")
    public List<Message> findConversations(@RequestParam Integer userId) {
        return messageService.findConversations(userId);
    }

    @Operation(summary = "发送消息")
    @PostMapping
    public Message send(@RequestBody Message message) {
        return messageService.send(message);
    }
}
