package com.kiwobollae.api.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BoardCommentServiceTest {

	@Mock private BoardCommentRepository boardCommentRepository;
	@Mock private BoardCommentLikeRepository boardCommentLikeRepository;
	@Mock private BoardPostRepository boardPostRepository;
	@Mock private UserRepository userRepository;
	@InjectMocks private BoardCommentService boardCommentService;

	private User mockUser(Long id) {
		User user = mock(User.class);
		lenient().when(user.getId()).thenReturn(id);
		lenient().when(user.getNickname()).thenReturn("초록이");
		return user;
	}

	private BoardPost mockPost(Long id, BoardStatus status) {
		BoardPost post = mock(BoardPost.class);
		lenient().when(post.getId()).thenReturn(id);
		lenient().when(post.getStatus()).thenReturn(status);
		return post;
	}

	private BoardComment mockComment(Long id, BoardPost post, User user, String content, BoardStatus status) {
		BoardComment comment = mock(BoardComment.class);
		lenient().when(comment.getId()).thenReturn(id);
		lenient().when(comment.getPost()).thenReturn(post);
		lenient().when(comment.getUser()).thenReturn(user);
		lenient().when(comment.getContent()).thenReturn(content);
		lenient().when(comment.getStatus()).thenReturn(status);
		return comment;
	}

	@Test
	void createCommentSucceedsForActivePost() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment saved = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));
		given(userRepository.getReferenceById(1L)).willReturn(user);
		given(boardCommentRepository.save(any(BoardComment.class))).willReturn(saved);

		BoardCommentResponse response =
				boardCommentService.createComment(1L, 10L, new BoardCommentCreateRequest("댓글 내용", null));

		assertThat(response.id()).isEqualTo(100L);
		verify(post).incrementCommentCount();
	}

	@Test
	void createCommentFailsWhenPostNotFound() {
		given(boardPostRepository.findById(404L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> boardCommentService.createComment(
				1L, 404L, new BoardCommentCreateRequest("댓글 내용", null)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_POST_NOT_FOUND);
	}

	@Test
	void createCommentFailsWhenPostHidden() {
		BoardPost post = mockPost(10L, BoardStatus.HIDDEN);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));

		assertThatThrownBy(() -> boardCommentService.createComment(
				1L, 10L, new BoardCommentCreateRequest("댓글 내용", null)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_POST_NOT_FOUND);
	}

	@Test
	void createReplySucceedsWhenParentIsActiveAndInSamePost() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment parent = mockComment(100L, post, user, "부모 댓글", BoardStatus.ACTIVE);
		BoardComment reply = mockComment(101L, post, user, "답글 내용", BoardStatus.ACTIVE);
		lenient().when(reply.getParentComment()).thenReturn(parent);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(parent));
		given(userRepository.getReferenceById(1L)).willReturn(user);
		given(boardCommentRepository.save(any(BoardComment.class))).willReturn(reply);

		BoardCommentResponse response =
				boardCommentService.createComment(1L, 10L, new BoardCommentCreateRequest("답글 내용", 100L));

		assertThat(response.id()).isEqualTo(101L);
		assertThat(response.parentCommentId()).isEqualTo(100L);
	}

	@Test
	void createReplyFailsWhenParentNotFound() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));
		given(boardCommentRepository.findByIdWithUser(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> boardCommentService.createComment(
				1L, 10L, new BoardCommentCreateRequest("답글 내용", 999L)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_COMMENT_NOT_FOUND);
	}

	@Test
	void createReplyFailsWhenParentBelongsToDifferentPost() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		BoardPost otherPost = mockPost(20L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment parentInOtherPost = mockComment(100L, otherPost, user, "다른 글의 댓글", BoardStatus.ACTIVE);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(parentInOtherPost));

		assertThatThrownBy(() -> boardCommentService.createComment(
				1L, 10L, new BoardCommentCreateRequest("답글 내용", 100L)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_COMMENT_NOT_FOUND);
	}

	@Test
	void createReplyFailsWhenParentHidden() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment hiddenParent = mockComment(100L, post, user, "삭제된 댓글", BoardStatus.HIDDEN);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(hiddenParent));

		assertThatThrownBy(() -> boardCommentService.createComment(
				1L, 10L, new BoardCommentCreateRequest("답글 내용", 100L)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_COMMENT_NOT_FOUND);
	}

	@Test
	void getCommentsReturnsActiveComments() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment comment = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));
		given(boardCommentRepository.findAllByPostIdAndStatus(10L, BoardStatus.ACTIVE))
				.willReturn(List.of(comment));

		List<BoardCommentResponse> responses = boardCommentService.getComments(10L, null);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).id()).isEqualTo(100L);
		assertThat(responses.get(0).likedByMe()).isFalse();
	}

	@Test
	void getCommentsMarksLikedByMeForCurrentUser() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment comment = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		given(boardPostRepository.findById(10L)).willReturn(Optional.of(post));
		given(boardCommentRepository.findAllByPostIdAndStatus(10L, BoardStatus.ACTIVE))
				.willReturn(List.of(comment));
		given(boardCommentLikeRepository.findLikedCommentIds(2L, List.of(100L))).willReturn(List.of(100L));

		List<BoardCommentResponse> responses = boardCommentService.getComments(10L, 2L);

		assertThat(responses.get(0).likedByMe()).isTrue();
	}

	@Test
	void deleteCommentHidesCommentAndDecrementsPostCount() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment comment = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(comment));

		boardCommentService.deleteComment(1L, 100L);

		verify(comment).hide(eq(BoardHiddenBy.AUTHOR), any());
		verify(post).decrementCommentCount();
	}

	@Test
	void deleteCommentFailsWhenNotOwner() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User owner = mockUser(1L);
		BoardComment comment = mockComment(100L, post, owner, "댓글 내용", BoardStatus.ACTIVE);
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(comment));

		assertThatThrownBy(() -> boardCommentService.deleteComment(2L, 100L))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_COMMENT_NOT_OWNED);
	}

	@Test
	void deleteCommentFailsWhenNotFound() {
		given(boardCommentRepository.findByIdWithUser(404L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> boardCommentService.deleteComment(1L, 404L))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_COMMENT_NOT_FOUND);
	}

	@Test
	void getMyCommentsMapsRepositoryPage() {
		Pageable pageable = PageRequest.of(0, 10);
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment comment = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		Page<BoardComment> page = new PageImpl<>(List.of(comment));
		given(boardCommentRepository.findAllByUserId(1L, pageable)).willReturn(page);

		Page<BoardCommentResponse> result = boardCommentService.getMyComments(1L, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).id()).isEqualTo(100L);
	}

	@Test
	void adminHideCommentHidesActiveCommentAndDecrementsPostCount() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment comment = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(comment));

		boardCommentService.adminHideComment(100L);

		verify(comment).hide(eq(BoardHiddenBy.ADMIN), any());
		verify(post).decrementCommentCount();
	}

	@Test
	void likeCommentSucceedsWhenNotAlreadyLiked() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment comment = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(comment));
		given(boardCommentLikeRepository.existsByCommentIdAndUserId(100L, 1L)).willReturn(false);
		given(userRepository.getReferenceById(1L)).willReturn(user);

		boardCommentService.likeComment(1L, 100L);

		verify(comment).incrementLikeCount();
	}

	@Test
	void likeCommentFailsWhenAlreadyLiked() {
		BoardPost post = mockPost(10L, BoardStatus.ACTIVE);
		User user = mockUser(1L);
		BoardComment comment = mockComment(100L, post, user, "댓글 내용", BoardStatus.ACTIVE);
		given(boardCommentRepository.findByIdWithUser(100L)).willReturn(Optional.of(comment));
		given(boardCommentLikeRepository.existsByCommentIdAndUserId(100L, 1L)).willReturn(true);

		assertThatThrownBy(() -> boardCommentService.likeComment(1L, 100L))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_ALREADY_LIKED);
	}

	@Test
	void unlikeCommentSucceedsWhenLiked() {
		BoardComment comment = mockComment(100L, mockPost(10L, BoardStatus.ACTIVE), mockUser(1L), "댓글 내용", BoardStatus.ACTIVE);
		BoardCommentLike like = mock(BoardCommentLike.class);
		given(boardCommentLikeRepository.findByCommentIdAndUserId(100L, 1L)).willReturn(Optional.of(like));
		given(boardCommentRepository.getReferenceById(100L)).willReturn(comment);

		boardCommentService.unlikeComment(1L, 100L);

		verify(boardCommentLikeRepository).delete(like);
		verify(comment).decrementLikeCount();
	}

	@Test
	void unlikeCommentFailsWhenNotLiked() {
		given(boardCommentLikeRepository.findByCommentIdAndUserId(100L, 1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> boardCommentService.unlikeComment(1L, 100L))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_LIKE_NOT_FOUND);
	}
}
