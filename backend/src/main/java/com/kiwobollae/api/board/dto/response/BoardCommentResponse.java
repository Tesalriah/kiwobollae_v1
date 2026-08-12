package com.kiwobollae.api.board.dto.response;

import com.kiwobollae.api.board.entity.BoardComment;
import java.time.LocalDateTime;

public record BoardCommentResponse(
		Long id,
		Long postId,
		Long userId,
		String nickname,
		String content,
		Long parentCommentId,
		Integer likeCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		Boolean likedByMe
) {
	public static BoardCommentResponse from(BoardComment comment) {
		return from(comment, false);
	}

	public static BoardCommentResponse from(BoardComment comment, boolean likedByMe) {
		return new BoardCommentResponse(
				comment.getId(),
				comment.getPost().getId(),
				comment.getUser().getId(),
				comment.getUser().getNickname(),
				comment.getContent(),
				comment.getParentComment() != null ? comment.getParentComment().getId() : null,
				comment.getLikeCount(),
				comment.getCreatedAt(),
				comment.getUpdatedAt(),
				likedByMe
		);
	}
}
