package com.spartaecommerce.user.domain.port.in;

import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.application.dto.result.UserResult;
import org.springframework.data.domain.Page;

public interface SearchUsersUseCase {

    Page<UserResult> search(UserSearchQuery searchQuery);
}
