package com.spartaecommerce.user.application;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.user.application.dto.command.UserCreateCommand;
import com.spartaecommerce.user.application.dto.command.UserUpdateCommand;
import com.spartaecommerce.user.application.dto.query.UserSearchQuery;
import com.spartaecommerce.user.application.dto.result.UserResult;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.repository.UserRepository;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.repository.PointWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PointWalletRepository walletRepository;

    @Transactional
    public Long create(UserCreateCommand createCommand) {
        if (userRepository.existsByEmail(createCommand.email())) {
            throw new BusinessException(ErrorCode.ENTITY_ALREADY_EXISTS, "Email: " + createCommand.email());
        }

        User user = User.createNew(
            createCommand.email(),
            createCommand.name(),
            createCommand.phoneNumber()
        );
        Long userId = userRepository.save(user);

        PointWallet wallet = PointWallet.createNew(userId);
        walletRepository.save(wallet);

        return userId;
    }

    public UserResult getById(Long userId) {
        User user = userRepository.getById(userId);
        return UserResult.from(user);
    }

    public Page<UserResult> search(UserSearchQuery searchQuery) {
        return userRepository.search(searchQuery)
            .map(UserResult::from);
    }

    @Transactional
    public void update(Long userId, UserUpdateCommand updateCommand) {
        User user = userRepository.getById(userId);
        user.update(updateCommand.name(), updateCommand.phoneNumber());
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long userId) {
        User user = userRepository.getById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Already deleted user: " + userId
            );
        }

        user.delete();
        userRepository.save(user);

        PointWallet wallet = walletRepository.getByUserId(userId);
        wallet.deactivate();
        walletRepository.save(wallet);
    }
}
