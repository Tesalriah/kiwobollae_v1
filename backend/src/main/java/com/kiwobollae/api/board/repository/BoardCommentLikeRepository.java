package com.kiwobollae.api.board.repository;

import com.kiwobollae.api.board.entity.BoardCommentLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCommentLikeRepository extends JpaRepository<BoardCommentLike, Long> {

	boolean existsByCommentIdAndUserId(Long commentId, Long userId);

	Optional<BoardCommentLike> findByCommentIdAndUserId(Long commentId, Long userId);
}
