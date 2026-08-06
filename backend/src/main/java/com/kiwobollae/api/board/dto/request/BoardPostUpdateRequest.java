package com.kiwobollae.api.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardPostUpdateRequest(
		@NotBlank @Size(max = 100) String title,
		@NotBlank @Size(max = 2000) String content
) {
}
