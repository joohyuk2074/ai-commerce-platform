package com.spartaecommerce.user.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private Long userId;

    private String username;

    private String password;

    private String email;

    private String name;

    private String phoneNumber;

    private UserGrade grade;

    private boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static User createNew(
        String username,
        String encodedPassword,
        String email,
        String name,
        String phoneNumber
    ) {
        return User.builder()
            .username(username)
            .password(encodedPassword)
            .email(email)
            .name(name)
            .phoneNumber(phoneNumber)
            .grade(UserGrade.NORMAL)
            .deleted(false)
            .build();
    }

    public static User createNewForAuth(
        String email,
        String name,
        String phoneNumber
    ) {
        return User.builder()
            .email(email)
            .name(name)
            .phoneNumber(phoneNumber)
            .grade(UserGrade.NORMAL)
            .deleted(false)
            .build();
    }

    public void update(String name, String phoneNumber) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void delete() {
        this.deleted = true;
    }

    public boolean isVip() {
        return this.grade.equals(UserGrade.VIP);
    }

    public boolean isAdmin() {
        return this.grade.equals(UserGrade.ADMIN);
    }

//    public boolean isPasswordMatch(String rawPassword, String encodedPassword) {
//        return this.password.equals(encodedPassword);
//    }
}
