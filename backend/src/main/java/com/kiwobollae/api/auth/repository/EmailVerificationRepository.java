package com.kiwobollae.api.auth.repository;

import com.kiwobollae.api.auth.entity.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

	Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
}
