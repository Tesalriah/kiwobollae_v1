package com.kiwobollae.api.board.service;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.board.dto.request.BoardCommentCreateRequest;
import com.kiwobollae.api.board.dto.response.BoardCommentResponse;
import com.kiwobollae.api.board.entity.BoardComment;
import com.kiwobollae.api.board.entity.BoardPost;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import com.kiwobollae.api.board.repository.BoardCommentRepository;
import com.kiwobollae.api.board.repository.BoardPostRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCommentService {

	private final BoardCommentRepository boardCommentRepository;
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

	public List<BoardCommentResponse> getComments(Long postId) {
		findActivePost(postId);
		return boardCommentRepository.findAllByPostIdAndStatus(postId, BoardStatus.ACTIVE).stream()
				.map(BoardCommentResponse::from)
				.toList();
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
