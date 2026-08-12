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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
		BoardComment parent = request.parentCommentId() != null
				? findActiveCommentInPost(request.parentCommentId(), postId)
				: null;
		User user = userRepository.getReferenceById(userId);
		BoardComment comment = boardCommentRepository.save(BoardComment.create(post, user, request.content(), parent));
		post.incrementCommentCount();
		return BoardCommentResponse.from(comment);
	}

	// 다른 도메인(report)이 댓글 존재 여부만 확인할 때 쓰는 조회 전용 진입점.
	public boolean existsActive(Long id) {
		return boardCommentRepository.findByIdWithUser(id)
				.map(comment -> comment.getStatus() == BoardStatus.ACTIVE)
				.orElse(false);
	}

	public List<BoardCommentResponse> getComments(Long postId, Long userId) {
		findActivePost(postId);
		List<BoardComment> comments = boardCommentRepository.findAllByPostIdAndStatus(postId, BoardStatus.ACTIVE);
		if (userId == null || comments.isEmpty()) {
			return comments.stream().map(BoardCommentResponse::from).toList();
		}
		List<Long> commentIds = comments.stream().map(BoardComment::getId).toList();
		Set<Long> likedCommentIds = Set.copyOf(boardCommentLikeRepository.findLikedCommentIds(userId, commentIds));
		return comments.stream()
				.map(comment -> BoardCommentResponse.from(comment, likedCommentIds.contains(comment.getId())))
				.toList();
	}

	public Page<BoardCommentResponse> getMyComments(Long userId, Pageable pageable) {
		return boardCommentRepository.findAllByUserId(userId, pageable).map(BoardCommentResponse::from);
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
	public void adminHideComment(Long commentId) {
		BoardComment comment = findActiveComment(commentId);
		comment.hide(BoardHiddenBy.ADMIN, LocalDateTime.now(KST));
		comment.getPost().decrementCommentCount();
	}

	@Transactional
	public void likeComment(Long userId, Long commentId) {
		BoardComment comment = findActiveComment(commentId);
		if (boardCommentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
			throw new BusinessException(ErrorCode.BOARD_ALREADY_LIKED);
		}
		User user = userRepository.getReferenceById(userId);
		try {
			// existsBy 사전 체크와 저장 사이의 동시성 경쟁으로 유니크 제약이 위반돼도 원시 DB 에러
			// 대신 "이미 좋아요를 눌렀다"는 안내로 보이게 한다.
			boardCommentLikeRepository.saveAndFlush(BoardCommentLike.create(comment, user, LocalDateTime.now(KST)));
		} catch (DataIntegrityViolationException e) {
			throw new BusinessException(ErrorCode.BOARD_ALREADY_LIKED);
		}
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

	// 답글의 부모 댓글은 같은 게시글에 속한 활성 댓글이어야 한다. 다른 게시글의 댓글을 부모로
	// 지정하거나, 숨겨진/존재하지 않는 댓글을 지정하면 전부 "댓글을 찾을 수 없음"으로 취급한다.
	private BoardComment findActiveCommentInPost(Long commentId, Long postId) {
		BoardComment parent = findActiveComment(commentId);
		if (!parent.getPost().getId().equals(postId)) {
			throw new BusinessException(ErrorCode.BOARD_COMMENT_NOT_FOUND);
		}
		return parent;
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
