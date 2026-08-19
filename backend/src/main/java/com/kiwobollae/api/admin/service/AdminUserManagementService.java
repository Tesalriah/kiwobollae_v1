package com.kiwobollae.api.admin.service;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.entity.enums.UserRole;
import com.kiwobollae.api.auth.entity.enums.UserStatus;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

	private final UserRepository userRepository;

	@Transactional
	public void suspendUser(Long userId, String reason) {
		User user = findUser(userId);
		if (user.getRole() == UserRole.ADMIN) {
			throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "관리자 계정은 정지할 수 없습니다.");
		}
		if (user.getStatus() == UserStatus.WITHDRAWN) {
			throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "탈퇴한 계정은 정지할 수 없습니다.");
		}
		user.suspend(reason);
	}

	@Transactional
	public void reactivateUser(Long userId) {
		User user = findUser(userId);
		if (user.getStatus() != UserStatus.SUSPENDED) {
			throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "정지 상태인 계정만 정지 해제할 수 있습니다.");
		}
		user.reactivate();
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND));
	}
}
