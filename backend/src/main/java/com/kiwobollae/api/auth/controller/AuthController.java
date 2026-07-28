package com.kiwobollae.api.auth.controller;

import com.kiwobollae.api.auth.dto.request.EmailVerificationConfirmRequest;
import com.kiwobollae.api.auth.dto.request.EmailVerificationRequest;
import com.kiwobollae.api.auth.dto.request.LoginRequest;
import com.kiwobollae.api.auth.dto.request.PasswordChangeRequest;
import com.kiwobollae.api.auth.dto.request.PasswordResetRequest;
import com.kiwobollae.api.auth.dto.request.PasswordVerifyRequest;
import com.kiwobollae.api.auth.dto.request.SignupRequest;
import com.kiwobollae.api.auth.dto.request.UserUpdateRequest;
import com.kiwobollae.api.auth.dto.request.WithdrawRequest;
import com.kiwobollae.api.auth.dto.response.AccessReissueResult;
import com.kiwobollae.api.auth.dto.response.AccessTokenResponse;
import com.kiwobollae.api.auth.dto.response.LoginResponse;
import com.kiwobollae.api.auth.dto.response.NicknameAvailabilityResponse;
import com.kiwobollae.api.auth.dto.response.TokenIssueResult;
import com.kiwobollae.api.auth.dto.response.UserResponse;
import com.kiwobollae.api.auth.service.AuthService;
import com.kiwobollae.api.auth.service.EmailVerificationService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입, 로그인 등 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/auth")
public class AuthController {

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
	private static final String AUTH_PATH = ApiVersion.V1 + "/auth";

	private final AuthService authService;
	private final EmailVerificationService emailVerificationService;

	@Value("${app.cookie.secure}")
	private boolean cookieSecure;

	@Value("${jwt.refresh-expiration}")
	private long refreshExpirationMs;

	@Operation(summary = "회원가입 이메일 인증코드 발송", description = "입력한 이메일로 6자리 인증코드를 보냅니다. 5분간 유효합니다.")
	@PostMapping("/signup/email-verification")
	public ResponseEntity<ApiResponse<Void>> requestEmailVerification(@Valid @RequestBody EmailVerificationRequest request) {
		emailVerificationService.requestCode(request.email());
		return ResponseEntity.ok(ApiResponse.<Void>success(null));
	}

	@Operation(summary = "회원가입 이메일 인증코드 확인", description = "받은 인증코드를 확인합니다. 확인된 이메일은 30분 이내에 회원가입을 완료해야 합니다.")
	@PostMapping("/signup/email-verification/confirm")
	public ResponseEntity<ApiResponse<Void>> confirmEmailVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
		emailVerificationService.confirmCode(request.email(), request.code());
		return ResponseEntity.ok(ApiResponse.<Void>success(null));
	}

	@Operation(summary = "닉네임 중복 확인", description = "회원가입/프로필 수정 전 닉네임 사용 가능 여부를 확인합니다.")
	@GetMapping("/signup/nickname-check")
	public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(
			@RequestParam String nickname) {
		return ResponseEntity.ok(ApiResponse.success(authService.checkNicknameAvailability(nickname)));
	}

	@Operation(summary = "회원가입", description = "이메일 인증이 완료된 이메일로 새 계정을 생성합니다.")
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

	@Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal Long userId) {
		return ResponseEntity.ok(ApiResponse.success(authService.getMe(userId)));
	}

	@Operation(summary = "비밀번호 재확인", description = "프로필 수정 등 민감한 작업 전 현재 비밀번호를 재확인합니다. 아무것도 변경하지 않습니다.")
	@PostMapping("/me/password/verify")
	public ResponseEntity<ApiResponse<Void>> verifyPassword(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody PasswordVerifyRequest request) {
		authService.verifyPassword(userId, request);
		return ResponseEntity.ok(ApiResponse.<Void>success(null));
	}

	@Operation(summary = "내 프로필 수정", description = "닉네임/이름/전화번호를 수정합니다. 이메일·권한·상태는 여기서 변경할 수 없습니다.")
	@PatchMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> updateMe(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody UserUpdateRequest request) {
		return ResponseEntity.ok(ApiResponse.success(authService.updateProfile(userId, request)));
	}

	@Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인한 뒤 새 비밀번호로 변경합니다. 소셜 로그인 계정은 사용할 수 없습니다.")
	@PatchMapping("/me/password")
	public ResponseEntity<ApiResponse<Void>> changePassword(
			@AuthenticationPrincipal Long userId,
			@Valid @RequestBody PasswordChangeRequest request) {
		authService.changePassword(userId, request);
		return ResponseEntity.ok(ApiResponse.<Void>success(null));
	}

	@Operation(summary = "비밀번호 재설정 이메일 인증코드 발송", description = "가입된 이메일로 6자리 인증코드를 보냅니다. 5분간 유효합니다.")
	@PostMapping("/password/reset/email-verification")
	public ResponseEntity<ApiResponse<Void>> requestPasswordResetVerification(
			@Valid @RequestBody EmailVerificationRequest request) {
		emailVerificationService.requestPasswordResetCode(request.email());
		return ResponseEntity.ok(ApiResponse.<Void>success(null));
	}

	@Operation(summary = "비밀번호 재설정 이메일 인증코드 확인", description = "받은 인증코드를 확인합니다. 확인된 이메일은 30분 이내에 비밀번호 재설정을 완료해야 합니다.")
	@PostMapping("/password/reset/email-verification/confirm")
	public ResponseEntity<ApiResponse<Void>> confirmPasswordResetVerification(
			@Valid @RequestBody EmailVerificationConfirmRequest request) {
		emailVerificationService.confirmPasswordResetCode(request.email(), request.code());
		return ResponseEntity.ok(ApiResponse.<Void>success(null));
	}

	@Operation(summary = "비밀번호 재설정", description = "이메일 인증이 완료된 계정의 비밀번호를 새 비밀번호로 변경합니다. 변경 즉시 기존 로그인 세션은 모두 폐기됩니다.")
	@PostMapping("/password/reset")
	public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok(ApiResponse.<Void>success(null));
	}

	@Operation(summary = "회원탈퇴", description = "계정을 비활성화합니다(소프트 삭제). 데이터는 실제로 삭제되지 않고 status만 WITHDRAWN으로 바뀝니다. 모든 리프레시 토큰이 폐기되고 즉시 로그아웃됩니다.")
	@PostMapping("/me/withdraw")
	public ResponseEntity<ApiResponse<Void>> withdraw(
			@AuthenticationPrincipal Long userId,
			@RequestBody(required = false) WithdrawRequest request) {
		authService.withdraw(userId, request != null ? request : new WithdrawRequest(null));

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
