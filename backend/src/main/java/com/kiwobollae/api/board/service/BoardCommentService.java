package com.kiwobollae.api.board.service;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.board.dto.request.BoardCommentCreateRequest;
import com.kiwobollae.api.board.dto.response.BoardCommentResponse;
import com.kiwobollae.api.board.entity.BoardComment;
import com.kiwobollae.api.board.entity.BoardCommentLike;
import com.kiwobollae.api.board.entity.BoardPost;
import com.kiwobollae.api.board.entity.enums.BoardHiddenBy;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import com.kiwobollae.api.board.repository.BoardCommentLikeRepository;
import com.kiwobollae.api.board.repository.BoardCommentRepository;
import com.kiwobollae.api.board.repository.BoardPostRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCommentService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final BoardCommentRepository boardCommentRepository;
	private final BoardCommentLikeRepository boardCommentLikeRepository;
	private final BoardPostRepository boardPostRepository;
	private final UserRepository userRepository;

	@Transactional
	public BoardCommentResponse createComment(Long userId, Long postId, BoardCommentCreateRequest request) {
		BoardPost post = findActivePost(postId);
		User user = userRepository.getReferenceById(userId);
		BoardComment comment = boardCommentRepository.save(BoardComment.create(post, user, request.content()));
		post.incrementCommentCount();
		return BoardCommentResponse.from(comment);
	}

	// 다른 도메인(report)이 댓글 존재 여부만 확인할 때 쓰는 조회 전용 진입점.
	public boolean existsActive(Long id) {
		return boardCommentRepository.findByIdWithUser(id)
				.map(comment -> comment.getStatus() == BoardStatus.ACTIVE)
				.orElse(false);
	}

	public List<BoardCommentResponse> getComments(Long postId) {
		findActivePost(postId);
		return boardCommentRepository.findAllByPostIdAndStatus(postId, BoardStatus.ACTIVE).stream()
				.map(BoardCommentResponse::from)
				.toList();
	}

	@Transactional
	public void deleteComment(Long userId, Long commentId) {
		BoardComment comment = findActiveComment(commentId);
		if (!comment.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.BOARD_COMMENT_NOT_OWNED);
		}
		comment.hide(BoardHiddenBy.AUTHOR, LocalDateTime.now(KST));
		comment.getPost().decrementCommentCount();
	}

	@Transactional
	public void likeComment(Long userId, Long commentId) {
		BoardComment comment = findActiveComment(commentId);
		if (boardCommentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
			throw new BusinessException(ErrorCode.BOARD_ALREADY_LIKED);
		}
		User user = userRepository.getReferenceById(userId);
		boardCommentLikeRepository.save(BoardCommentLike.create(comment, user, LocalDateTime.now(KST)));
		comment.incrementLikeCount();
	}

	@Transactional
	public void unlikeComment(Long userId, Long commentId) {
		BoardCommentLike like = boardCommentLikeRepository.findByCommentIdAndUserId(commentId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_LIKE_NOT_FOUND));
		boardCommentLikeRepository.delete(like);
		boardCommentRepository.getReferenceById(commentId).decrementLikeCount();
	}

	private BoardComment findActiveComment(Long commentId) {
		BoardComment comment = boardCommentRepository.findByIdWithUser(commentId)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_COMMENT_NOT_FOUND));
		if (comment.getStatus() != BoardStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.BOARD_COMMENT_NOT_FOUND);
		}
		return comment;
	}

	private BoardPost findActivePost(Long postId) {
		BoardPost post = boardPostRepository.findById(postId)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_POST_NOT_FOUND));
		if (post.getStatus() != BoardStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.BOARD_POST_NOT_FOUND);
		}
		return post;
	}
}
