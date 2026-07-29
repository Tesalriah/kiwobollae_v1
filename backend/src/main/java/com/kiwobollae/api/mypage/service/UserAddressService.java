package com.kiwobollae.api.mypage.service;

import com.kiwobollae.api.auth.entity.User;
import com.kiwobollae.api.auth.repository.UserRepository;
import com.kiwobollae.api.global.exception.BusinessException;
import com.kiwobollae.api.global.exception.ErrorCode;
import com.kiwobollae.api.mypage.dto.request.UserAddressRequest;
import com.kiwobollae.api.mypage.dto.response.UserAddressResponse;
import com.kiwobollae.api.mypage.entity.UserAddress;
import com.kiwobollae.api.mypage.repository.UserAddressRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAddressService {

	private static final int MAX_ADDRESSES_PER_USER = 5;

	private final UserAddressRepository userAddressRepository;
	private final UserRepository userRepository;

	public List<UserAddressResponse> getAddresses(Long userId) {
		return userAddressRepository.findAllByUser_IdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
				.map(UserAddressResponse::from)
				.toList();
	}

	@Transactional
	public UserAddressResponse createAddress(Long userId, UserAddressRequest request) {
		if (userAddressRepository.countByUser_Id(userId) >= MAX_ADDRESSES_PER_USER) {
			throw new BusinessException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
		}
		User user = userRepository.getReferenceById(userId);
		UserAddress saved = userAddressRepository.save(UserAddress.create(
				user, request.receiverName(), request.receiverPhone(), request.zipCode(),
				request.address(), request.addressDetail(), request.isDefault()));
		// 새 배송지는 저장 후에야 id가 생기므로, 기본 지정은 저장 다음에 원자적으로 처리한다.
		if (request.isDefault()) {
			userAddressRepository.setOnlyDefault(userId, saved.getId());
		}
		return UserAddressResponse.from(saved);
	}

	@Transactional
	public UserAddressResponse updateAddress(Long userId, Long addressId, UserAddressRequest request) {
		UserAddress address = findOwnedAddress(userId, addressId);
		address.update(request.receiverName(), request.receiverPhone(), request.zipCode(),
				request.address(), request.addressDetail());
		if (request.isDefault()) {
			userAddressRepository.setOnlyDefault(userId, addressId);
			address.markDefault();
		} else if (address.getIsDefault()) {
			address.unmarkDefault();
		}
		return UserAddressResponse.from(address);
	}

	@Transactional
	public void deleteAddress(Long userId, Long addressId) {
		UserAddress address = findOwnedAddress(userId, addressId);
		userAddressRepository.delete(address);
	}

	@Transactional
	public UserAddressResponse setDefaultAddress(Long userId, Long addressId) {
		UserAddress address = findOwnedAddress(userId, addressId);
		if (!address.getIsDefault()) {
			userAddressRepository.setOnlyDefault(userId, addressId);
			address.markDefault();
		}
		return UserAddressResponse.from(address);
	}

	private UserAddress findOwnedAddress(Long userId, Long addressId) {
		return userAddressRepository.findByIdAndUser_Id(addressId, userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
	}
}
