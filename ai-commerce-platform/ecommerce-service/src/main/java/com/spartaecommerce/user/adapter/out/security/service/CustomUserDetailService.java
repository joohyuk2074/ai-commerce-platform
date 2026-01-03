package com.spartaecommerce.user.adapter.out.security.service;

import com.spartaecommerce.user.adapter.out.security.CustomUserDetails;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final LoadUserPort loadUserPort;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = loadUserPort.getByUsername(username);

        return new CustomUserDetails(
            user.getUserId(),
            user.getUsername(),
            user.getPassword(),
            user.getGrade()
        );
    }
}
