package com.kiwobollae.api.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.entity.enums.UserRole;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.board.dto.request.BoardPostCreateRequest;
import com.kiwobollae.api.board.dto.response.BoardPostResponse;
import com.kiwobollae.api.board.entity.BoardPost;
import com.kiwobollae.api.board.entity.enums.BoardCategory;
import com.kiwobollae.api.board.entity.enums.BoardStatus;
import com.kiwobollae.api.board.repository.BoardPostRepository;
import com.kiwobollae.api.content.entity.PlantJournal;
import com.kiwobollae.api.content.repository.PlantJournalRepository;
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
class BoardPostServiceTest {

	@Mock private BoardPostRepository boardPostRepository;
	@Mock private UserRepository userRepository;
	@Mock private PlantJournalRepository plantJournalRepository;
	@InjectMocks private BoardPostService boardPostService;

	private User mockUser(Long id, UserRole role) {
		User user = mock(User.class);
		lenient().when(user.getId()).thenReturn(id);
		lenient().when(user.getRole()).thenReturn(role);
		lenient().when(user.getNickname()).thenReturn("초록이");
		return user;
	}

	private BoardPost mockPost(Long id, User user, BoardStatus status) {
		BoardPost post = mock(BoardPost.class);
		lenient().when(post.getId()).thenReturn(id);
		lenient().when(post.getUser()).thenReturn(user);
		lenient().when(post.getCategory()).thenReturn(BoardCategory.FREE);
		lenient().when(post.getTitle()).thenReturn("제목");
		lenient().when(post.getContent()).thenReturn("내용");
		lenient().when(post.getViewCount()).thenReturn(0);
		lenient().when(post.getLikeCount()).thenReturn(0);
		lenient().when(post.getCommentCount()).thenReturn(0);
		lenient().when(post.getStatus()).thenReturn(status);
		return post;
	}

	@Test
	void createPostSucceedsForFreeCategory() {
		User user = mockUser(1L, UserRole.USER);
		given(userRepository.getReferenceById(1L)).willReturn(user);
		given(boardPostRepository.save(any(BoardPost.class))).willReturn(mockPost(10L, user, BoardStatus.ACTIVE));

		BoardPostResponse response = boardPostService.createPost(
				1L, new BoardPostCreateRequest(BoardCategory.FREE, "제목", "내용", null)
		);

		assertThat(response.id()).isEqualTo(10L);
		verify(plantJournalRepository, never()).findOwnedActive(anyLong(), anyLong());
	}

	@Test
	void createPostFailsWhenNonAdminWritesNotice() {
		User user = mockUser(1L, UserRole.USER);
		given(userRepository.getReferenceById(1L)).willReturn(user);

		assertThatThrownBy(() -> boardPostService.createPost(
				1L, new BoardPostCreateRequest(BoardCategory.NOTICE, "제목", "내용", null)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_NOTICE_FORBIDDEN);
	}

	@Test
	void createPostSucceedsWhenAdminWritesNotice() {
		User admin = mockUser(1L, UserRole.ADMIN);
		given(userRepository.getReferenceById(1L)).willReturn(admin);
		given(boardPostRepository.save(any(BoardPost.class))).willReturn(mockPost(10L, admin, BoardStatus.ACTIVE));

		BoardPostResponse response = boardPostService.createPost(
				1L, new BoardPostCreateRequest(BoardCategory.NOTICE, "공지", "내용", null)
		);

		assertThat(response.id()).isEqualTo(10L);
	}

	@Test
	void createPostFailsWhenPlantQnaHasNoJournalId() {
		User user = mockUser(1L, UserRole.USER);
		given(userRepository.getReferenceById(1L)).willReturn(user);

		assertThatThrownBy(() -> boardPostService.createPost(
				1L, new BoardPostCreateRequest(BoardCategory.PLANT_QNA, "제목", "내용", null)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_JOURNAL_REQUIRED);
	}

	@Test
	void createPostFailsWhenJournalNotOwnedByRequester() {
		User user = mockUser(1L, UserRole.USER);
		given(userRepository.getReferenceById(1L)).willReturn(user);
		given(plantJournalRepository.findOwnedActive(99L, 1L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> boardPostService.createPost(
				1L, new BoardPostCreateRequest(BoardCategory.PLANT_QNA, "제목", "내용", 99L)
		)).isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_JOURNAL_NOT_OWNED);
	}

	@Test
	void createPostSucceedsWhenJournalOwnedByRequester() {
		User user = mockUser(1L, UserRole.USER);
		given(userRepository.getReferenceById(1L)).willReturn(user);
		given(plantJournalRepository.findOwnedActive(99L, 1L)).willReturn(Optional.of(mock(PlantJournal.class)));
		given(boardPostRepository.save(any(BoardPost.class))).willReturn(mockPost(10L, user, BoardStatus.ACTIVE));

		BoardPostResponse response = boardPostService.createPost(
				1L, new BoardPostCreateRequest(BoardCategory.PLANT_QNA, "제목", "내용", 99L)
		);

		assertThat(response.id()).isEqualTo(10L);
	}

	@Test
	void getPostsMapsRepositoryPage() {
		Pageable pageable = PageRequest.of(0, 10);
		User user = mockUser(1L, UserRole.USER);
		Page<BoardPost> page = new PageImpl<>(List.of(mockPost(10L, user, BoardStatus.ACTIVE)));
		given(boardPostRepository.search(BoardStatus.ACTIVE, BoardCategory.FREE, pageable)).willReturn(page);

		Page<BoardPostResponse> result = boardPostService.getPosts(BoardCategory.FREE, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).id()).isEqualTo(10L);
	}

	@Test
	void getPostReturnsActivePost() {
		User user = mockUser(1L, UserRole.USER);
		given(boardPostRepository.findByIdWithUser(10L))
				.willReturn(Optional.of(mockPost(10L, user, BoardStatus.ACTIVE)));

		BoardPostResponse response = boardPostService.getPost(10L);

		assertThat(response.id()).isEqualTo(10L);
	}

	@Test
	void getPostFailsWhenHidden() {
		User user = mockUser(1L, UserRole.USER);
		given(boardPostRepository.findByIdWithUser(10L))
				.willReturn(Optional.of(mockPost(10L, user, BoardStatus.HIDDEN)));

		assertThatThrownBy(() -> boardPostService.getPost(10L))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_POST_NOT_FOUND);
	}

	@Test
	void getPostFailsWhenNotFound() {
		given(boardPostRepository.findByIdWithUser(404L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> boardPostService.getPost(404L))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getErrorCode())
				.isEqualTo(ErrorCode.BOARD_POST_NOT_FOUND);
	}
}
