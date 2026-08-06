package com.kiwobollae.api.board.service;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.entity.enums.UserRole;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.board.dto.request.BoardPostCreateRequest;
import com.kiwobollae.api.board.dto.response.BoardPostResponse;
import com.kiwobollae.api.board.entity.BoardPost;
import com.kiwobollae.api.board.entity.enums.BoardCategory;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import com.kiwobollae.api.board.repository.BoardPostRepository;
import com.kiwobollae.api.content.repository.PlantJournalRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardPostService {

	private final BoardPostRepository boardPostRepository;
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

	public Page<BoardPostResponse> getPosts(BoardCategory category, Pageable pageable) {
		return boardPostRepository.search(BoardStatus.ACTIVE, category, pageable).map(BoardPostResponse::from);
	}

	public BoardPostResponse getPost(Long id) {
		return BoardPostResponse.from(findActivePost(id));
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
