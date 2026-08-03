package com.kiwobollae.api.payment.provider;

import com.kiwobollae.api.payment.entity.enums.PaymentProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionPaymentProviderValidator {

	// 실제 결제 연동(Toss 등) 구현 전까지 운영 배포를 열어두기 위한 임시 탈출구.
	// payment.allow-mock-in-prod를 명시적으로 true로 설정하지 않으면 여전히 막힌다 —
	// 실결제 연동이 끝나면 이 설정과 이 우회 로직 자체를 제거해야 한다.
	public ProductionPaymentProviderValidator(
			@Value("${payment.provider}") PaymentProviderType paymentProvider,
			@Value("${payment.allow-mock-in-prod:false}") boolean allowMockInProd
	) {
		if (paymentProvider == PaymentProviderType.MOCK && !allowMockInProd) {
			throw new IllegalStateException(
					"운영 환경에서는 Mock 결제 프로바이더를 사용할 수 없습니다. "
							+ "실제 결제 연동 전 임시로 필요하다면 payment.allow-mock-in-prod=true를 명시적으로 설정하세요."
			);
		}
	}
}
