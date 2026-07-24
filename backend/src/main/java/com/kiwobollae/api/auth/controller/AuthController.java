package com.kiwobollae.api.auth.controller;

import com.kiwobollae.api.auth.dto.request.LoginRequest;
import com.kiwobollae.api.auth.dto.request.SignupRequest;
import com.kiwobollae.api.auth.dto.response.AccessReissueResult;
import com.kiwobollae.api.auth.dto.response.AccessTokenResponse;
import com.kiwobollae.api.auth.dto.response.LoginResponse;
import com.kiwobollae.api.auth.dto.response.TokenIssueResult;
import com.kiwobollae.api.auth.dto.response.UserResponse;
import com.kiwobollae.api.auth.service.AuthService;
import com.kiwobollae.api.global.common.ApiResponse;
import com.kiwobollae.api.global.common.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입, 로그인 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/auth")
public class AuthController {

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
	private static final String AUTH_PATH = ApiVersion.V1 + "/auth";

	private final AuthService authService;

	@Value("${app.cookie.secure}")
	private boolean cookieSecure;

	@Value("${jwt.refresh-expiration}")
	private long refreshExpirationMs;

	@Operation(summary = "회원가입", description = "이메일/비밀번호로 새 계정을 생성합니다.")
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authService.signup(request)));
	}

	@Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 액세스 토큰을 발급받습니다. 리프레시 토큰은 httpOnly 쿠키로 내려갑니다.")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		TokenIssueResult result = authService.login(request);
		return withRefreshCookie(result);
	}

	@Operation(summary = "토큰 재발급", description = "httpOnly 쿠키의 리프레시 토큰을 검증하고 액세스 토큰만 새로 발급합니다. 리프레시 토큰 자체는 로그인 시 발급된 것을 만료 전까지 그대로 재사용합니다.")
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<AccessTokenResponse>> reissue(
			@CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
		AccessReissueResult result = authService.reissue(refreshToken);
		AccessTokenResponse body = new AccessTokenResponse(result.accessToken(), result.tokenType(), result.user());
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@Operation(summary = "로그아웃", description = "리프레시 토큰을 폐기하고 쿠키를 만료시킵니다.")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			@CookieValue(value = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
		authService.logout(refreshToken);
		ResponseCookie expired = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
				.httpOnly(true)
				.secure(cookieSecure)
				.sameSite("Lax")
				.path(AUTH_PATH)
				.maxAge(0)
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, expired.toString())
				.body(ApiResponse.<Void>success(null));
	}

	private ResponseEntity<ApiResponse<LoginResponse>> withRefreshCookie(TokenIssueResult result) {
		LoginResponse body = new LoginResponse(result.accessToken(), result.tokenType(), result.user());
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.rawRefreshToken()).toString())
				.body(ApiResponse.success(body));
	}

	private ResponseCookie buildRefreshCookie(String rawRefreshToken) {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE, rawRefreshToken)
				.httpOnly(true)
				.secure(cookieSecure)
				.sameSite("Lax")
				.path(AUTH_PATH)
				.maxAge(Duration.ofMillis(refreshExpirationMs))
				.build();
	}
}
