package com.spartaecommerce.user.infrastructure.persistence.jpa.entity;

import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.entity.UserGrade;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column
    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserGrade grade;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static UserJpaEntity from(User user) {
        return new UserJpaEntity(
            user.getUserId(),
            user.getEmail(),
            user.getName(),
            user.getPhoneNumber(),
            user.getGrade(),
            user.isDeleted(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public User toDomain() {
        return User.builder()
            .userId(this.userId)
            .email(this.email)
            .name(this.name)
            .phoneNumber(this.phoneNumber)
            .grade(this.grade)
            .deleted(this.deleted)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}
