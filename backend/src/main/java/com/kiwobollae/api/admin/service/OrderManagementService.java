package com.kiwobollae.api.admin.service;

import com.kiwobollae.api.commerce.dto.response.OrderDetailResponse;
import com.kiwobollae.api.commerce.dto.response.OrderItemResponse;
import com.kiwobollae.api.commerce.dto.response.OrderResponse;
import com.kiwobollae.api.commerce.entity.Order;
import com.kiwobollae.api.commerce.entity.enums.DeliveryStatus;
import com.kiwobollae.api.commerce.entity.enums.OrderStatus;
import com.kiwobollae.api.commerce.repository.OrderItemRepository;
import com.kiwobollae.api.commerce.repository.OrderRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

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

	private Order findOrderForAdmin(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
	}
}
