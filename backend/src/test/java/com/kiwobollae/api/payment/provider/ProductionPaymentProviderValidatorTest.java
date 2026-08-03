package com.kiwobollae.api.payment.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

class ProductionPaymentProviderValidatorTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(ProductionPaymentProviderValidator.class);

	@Test
	void productionProfileDefaultsProviderToToss() throws IOException {
		var propertySources = new YamlPropertySourceLoader()
				.load("application-prod", new ClassPathResource("application-prod.yaml"));

		assertThat(propertySources)
				.anySatisfy(propertySource ->
						assertThat(propertySource.getProperty("payment.provider")).isEqualTo("TOSS"));
	}

	@Test
	void rejectsMockProviderWhenProductionProfileIsActiveAndAllowMockInProdIsNotSet() {
		contextRunner
				.withPropertyValues(
						"spring.profiles.active=prod",
						"payment.provider=MOCK"
				)
				.run(context -> {
					assertThat(context).hasFailed();
					assertThat(context.getStartupFailure())
							.hasRootCauseInstanceOf(IllegalStateException.class);
					assertThat(context.getStartupFailure().getRootCause().getMessage())
							.contains("운영 환경에서는 Mock 결제 프로바이더를 사용할 수 없습니다.");
				});
	}

	// 실제 Toss 연동 전까지 운영 배포를 열어두기 위한 임시 탈출구 — payment.allow-mock-in-prod를
	// 명시적으로 true로 켜야만 통과한다. Toss 연동이 끝나면 이 테스트와 그 대상 로직 모두 제거한다.
	@Test
	void acceptsMockProviderWhenProductionProfileIsActiveAndAllowMockInProdIsExplicitlyTrue() {
		contextRunner
				.withPropertyValues(
						"spring.profiles.active=prod",
						"payment.provider=MOCK",
						"payment.allow-mock-in-prod=true"
				)
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).hasSingleBean(ProductionPaymentProviderValidator.class);
				});
	}

	@Test
	void acceptsTossProviderWhenProductionProfileIsActive() {
		contextRunner
				.withPropertyValues(
						"spring.profiles.active=prod",
						"payment.provider=TOSS"
				)
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).hasSingleBean(ProductionPaymentProviderValidator.class);
				});
	}

	@Test
	void doesNotApplyProductionValidatorToLocalMockProvider() {
		contextRunner
				.withPropertyValues(
						"spring.profiles.active=local",
						"payment.provider=MOCK"
				)
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).doesNotHaveBean(ProductionPaymentProviderValidator.class);
				});
	}

	// MockPaymentProvider 자체는 프로필과 무관하게 payment.provider=MOCK이면 항상 뜬다 — prod에서의
	// 차단은 이 빈이 아니라 ProductionPaymentProviderValidator(allow-mock-in-prod 미설정 시 기동 실패)가
	// 담당한다.
	@Test
	void loadsMockProviderRegardlessOfProfileWhenProviderIsMock() {
		new ApplicationContextRunner()
				.withUserConfiguration(MockPaymentProvider.class)
				.withPropertyValues(
						"spring.profiles.active=prod",
						"payment.provider=MOCK"
				)
				.run(context -> {
					assertThat(context).hasNotFailed();
					assertThat(context).hasSingleBean(MockPaymentProvider.class);
				});
	}
}
