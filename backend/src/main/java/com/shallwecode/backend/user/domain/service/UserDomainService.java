package com.shallwecode.backend.user.domain.service;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shallwecode.backend.common.util.CustomUserUtils;
import com.shallwecode.backend.user.application.dto.FindUserListDTO;
import com.shallwecode.backend.user.application.dto.UserSaveDTO;
import com.shallwecode.backend.common.exception.CustomException;
import com.shallwecode.backend.common.exception.ErrorCode;
import com.shallwecode.backend.user.application.dto.FindUserDTO;
import com.shallwecode.backend.user.application.dto.UserUpdateDTO;
import com.shallwecode.backend.user.domain.aggregate.QUserInfo;
import com.shallwecode.backend.user.domain.aggregate.UserInfo;
import com.shallwecode.backend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDomainService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JPAQueryFactory queryFactory;

    // 회원 가입 시 유효성 검사
    public void validateNewUser(UserSaveDTO saveUserDTO) {
        if (userRepository.existsByEmail(saveUserDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 사용자 email 입니다.");
        }
    }

    // 회원 닉네임 수정
    public void updateUser(UserInfo userInfo, UserUpdateDTO userUpdateDTO) {
        userInfo.updateUser(userUpdateDTO.getNickName());
    }

    // 회원 삭제
    public void deleteUser(Long userId){
        UserInfo userInfo = userRepository.findById(userId).orElseThrow(()->new UsernameNotFoundException("user not found " + userId));
        userRepository.delete(userInfo);
    }

    public FindUserDTO findById(Long userId) {
        UserInfo foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        return modelMapper.map(foundUser, FindUserDTO.class);
    }

    public void save(UserSaveDTO saveUserDTO) {
        Long loginUserId = CustomUserUtils.getCurrentUserSeq();
        UserInfo saveUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        modelMapper.map(saveUserDTO, saveUser);
        saveUser.updateAuth();
    }

    public List<FindUserListDTO> findRequestUser(Long loginUserId) {

        QUserInfo user = QUserInfo.userInfo;

        List<FindUserListDTO> list = new ArrayList<>();

//        return queryFactory
//                .select(Projections.constructor(FindUserListDTO.class,
//                        user.userId,
//                        user.email,
//                        user.nickname))
//                .from(user)
//                .where()
//                .fetch();

        return list;
    }
}
