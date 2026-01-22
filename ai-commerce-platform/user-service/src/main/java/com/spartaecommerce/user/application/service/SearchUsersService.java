package com.spartaecommerce.user.application.service;

import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.port.in.SearchUsersUseCase;
import com.spartaecommerce.user.domain.port.out.SearchUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUsersService implements SearchUsersUseCase {

    private final SearchUserPort searchUserPort;

    @Override
    public Page<UserResult> search(UserSearchQuery searchQuery) {
        return searchUserPort.search(searchQuery)
            .map(UserResult::from);
    }
}
