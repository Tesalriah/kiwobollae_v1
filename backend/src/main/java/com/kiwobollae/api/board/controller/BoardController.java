package com.kiwobollae.api.board.controller;

import com.kiwobollae.api.board.dto.request.BoardPostCreateRequest;
import com.kiwobollae.api.board.dto.request.BoardPostUpdateRequest;
import com.kiwobollae.api.board.dto.response.BoardPostResponse;
import com.kiwobollae.api.board.entity.enums.BoardCategory;
import com.kiwobollae.api.board.service.BoardPostService;
import com.kiwobollae.api.global.common.ApiResponse;
import com.kiwobollae.api.global.common.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시판", description = "커뮤니티 게시판(공지사항/자유게시판/식물 Q&A) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/board/posts")
public class BoardController {

	private final BoardPostService boardPostService;

	@Operation(
			summary = "게시글 작성",
			description = "카테고리, 제목, 본문으로 게시글을 생성합니다. NOTICE는 관리자만, PLANT_QNA는 본인 소유 일지 연동이 필수입니다."
	)
	@PostMapping
	public ResponseEntity<ApiResponse<BoardPostResponse>> createPost(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody BoardPostCreateRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(boardPostService.createPost(userId, request)));
	}

	@Operation(summary = "게시글 목록 조회", description = "카테고리 필터, 페이지네이션, 정렬(최신 기본)을 지원합니다.")
	@GetMapping
	public ResponseEntity<ApiResponse<Page<BoardPostResponse>>> getPosts(
			@RequestParam(required = false) BoardCategory category,
			@ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable
	) {
		return ResponseEntity.ok(ApiResponse.success(boardPostService.getPosts(category, pageable)));
	}

	@Operation(summary = "게시글 상세 조회", description = "게시글 본문과 작성자 정보를 반환합니다.")
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BoardPostResponse>> getPost(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success(boardPostService.getPost(id)));
	}

	@Operation(summary = "게시글 수정", description = "작성자 본인만 제목/본문을 수정할 수 있습니다. 카테고리는 변경할 수 없습니다.")
	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<BoardPostResponse>> updatePost(
			@AuthenticationPrincipal Long userId,
			@PathVariable Long id,
			@Valid @RequestBody BoardPostUpdateRequest request
	) {
		return ResponseEntity.ok(ApiResponse.success(boardPostService.updatePost(userId, id, request)));
	}

	@Operation(summary = "게시글 삭제", description = "작성자 본인만 삭제할 수 있습니다. 물리 삭제 없이 숨김 처리합니다.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePost(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
		boardPostService.deletePost(userId, id);
		return ResponseEntity.noContent().build();
	}
}
