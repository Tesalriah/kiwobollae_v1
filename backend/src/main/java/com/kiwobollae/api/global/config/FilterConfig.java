package com.kiwobollae.api.global.config;

import com.kiwobollae.api.global.common.ApiVersion;
import com.kiwobollae.api.global.security.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<RateLimitFilter> reissueRateLimitFilter(ObjectMapper objectMapper) {
		FilterRegistrationBean<RateLimitFilter> registration =
				new FilterRegistrationBean<>(new RateLimitFilter(objectMapper, 30));
		registration.addUrlPatterns(ApiVersion.V1 + "/auth/reissue");
		registration.setName("reissueRateLimitFilter");
		registration.setOrder(1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<RateLimitFilter> emailVerificationRateLimitFilter(ObjectMapper objectMapper) {
		// Much stricter than reissue: sending email costs real money/reputation and is a
		// common enumeration/spam vector, so cap it well below the generic default.
		FilterRegistrationBean<RateLimitFilter> registration =
				new FilterRegistrationBean<>(new RateLimitFilter(objectMapper, 5));
		registration.addUrlPatterns(ApiVersion.V1 + "/auth/signup/email-verification");
		registration.setName("emailVerificationRateLimitFilter");
		registration.setOrder(1);
		return registration;
	}
}
