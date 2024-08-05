package com.ajangajang.backend.user.model.service;

import com.ajangajang.backend.api.kakaomap.model.service.KakaoApiService;
import com.ajangajang.backend.exception.CustomGlobalException;
import com.ajangajang.backend.exception.CustomStatusCode;
import com.ajangajang.backend.user.model.dto.AddressDto;
import com.ajangajang.backend.user.model.entity.Address;
import com.ajangajang.backend.user.model.entity.User;
import com.ajangajang.backend.user.model.entity.UserAddress;
import com.ajangajang.backend.user.model.repository.AddressRepository;
import com.ajangajang.backend.user.model.repository.UserAddressRepository;
import com.ajangajang.backend.user.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final KakaoApiService kakaoApiService;

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserAddressRepository userAddressRepository;

    public AddressDto saveAddressInfo(String username, String addressName) {
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new CustomGlobalException(CustomStatusCode.USER_NOT_FOUND));
        Address responseAddress = kakaoApiService.getAddressByAddressName(addressName);
        Address findAddress = addressRepository.findByAddressCode(responseAddress.getAddressCode());

        // 이미 있는 주소면 조회 / 없으면 저장후 반환
        Address address;
        if (findAddress == null) {
            address = addressRepository.save(responseAddress);
        } else {
            address = findAddress;
        }

        // 대표 주소가 없으면 첫번째 주소를 대표로 설정
        if (findUser.getMainAddress() == null) {
            findUser.setMainAddress(address);
        }

        UserAddress userAddress = new UserAddress(findUser, address);
        userAddressRepository.save(userAddress);
        return new AddressDto(address.getSido(), address.getSigg(), address.getEmd(),
                            address.getFullAddress());
    }

    public AddressDto saveAddressInfo(String username, double longitude, double latitude) {
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new CustomGlobalException(CustomStatusCode.USER_NOT_FOUND));
        Address responseAddress = kakaoApiService.getAddressByCoordinates(longitude, latitude);
        Address findAddress = addressRepository.findByAddressCode(responseAddress.getAddressCode());

        // 이미 있는 주소면 조회 / 없으면 저장후 반환
        Address address;
        if (findAddress == null) {
            address = addressRepository.save(responseAddress);
        } else {
            address = findAddress;
        }

        // 대표 주소가 없으면 첫번째 주소를 대표로 설정
        if (findUser.getMainAddress() == null) {
            findUser.setMainAddress(address);
        }
        UserAddress userAddress = new UserAddress(findUser, address);
        userAddressRepository.save(userAddress);
        return new AddressDto(address.getSido(), address.getSigg(), address.getEmd(),
                            address.getFullAddress());
    }

    public List<AddressDto> findMyAddresses(String username) {
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new CustomGlobalException(CustomStatusCode.USER_NOT_FOUND));
        return userAddressRepository.findMyAddresses(findUser.getId()).stream()
                .map(address -> new AddressDto(address.getId(), address.getSido(), address.getSigg(),
                                            address.getEmd(), address.getFullAddress()))
                .collect(Collectors.toList());
    }

    public void deleteAddress(String username, Long id) {
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new CustomGlobalException(CustomStatusCode.USER_NOT_FOUND));
        userAddressRepository.findByUserIdAndAddressId(findUser.getId(), id).orElseThrow(() -> new CustomGlobalException(CustomStatusCode.ADDRESS_NOT_FOUND));
        userAddressRepository.deleteByUserIdAndAddressId(findUser.getId(), id);
    }

    public Long saveMainAddress(String username, Long id) {
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new CustomGlobalException(CustomStatusCode.USER_NOT_FOUND));
        Address findAddress = addressRepository.findById(id).orElseThrow(() -> new CustomGlobalException(CustomStatusCode.ADDRESS_NOT_FOUND));
        findUser.setMainAddress(findAddress);
        return findUser.getMainAddress().getId();
    }

}
