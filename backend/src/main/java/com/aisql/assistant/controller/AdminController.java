package com.aisql.assistant.controller;

import com.aisql.assistant.dto.UserSummary;
import com.aisql.assistant.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/users")
    public List<UserSummary> listUsers() {
        return userRepository.findAll().stream().map(UserSummary::from).toList();
    }
}
