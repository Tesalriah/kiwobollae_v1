package com.kiwobollae.api.board.repository;

import com.kiwobollae.api.board.entity.BoardComment;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

	@Query("select c from BoardComment c join fetch c.user "
			+ "where c.post.id = :postId and c.status = :status order by c.createdAt asc")
	List<BoardComment> findAllByPostIdAndStatus(@Param("postId") Long postId, @Param("status") BoardStatus status);

	@Query("select c from BoardComment c join fetch c.user where c.id = :id")
	Optional<BoardComment> findByIdWithUser(@Param("id") Long id);

	// 마이페이지 "내가 쓴 댓글" — 본인 글이라 상태(ACTIVE/HIDDEN) 필터 없이 전부 보여준다.
	@Query(value = "select c from BoardComment c join fetch c.user join fetch c.post where c.user.id = :userId",
			countQuery = "select count(c) from BoardComment c where c.user.id = :userId")
	Page<BoardComment> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
