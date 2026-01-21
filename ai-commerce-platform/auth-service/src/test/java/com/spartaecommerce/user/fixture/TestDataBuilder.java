package com.spartaecommerce.user.fixture;

import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.entity.UserGrade;

import java.time.LocalDateTime;

/**
 * Test Data Builder for User domain tests.
 * <p>
 * Big Tech Testing Pattern:
 * - Fluent API for readable test setup
 * - Sensible defaults to reduce boilerplate
 * - Only specify what matters for each test
 * - Immutable by default (creates new instances)
 * <p>
 * Example usage:
 * <pre>
 * RegisterUserCommand command = aRegisterUserCommand()
 *     .withUsername("customuser")
 *     .build();
 * </pre>
 */
public class TestDataBuilder {

    /**
     * Creates a builder for RegisterUserCommand with sensible defaults.
     */
    public static RegisterUserCommandBuilder aRegisterUserCommand() {
        return new RegisterUserCommandBuilder();
    }

    /**
     * Creates a builder for User entity with sensible defaults.
     */
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    /**
     * Builder for RegisterUserCommand.
     * Follows the Test Data Builder pattern from "Growing Object-Oriented Software, Guided by Tests"
     */
    public static class RegisterUserCommandBuilder {
        private String username = "testuser";
        private String password = "password123";
        private String email = "test@example.com";
        private String name = "테스트유저";
        private String phoneNumber = "01012345678";

        public RegisterUserCommandBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public RegisterUserCommandBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public RegisterUserCommandBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public RegisterUserCommandBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RegisterUserCommandBuilder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        /**
         * Creates a command with invalid/empty username.
         * Useful for validation tests.
         */
        public RegisterUserCommandBuilder withInvalidUsername() {
            this.username = "";
            return this;
        }

        /**
         * Creates a command with invalid email format.
         * Useful for validation tests.
         */
        public RegisterUserCommandBuilder withInvalidEmail() {
            this.email = "invalid-email";
            return this;
        }

        public RegisterUserCommand build() {
            return new RegisterUserCommand(
                username,
                password,
                email,
                name,
                phoneNumber
            );
        }
    }

    /**
     * Builder for User entity.
     * Useful for setting up test fixtures with existing users.
     */
    public static class UserBuilder {
        private Long userId = 1L;
        private String username = "existinguser";
        private String password = "encoded_password123";
        private String email = "existing@example.com";
        private String name = "기존유저";
        private String phoneNumber = "01087654321";
        private UserGrade grade = UserGrade.NORMAL;
        private boolean deleted = false;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public UserBuilder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public UserBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserBuilder withGrade(UserGrade grade) {
            this.grade = grade;
            return this;
        }

        public UserBuilder asVip() {
            this.grade = UserGrade.VIP;
            return this;
        }

        public UserBuilder asAdmin() {
            this.grade = UserGrade.ADMIN;
            return this;
        }

        public UserBuilder asDeleted() {
            this.deleted = true;
            return this;
        }

        public UserBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public User build() {
            return User.builder()
                .userId(userId)
                .username(username)
                .password(password)
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .grade(grade)
                .deleted(deleted)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        }
    }
}
