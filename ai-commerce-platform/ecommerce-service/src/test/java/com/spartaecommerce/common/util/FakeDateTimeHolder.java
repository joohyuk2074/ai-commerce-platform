package com.spartaecommerce.common.util;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FakeDateTimeHolder implements DateTimeHolder {

    private LocalDateTime currentDateTime;

    public FakeDateTimeHolder(LocalDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    @Override
    public LocalDateTime getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(LocalDateTime dateTime) {
        this.currentDateTime = dateTime;
    }
}