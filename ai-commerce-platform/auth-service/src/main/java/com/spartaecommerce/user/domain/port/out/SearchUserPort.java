package com.spartaecommerce.user.domain.port.out;

import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.domain.entity.User;
import org.springframework.data.domain.Page;

/**
 * 사용자 검색을 위한 Output Port
 */
public interface SearchUserPort {

    /**
     * 사용자 검색
     *
     * @param searchQuery 검색 조건
     * @return 검색 결과 페이지
     */
    Page<User> search(UserSearchQuery searchQuery);
}
