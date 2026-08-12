package com.kiwobollae.api.board.dto.response;

import com.kiwobollae.api.board.entity.BoardPost;
import com.kiwobollae.api.board.entity.enums.BoardCategory;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import java.time.LocalDateTime;

public record BoardPostResponse(
		Long id,
		Long userId,
		String nickname,
		BoardCategory category,
		String title,
		String content,
		Long journalId,
		Integer viewCount,
		Integer likeCount,
		Integer commentCount,
		BoardStatus status,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		Boolean likedByMe
) {
	public static BoardPostResponse from(BoardPost post) {
		return from(post, false);
	}

	public static BoardPostResponse from(BoardPost post, boolean likedByMe) {
		return new BoardPostResponse(
				post.getId(),
				post.getUser().getId(),
				post.getUser().getNickname(),
				post.getCategory(),
				post.getTitle(),
				post.getContent(),
				post.getJournalId(),
				post.getViewCount(),
				post.getLikeCount(),
				post.getCommentCount(),
				post.getStatus(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				likedByMe
		);
	}
}
