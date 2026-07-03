package com.aisql.assistant.dto;

import com.aisql.assistant.model.User;

public class UserSummary {

    private Long id;
    private String name;
    private String email;
    private String role;

    public static UserSummary from(User u) {
        UserSummary s = new UserSummary();
        s.id = u.getId();
        s.name = u.getName();
        s.email = u.getEmail();
        s.role = u.getRole().name();
        return s;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
