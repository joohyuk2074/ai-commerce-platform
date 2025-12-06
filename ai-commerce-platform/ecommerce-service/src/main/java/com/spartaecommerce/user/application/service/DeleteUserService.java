package com.spartaecommerce.user.application.service;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.pointwallet.domain.entity.PointWallet;
import com.spartaecommerce.pointwallet.domain.repository.PointWalletRepository;
import com.spartaecommerce.user.domain.entity.User;
import com.spartaecommerce.user.domain.port.in.DeleteUserUseCase;
import com.spartaecommerce.user.domain.port.out.LoadUserPort;
import com.spartaecommerce.user.domain.port.out.SaveUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeleteUserService implements DeleteUserUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final PointWalletRepository walletRepository;

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = loadUserPort.getById(userId);

        if (user.isDeleted()) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Already deleted user: " + userId
            );
        }

        user.delete();
        saveUserPort.save(user);

        PointWallet wallet = walletRepository.getByUserId(userId);
        wallet.deactivate();
        walletRepository.save(wallet);
    }
}
