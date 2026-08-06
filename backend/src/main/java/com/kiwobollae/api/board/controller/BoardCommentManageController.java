package com.kiwobollae.api.board.controller;

import com.kiwobollae.api.board.service.BoardCommentService;
import com.kiwobollae.api.global.common.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** postId 없이 comment id만으로 다루는 엔드포인트 — BoardCommentController와 경로 프리픽스가 달라 분리했다. */
@Tag(name = "게시판 댓글", description = "게시판 댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/board/comments")
public class BoardCommentManageController {

	private final BoardCommentService boardCommentService;

	@Operation(summary = "댓글 삭제", description = "작성자 본인만 삭제할 수 있습니다. 물리 삭제 없이 숨김 처리합니다.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
		boardCommentService.deleteComment(userId, id);
		return ResponseEntity.noContent().build();
	}
}
