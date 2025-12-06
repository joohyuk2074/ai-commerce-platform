package com.spartaecommerce.user.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.repository.PointWalletRepository;
import com.spartaecommerce.user.application.dto.command.RegisterUserCommand;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.in.RegisterUserUseCase;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import com.spartaecommerce.user.domain.port.out.PasswordEncoderPort;
import com.spartaecommerce.user.domain.port.out.SaveUserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegisterUserService implements RegisterUserUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final PointWalletRepository walletRepository;

    @Override
    @Transactional
    public Long register(RegisterUserCommand command) {
        if (loadUserPort.existsByUsername(command.username())) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Username already exists: " + command.username()
            );
        }

        if (loadUserPort.existsByEmail(command.email())) {
            throw new BusinessException(
                ErrorCode.ENTITY_ALREADY_EXISTS,
                "Email already exists: " + command.email()
            );
        }

        String encodedPassword = passwordEncoderPort.encode(command.password());

        User user = User.createNew(
            command.username(),
            encodedPassword,
            command.email(),
            command.name(),
            command.phoneNumber()
        );

        Long userId = saveUserPort.save(user);

        PointWallet wallet = PointWallet.createNew(userId);
        walletRepository.save(wallet);

        log.info("User {} has been created", userId);

        return userId;
    }
}
