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

	long countByUser_Id(Long userId);

	// 대상 배송지만 기본으로 남기고 나머지는 전부 해제하는 원자적 단일 UPDATE.
	// "기존 기본 해제" + "새 기본 지정"을 별도의 두 단계로 나누면 두 요청이 동시에
	// 들어왔을 때 서로 다른 배송지를 각자 기본으로 지정한 채 커밋되어 기본 배송지가
	// 2개가 될 수 있다 — 하나의 UPDATE 문으로 묶어야 DB가 이 갱신을 원자적으로 처리한다.
	@Modifying
	@Query(value = "update user_address set is_default = (id = :targetId) "
			+ "where user_id = :userId and (is_default = true or id = :targetId)", nativeQuery = true)
	void setOnlyDefault(@Param("userId") Long userId, @Param("targetId") Long targetId);
}
