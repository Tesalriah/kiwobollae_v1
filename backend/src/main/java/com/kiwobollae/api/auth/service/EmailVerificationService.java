package com.kiwobollae.api.auth.service;

import com.kiwobollae.api.auth.entity.EmailVerification;
import com.kiwobollae.api.auth.entity.enums.EmailVerificationPurpose;
import com.kiwobollae.api.auth.repository.EmailVerificationRepository;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import com.kiwobollae.api.global.security.TokenHasher;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

	private static final int CODE_LENGTH = 6;
	private static final int CODE_EXPIRATION_MINUTES = 5;
	private static final int MAX_ATTEMPTS = 5;
	// How long a verified-but-not-yet-signed-up email stays good for finishing signup.
	private static final int VERIFIED_VALIDITY_MINUTES = 30;

	private final EmailVerificationRepository emailVerificationRepository;
	private final UserRepository userRepository;
	private final EmailSender emailSender;
	private final TokenHasher tokenHasher;
	private final SecureRandom random = new SecureRandom();

	@Transactional
	public void requestCode(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
		}
		issueCode(email, EmailVerificationPurpose.SIGNUP);
	}

	@Transactional
	public void requestPasswordResetCode(String email) {
		if (!userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.AUTH_EMAIL_NOT_FOUND);
		}
		issueCode(email, EmailVerificationPurpose.PASSWORD_RESET);
	}

	@Transactional
	public void confirmCode(String email, String code) {
		confirm(email, code, EmailVerificationPurpose.SIGNUP);
	}

	@Transactional
	public void confirmPasswordResetCode(String email, String code) {
		confirm(email, code, EmailVerificationPurpose.PASSWORD_RESET);
	}

	/**
	 * Called from signup — throws if this email hasn't been verified recently enough.
	 */
	public void assertVerified(String email) {
		assertVerified(email, EmailVerificationPurpose.SIGNUP);
	}

	/**
	 * Called from password reset — throws if this email hasn't been verified recently enough.
	 */
	public void assertPasswordResetVerified(String email) {
		assertVerified(email, EmailVerificationPurpose.PASSWORD_RESET);
	}

	private void assertVerified(String email, EmailVerificationPurpose purpose) {
		EmailVerification verification = emailVerificationRepository
				.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_EMAIL_NOT_VERIFIED));

		boolean verifiedRecently = verification.isVerified()
				&& verification.getVerifiedAt().isAfter(LocalDateTime.now().minusMinutes(VERIFIED_VALIDITY_MINUTES));
		if (!verifiedRecently) {
			throw new BusinessException(ErrorCode.AUTH_EMAIL_NOT_VERIFIED);
		}
	}

	private void issueCode(String email, EmailVerificationPurpose purpose) {
		String code = generateCode();
		EmailVerification verification = EmailVerification.builder()
				.email(email)
				.purpose(purpose)
				.codeHash(tokenHasher.hash(code))
				.expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES))
				.createdAt(LocalDateTime.now())
				.build();
		emailVerificationRepository.save(verification);

		if (purpose == EmailVerificationPurpose.SIGNUP) {
			emailSender.send(
					email,
					"[키워볼래] 이메일 인증코드",
					VerificationEmailTemplate.renderSignup(code, CODE_EXPIRATION_MINUTES));
		} else {
			emailSender.send(
					email,
					"[키워볼래] 비밀번호 재설정 인증코드",
					VerificationEmailTemplate.renderPasswordReset(code, CODE_EXPIRATION_MINUTES));
		}
	}

	private void confirm(String email, String code, EmailVerificationPurpose purpose) {
		EmailVerification verification = emailVerificationRepository
				.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
				.orElseThrow(() -> new BusinessException(ErrorCode.AUTH_VERIFICATION_CODE_INVALID));

		if (verification.isVerified()) {
			return;
		}
		if (verification.isExpired()) {
			throw new BusinessException(ErrorCode.AUTH_VERIFICATION_CODE_EXPIRED);
		}
		if (verification.getAttempts() >= MAX_ATTEMPTS) {
			throw new BusinessException(ErrorCode.AUTH_VERIFICATION_TOO_MANY_ATTEMPTS);
		}
		if (!verification.getCodeHash().equals(tokenHasher.hash(code))) {
			verification.recordFailedAttempt();
			throw new BusinessException(ErrorCode.AUTH_VERIFICATION_CODE_INVALID);
		}

		verification.markVerified();
	}

	private String generateCode() {
		int bound = (int) Math.pow(10, CODE_LENGTH);
		int value = random.nextInt(bound);
		return String.format("%0" + CODE_LENGTH + "d", value);
	}
}
