package com.spartaecommerce.user.domain.entity;

public enum UserGrade {
    NORMAL,
    VIP,
    ADMIN;

    public String toRole() {
        return "ROLE_" + this.name();
    }
}
