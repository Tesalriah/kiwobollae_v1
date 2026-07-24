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
	public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(ObjectMapper objectMapper) {
		FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(new RateLimitFilter(objectMapper));
		registration.addUrlPatterns(ApiVersion.V1 + "/auth/reissue");
		registration.setName("rateLimitFilter");
		registration.setOrder(1);
		return registration;
	}
}
