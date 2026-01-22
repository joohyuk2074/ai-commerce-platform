package com.spartaecommerce.common.infrastructure;

import com.spartaecommerce.common.util.DateTimeHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SystemDateTimeHolder implements DateTimeHolder {

    @Override
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
}
