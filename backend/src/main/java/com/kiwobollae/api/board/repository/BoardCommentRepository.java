package com.kiwobollae.api.board.repository;

import com.kiwobollae.api.board.entity.BoardComment;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

	@Query("select c from BoardComment c join fetch c.user "
			+ "where c.post.id = :postId and c.status = :status order by c.createdAt asc")
	List<BoardComment> findAllByPostIdAndStatus(@Param("postId") Long postId, @Param("status") BoardStatus status);

	@Query("select c from BoardComment c join fetch c.user where c.id = :id")
	Optional<BoardComment> findByIdWithUser(@Param("id") Long id);
}
