package com.kiwobollae.api.board.service;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.entity.enums.UserRole;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.board.dto.request.BoardPostCreateRequest;
import com.kiwobollae.api.board.dto.request.BoardPostUpdateRequest;
import com.kiwobollae.api.board.dto.response.BoardPostResponse;
import com.kiwobollae.api.board.entity.BoardPost;
import com.kiwobollae.api.board.entity.BoardPostLike;
import com.kiwobollae.api.board.entity.enums.BoardCategory;
import com.kiwobollae.api.board.entity.enums.BoardHiddenBy;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import com.kiwobollae.api.board.repository.BoardPostLikeRepository;
import com.kiwobollae.api.board.repository.BoardPostRepository;
import com.kiwobollae.api.content.repository.PlantJournalRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardPostService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final BoardPostRepository boardPostRepository;
	private final BoardPostLikeRepository boardPostLikeRepository;
	private final UserRepository userRepository;
	private final PlantJournalRepository plantJournalRepository;

	@Transactional
	public BoardPostResponse createPost(Long userId, BoardPostCreateRequest request) {
		// 인증된 요청이라 행 존재가 보장되므로 조회 없이 참조만 얻는다. role은 NOTICE 검증 시에만 지연 로딩된다.
		User user = userRepository.getReferenceById(userId);

		if (request.category() == BoardCategory.NOTICE && user.getRole() != UserRole.ADMIN) {
			throw new BusinessException(ErrorCode.BOARD_NOTICE_FORBIDDEN);
		}

		Long journalId = null;
		if (request.category() == BoardCategory.PLANT_QNA) {
			if (request.journalId() == null) {
				throw new BusinessException(ErrorCode.BOARD_JOURNAL_REQUIRED);
			}
			plantJournalRepository.findOwnedActive(request.journalId(), userId)
					.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_JOURNAL_NOT_OWNED));
			journalId = request.journalId();
		}

		BoardPost post = boardPostRepository.save(
				BoardPost.create(user, request.category(), request.title(), request.content(), journalId)
		);
		return BoardPostResponse.from(post);
	}

	public Page<BoardPostResponse> getPosts(BoardCategory category, Pageable pageable, Long userId) {
		Page<BoardPost> posts = boardPostRepository.search(BoardStatus.ACTIVE, category, pageable);
		if (userId == null || posts.isEmpty()) {
			return posts.map(BoardPostResponse::from);
		}
		List<Long> postIds = posts.map(BoardPost::getId).toList();
		Set<Long> likedPostIds = Set.copyOf(boardPostLikeRepository.findLikedPostIds(userId, postIds));
		return posts.map(post -> BoardPostResponse.from(post, likedPostIds.contains(post.getId())));
	}

	public BoardPostResponse getPost(Long id, Long userId) {
		BoardPost post = findActivePost(id);
		boolean likedByMe = userId != null && boardPostLikeRepository.existsByPostIdAndUserId(id, userId);
		return BoardPostResponse.from(post, likedByMe);
	}

	public Page<BoardPostResponse> getMyPosts(Long userId, Pageable pageable) {
		return boardPostRepository.findAllByUserId(userId, pageable).map(BoardPostResponse::from);
	}

	// 다른 도메인(report)이 게시글 존재 여부만 확인할 때 쓰는 조회 전용 진입점.
	public boolean existsActive(Long id) {
		return boardPostRepository.findById(id)
				.map(post -> post.getStatus() == BoardStatus.ACTIVE)
				.orElse(false);
	}

	@Transactional
	public BoardPostResponse updatePost(Long userId, Long id, BoardPostUpdateRequest request) {
		BoardPost post = findOwnedActivePost(userId, id);
		post.update(request.title(), request.content());
		return BoardPostResponse.from(post);
	}

	@Transactional
	public void deletePost(Long userId, Long id) {
		BoardPost post = findOwnedActivePost(userId, id);
		post.hide(BoardHiddenBy.AUTHOR, LocalDateTime.now(KST));
	}

	@Transactional
	public void adminHidePost(Long id) {
		BoardPost post = findActivePost(id);
		post.hide(BoardHiddenBy.ADMIN, LocalDateTime.now(KST));
	}

	@Transactional
	public void likePost(Long userId, Long id) {
		BoardPost post = findActivePost(id);
		if (boardPostLikeRepository.existsByPostIdAndUserId(id, userId)) {
			throw new BusinessException(ErrorCode.BOARD_ALREADY_LIKED);
		}
		User user = userRepository.getReferenceById(userId);
		boardPostLikeRepository.save(BoardPostLike.create(post, user, LocalDateTime.now(KST)));
		post.incrementLikeCount();
	}

	@Transactional
	public void unlikePost(Long userId, Long id) {
		BoardPostLike like = boardPostLikeRepository.findByPostIdAndUserId(id, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_LIKE_NOT_FOUND));
		boardPostLikeRepository.delete(like);
		boardPostRepository.getReferenceById(id).decrementLikeCount();
	}

	private BoardPost findOwnedActivePost(Long userId, Long id) {
		BoardPost post = findActivePost(id);
		if (!post.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.BOARD_POST_NOT_OWNED);
		}
		return post;
	}

	private BoardPost findActivePost(Long id) {
		BoardPost post = boardPostRepository.findByIdWithUser(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.BOARD_POST_NOT_FOUND));
		if (post.getStatus() != BoardStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.BOARD_POST_NOT_FOUND);
		}
		return post;
	}
}
