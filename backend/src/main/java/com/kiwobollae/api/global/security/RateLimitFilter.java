package com.kiwobollae.api.global.security;

import com.kiwobollae.api.global.exception.ErrorCode;
import com.kiwobollae.api.global.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Simple in-memory fixed-window rate limiter. Registered only for the
 * token-reissue path (see FilterConfig) to blunt abuse/loop scenarios — a
 * legitimate user refreshing on page load/401 retries never gets close to
 * this limit. Not a {@code @Component} on purpose: it's instantiated and
 * URL-scoped explicitly by FilterConfig instead of being auto-registered
 * for "/*" by Spring Boot.
 */
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

	private static final int MAX_REQUESTS_PER_WINDOW = 30;
	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final ObjectMapper objectMapper;
	private final ConcurrentHashMap<String, Window> windowsByClient = new ConcurrentHashMap<>();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String clientKey = resolveClientKey(request);
		Window window = windowsByClient.computeIfAbsent(clientKey, key -> new Window());

		if (window.tryConsume()) {
			filterChain.doFilter(request, response);
			return;
		}

		ErrorCode errorCode = ErrorCode.COMMON_RATE_LIMITED;
		ErrorResponse body = ErrorResponse.of(
				errorCode, errorCode.getDefaultMessage(), null, null,
				ErrorResponse.newTraceId(), request.getRequestURI());

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}

	private String resolveClientKey(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static final class Window {
		private volatile long windowStartMillis = System.currentTimeMillis();
		private final AtomicInteger count = new AtomicInteger(0);

		synchronized boolean tryConsume() {
			long now = System.currentTimeMillis();
			if (now - windowStartMillis >= WINDOW.toMillis()) {
				windowStartMillis = now;
				count.set(0);
			}
			return count.incrementAndGet() <= MAX_REQUESTS_PER_WINDOW;
		}
	}
}
