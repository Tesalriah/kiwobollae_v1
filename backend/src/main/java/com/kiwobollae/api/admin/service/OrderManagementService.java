package com.kiwobollae.api.admin.service;

import com.kiwobollae.api.commerce.dto.response.OrderDetailResponse;
import com.kiwobollae.api.commerce.dto.response.OrderItemResponse;
import com.kiwobollae.api.commerce.dto.response.OrderResponse;
import com.kiwobollae.api.commerce.entity.Order;
import com.kiwobollae.api.commerce.entity.OrderItem;
import com.kiwobollae.api.commerce.entity.enums.DeliveryStatus;
import com.kiwobollae.api.commerce.entity.enums.OrderStatus;
import com.kiwobollae.api.commerce.repository.OrderItemRepository;
import com.kiwobollae.api.commerce.repository.OrderRepository;
import com.kiwobollae.api.commerce.repository.ProductRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import com.kiwobollae.api.point.entity.enums.PointRefType;
import com.kiwobollae.api.point.service.WalletService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;
	private final WalletService walletService;

	@Transactional(readOnly = true)
	public Page<OrderResponse> getOrdersForAdmin(
			OrderStatus status,
			DeliveryStatus deliveryStatus,
			Long userId,
			LocalDateTime from,
			LocalDateTime to,
			Pageable pageable
	) {
		return orderRepository.search(status, deliveryStatus, userId, from, to, pageable).map(OrderResponse::from);
	}

	@Transactional(readOnly = true)
	public OrderDetailResponse getOrderForAdmin(Long id) {
		Order order = findOrderForAdmin(id);
		List<OrderItemResponse> items = orderItemRepository.findAllByOrderId(id).stream()
				.map(OrderItemResponse::from)
				.toList();
		return new OrderDetailResponse(OrderResponse.from(order), items);
	}

	@Transactional
	public OrderResponse shipOrder(Long id) {
		int updated = orderRepository.updateDeliveryStatusIfMatches(
				id, DeliveryStatus.SHIPPING, OrderStatus.PAID, DeliveryStatus.PREPARING
		);
		if (updated == 0) {
			throwNotFoundOrInvalidState(id);
		}
		return OrderResponse.from(findOrderForAdmin(id));
	}

	@Transactional
	public OrderResponse deliverOrder(Long id) {
		int updated = orderRepository.deliverIfMatches(
				id, DeliveryStatus.DELIVERED, LocalDateTime.now(KST), OrderStatus.PAID, DeliveryStatus.SHIPPING
		);
		if (updated == 0) {
			throwNotFoundOrInvalidState(id);
		}
		return OrderResponse.from(findOrderForAdmin(id));
	}

	@Transactional
	public OrderResponse adminCancelOrder(Long id) {
		Order order = findOrderForAdmin(id);
		int updated = orderRepository.cancelIfMatches(
				id, OrderStatus.CANCELLED, OrderStatus.PAID, DeliveryStatus.PREPARING, LocalDateTime.now(KST)
		);
		if (updated == 0) {
			throwNotFoundOrInvalidState(id);
		}

		for (OrderItem item : orderItemRepository.findAllByOrderId(id)) {
			productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
		}
		walletService.restorePurchasePoints(
				order.getUser().getId(),
				order.getUsedFreePoint(),
				order.getUsedPaidPoint(),
				PointRefType.ORDER,
				id
		);
		return OrderResponse.from(findOrderForAdmin(id));
	}

	private Order findOrderForAdmin(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
	}

	private void throwNotFoundOrInvalidState(Long id) {
		if (!orderRepository.existsById(id)) {
			throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
		}
		throw new BusinessException(ErrorCode.ORDER_INVALID_STATE);
	}
}
