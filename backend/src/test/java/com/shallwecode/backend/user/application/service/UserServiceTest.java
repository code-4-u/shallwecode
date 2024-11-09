package com.shallwecode.backend.user.application.service;

import com.shallwecode.backend.user.application.dto.UserSaveDTO;
import com.shallwecode.backend.user.application.dto.UserUpdateDTO;
import com.shallwecode.backend.user.domain.aggregate.UserInfo;
import com.shallwecode.backend.user.domain.repository.UserRepository;
import com.shallwecode.backend.user.domain.service.UserDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserDomainService userDomainService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Test
    @DisplayName("회원가입 테스트")
    void saveUser(){
        // given
        UserSaveDTO userSaveDTO = new UserSaveDTO();
        userSaveDTO.setUserId(32L);
        userSaveDTO.setEmail("sr1094@naver.com");
        userSaveDTO.setNickname("serom");

        // when
        userService.saveUser(userSaveDTO);

        // then
        Optional<UserInfo> userInfo = userRepository.findByEmail("sr1094@naver.com");
        assertNotNull(userInfo);
    }

    @Test
    @DisplayName("회원 수정 테스트")
    void updateUser(){
        // 수정테스트 위한 회원가입
        // given
        UserSaveDTO userSaveDTO = new UserSaveDTO();
        userSaveDTO.setUserId(32L);
        userSaveDTO.setEmail("sr1094@naver.com");
        userSaveDTO.setNickname("serom");
        userService.saveUser(userSaveDTO);

        // when
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUserId(32L);
        userUpdateDTO.setNickName("꺄르륵");

        userService.updateUser(userUpdateDTO);

        // then
        Optional<UserInfo> userInfo = userRepository.findByEmail("sr1094@naver.com");
        assertEquals("꺄르륵", userInfo.get().getNickname());
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    void deleteUser(){
        // 삭제테스트 위한 회원가입
        // given
        UserSaveDTO userSaveDTO = new UserSaveDTO();
        userSaveDTO.setUserId(32L);
        userSaveDTO.setEmail("sr1094@naver.com");
        userSaveDTO.setNickname("serom");
        userService.saveUser(userSaveDTO);

        // when
        userService.deleteUser(32L);

        // then
        assertFalse(userRepository.findByEmail("sr1094@naver.com").isPresent());
    }
}