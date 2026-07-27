package com.kiwobollae.api.auth.entity;

import com.kiwobollae.api.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A single "we sent this code to this email" attempt. Only the hash of the raw
 * 6-digit code is stored (mirrors RefreshToken's token_hash approach) so a DB
 * leak alone can't be used to complete someone else's signup.
 */
@Getter
@Entity
@Table(name = "email_verification", indexes = {
		@Index(name = "idx_email_verification_email_created_at", columnList = "email, created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailVerification extends BaseEntity {

	@Column(nullable = false, length = 255)
	private String email;

	@Column(name = "code_hash", nullable = false, length = 64)
	private String codeHash;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@Builder.Default
	@Column(nullable = false)
	private Integer attempts = 0;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public boolean isExpired() {
		return expiresAt.isBefore(LocalDateTime.now());
	}

	public boolean isVerified() {
		return verifiedAt != null;
	}

	public void markVerified() {
		this.verifiedAt = LocalDateTime.now();
	}

	public void recordFailedAttempt() {
		this.attempts += 1;
	}
}
