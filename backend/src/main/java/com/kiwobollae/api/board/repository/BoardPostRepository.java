package com.kiwobollae.api.board.repository;

import com.kiwobollae.api.board.entity.BoardPost;
import com.kiwobollae.api.board.entity.enums.BoardCategory;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

	// join fetch로 User를 함께 가져와 목록 조회 시 건별 지연 로딩(N+1)을 막는다.
	@Query(value = "select p from BoardPost p join fetch p.user "
			+ "where p.status = :status and (:category is null or p.category = :category)",
			countQuery = "select count(p) from BoardPost p "
					+ "where p.status = :status and (:category is null or p.category = :category)")
	Page<BoardPost> search(
			@Param("status") BoardStatus status,
			@Param("category") BoardCategory category,
			Pageable pageable
	);

	@Query("select p from BoardPost p join fetch p.user where p.id = :id")
	Optional<BoardPost> findByIdWithUser(@Param("id") Long id);

	// 마이페이지 "내가 쓴 게시글" — 본인 글이라 상태(ACTIVE/HIDDEN) 필터 없이 전부 보여준다.
	@Query(value = "select p from BoardPost p join fetch p.user where p.user.id = :userId",
			countQuery = "select count(p) from BoardPost p where p.user.id = :userId")
	Page<BoardPost> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
