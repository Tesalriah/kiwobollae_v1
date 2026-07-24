package com.kiwobollae.api.auth.service;

import com.kiwobollae.api.auth.dto.request.LoginRequest;
import com.kiwobollae.api.auth.dto.request.SignupRequest;
import com.kiwobollae.api.auth.dto.response.AccessReissueResult;
import com.kiwobollae.api.auth.dto.response.TokenIssueResult;
import com.kiwobollae.api.auth.dto.response.UserResponse;
import com.kiwobollae.api.auth.entity.RefreshToken;
import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.entity.enums.AuthProvider;
import com.kiwobollae.api.auth.entity.enums.UserRole;
import com.kiwobollae.api.auth.entity.enums.UserStatus;
import com.kiwobollae.api.auth.repository.RefreshTokenRepository;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import com.kiwobollae.api.global.security.JwtTokenProvider;
import com.kiwobollae.api.global.security.TokenHasher;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final TokenHasher tokenHasher;

	@Transactional
	public UserResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
		}
		if (userRepository.existsByNickname(request.nickname())) {
			throw new BusinessException(ErrorCode.AUTH_NICKNAME_ALREADY_EXISTS);
		}

		User user = User.builder()
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.nickname(request.nickname())
				.name(request.name())
				.phoneNumber(request.phoneNumber())
				.provider(AuthProvider.LOCAL)
				.role(UserRole.USER)
				.level(1)
				.status(UserStatus.ACTIVE)
				.build();

		return UserResponse.from(userRepository.save(user));
	}

	@Transactional
	public TokenIssueResult login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

		if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
		}
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.AUTH_ACCOUNT_NOT_ACTIVE);
		}

		return issueTokens(user);
	}

	public AccessReissueResult reissue(String rawRefreshToken) {
		if (rawRefreshToken == null || !jwtTokenProvider.validateToken(rawRefreshToken)) {
			throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
		}

		String tokenHash = tokenHasher.hash(rawRefreshToken);
		RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_TOKEN_INVALID));

		if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.AUTH_TOKEN_EXPIRED);
		}

		// No rotation: the same refresh token (and its cookie) stays valid and gets
		// reused until it expires on its own or the user logs out.
		User user = stored.getUser();
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.AUTH_ACCOUNT_NOT_ACTIVE);
		}

		String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
		return new AccessReissueResult(accessToken, "Bearer", UserResponse.from(user));
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		if (rawRefreshToken == null) {
			return;
		}
		String tokenHash = tokenHasher.hash(rawRefreshToken);
		refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
				.ifPresent(stored -> stored.revoke(LocalDateTime.now()));
	}

	private TokenIssueResult issueTokens(User user) {
		String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
		String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

		RefreshToken entity = RefreshToken.builder()
				.user(user)
				.tokenHash(tokenHasher.hash(refreshToken))
				.expiresAt(LocalDateTime.now().plusNanos(jwtTokenProvider.getRefreshExpirationMs() * 1_000_000L))
				.createdAt(LocalDateTime.now())
				.build();
		refreshTokenRepository.save(entity);

		return new TokenIssueResult(accessToken, "Bearer", UserResponse.from(user), refreshToken);
	}
}
