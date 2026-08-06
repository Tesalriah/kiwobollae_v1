package com.kiwobollae.api.board.entity;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.board.entity.enums.BoardCategory;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import com.kiwobollae.api.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "board_posts", indexes = {
		@Index(name = "idx_board_posts_category_created_at", columnList = "category, created_at"),
		@Index(name = "idx_board_posts_user_id", columnList = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardPost extends BaseTimeEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BoardCategory category;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, length = 2000)
	private String content;

	// PLANT_QNA에서만 사용. 논리 참조(FK 없음) — 일지가 나중에 삭제/비공개돼도 게시글은 남는다.
	@Column(name = "journal_id")
	private Long journalId;

	@Column(name = "view_count", nullable = false)
	private Integer viewCount;

	@Column(name = "like_count", nullable = false)
	private Integer likeCount;

	@Column(name = "comment_count", nullable = false)
	private Integer commentCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private BoardStatus status;

	public static BoardPost create(User user, BoardCategory category, String title, String content, Long journalId) {
		return BoardPost.builder()
				.user(user)
				.category(category)
				.title(title)
				.content(content)
				.journalId(journalId)
				.viewCount(0)
				.likeCount(0)
				.commentCount(0)
				.status(BoardStatus.ACTIVE)
				.build();
	}
}
