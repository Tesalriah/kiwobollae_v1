package com.kiwobollae.api.mypage.repository;

import com.kiwobollae.api.mypage.entity.UserAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

	List<UserAddress> findAllByUser_IdOrderByIsDefaultDescCreatedAtDesc(Long userId);

	Optional<UserAddress> findByIdAndUser_Id(Long id, Long userId);

	// 새 기본 배송지를 지정하기 전 호출 — 사용자당 기본 배송지가 항상 최대 1개만 남도록
	// 기존 기본 배송지를 먼저 해제한다.
	@Modifying
	@Query("update UserAddress a set a.isDefault = false where a.user.id = :userId and a.isDefault = true")
	void clearDefault(@Param("userId") Long userId);
}
