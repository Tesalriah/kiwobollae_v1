package com.kiwobollae.api.board.repository;

import com.kiwobollae.api.board.entity.BoardPostLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostLikeRepository extends JpaRepository<BoardPostLike, Long> {

	boolean existsByPostIdAndUserId(Long postId, Long userId);

	Optional<BoardPostLike> findByPostIdAndUserId(Long postId, Long userId);
}
