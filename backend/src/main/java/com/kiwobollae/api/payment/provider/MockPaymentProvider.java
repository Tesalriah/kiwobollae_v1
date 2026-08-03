package com.kiwobollae.api.payment.provider;

import com.kiwobollae.api.payment.entity.enums.PaymentProviderType;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// prod에서도 이 빈이 뜰 수 있다 — 그 대신 ProductionPaymentProviderValidator가
// payment.allow-mock-in-prod를 명시적으로 켜지 않으면 기동 자체를 막는다. 실제 결제
// 연동(Toss 등) 구현 전, 운영 배포를 임시로 열어두기 위한 장치다.
@Component
@ConditionalOnProperty(prefix = "payment", name = "provider", havingValue = "MOCK", matchIfMissing = true)
public class MockPaymentProvider implements PaymentProvider {

	@Override
	public PaymentProviderType getType() {
		return PaymentProviderType.MOCK;
	}

	@Override
	public PaymentConfirmResult confirm(PaymentConfirmCommand command) {
		return switch (command.scenario()) {
			case SUCCESS -> PaymentConfirmResult.success();
			case FAILURE -> PaymentConfirmResult.failure();
			case CANCEL -> PaymentConfirmResult.canceled();
		};
	}

	@Override
	public PaymentRefundResult refund(PaymentRefundCommand command) {
		return PaymentRefundResult.success("MOCK-REFUND-" + UUID.randomUUID());
	}
}
