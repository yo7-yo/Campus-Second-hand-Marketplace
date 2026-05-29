package com.example.secondhand.service;

import com.example.secondhand.entity.User;
import com.example.secondhand.mapper.UserMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public User create(User user) {
        userMapper.insert(user);
        return user;
    }

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        user.setPassword(null);
        return user;
    }

    public boolean isAdmin(Integer userId) {
        User user = userMapper.findById(userId);
        return user != null && Boolean.TRUE.equals(user.getIsAdmin());
    }

    public void updateCreditScore(Integer userId, Integer creditScore) {
        userMapper.updateCreditScore(userId, creditScore);
    }
}
