package com.shallwecode.backend.user.application.service;

import com.shallwecode.backend.common.exception.CustomException;
import com.shallwecode.backend.common.exception.ErrorCode;
import com.shallwecode.backend.problem.application.dto.coop.CoopResDTO;
import com.shallwecode.backend.problem.domain.service.CodingRoomDomainService;
import com.shallwecode.backend.problem.domain.service.CoopDomainService;
import com.shallwecode.backend.problem.domain.service.TryDomainService;
import com.shallwecode.backend.user.application.dto.friend.FindFriendDetailDTO;
import com.shallwecode.backend.user.application.dto.user.*;
import com.shallwecode.backend.user.domain.aggregate.AuthType;
import com.shallwecode.backend.user.domain.aggregate.UserInfo;
import com.shallwecode.backend.user.domain.repository.UserRepository;
import com.shallwecode.backend.user.domain.service.FriendDomainService;
import com.shallwecode.backend.user.domain.service.NotiDomainService;
import com.shallwecode.backend.user.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final CoopDomainService coopDomainService;
    private final CodingRoomDomainService codingRoomDomainService;
    private final NotiDomainService notiDomainService;
    private final TryDomainService tryDomainService;
    private final FriendDomainService friendDomainService;

    private final ModelMapper modelMapper;

    // 회원 가입
    @Transactional
    public void saveUser(UserSaveDTO userSaveDTO) {
        userDomainService.validateNewUser(userSaveDTO);
        userDomainService.save(userSaveDTO);
    }

    // 회원 닉네임 수정
    @Transactional
    public void updateUser(UserUpdateDTO userUpdateDTO, Long loginUserId) {
        userDomainService.updateUser(userUpdateDTO, loginUserId);
    }

    // 회원 삭제
    @Transactional
    public void deleteUser(Long userId) {

        // 회원 삭제 전
        // 협업 친구 삭제시 삭제한 userId가 호스트라면 연관된 코딩방도 함께 삭제되어야 한다.

        FindUserDTO findUserDTO = userDomainService.findById(userId);
        if(findUserDTO.getAuth().equals(AuthType.ADMIN)) {
            throw new CustomException(ErrorCode.NOT_ADMIN_DELETE_ADMIN);
        }

        CoopResDTO coopResDTO = coopDomainService.findCoopByUserId(userId);
        if(coopResDTO != null) { // 해당 유저 코딩방 존재하는 경우
            if (coopResDTO.isHost()) {
                // 해당 호스트 유저의 코딩방을 삭제한다.
                codingRoomDomainService.deleteCodingRoom(coopResDTO.getCodingRoomId());
            } else {
                coopDomainService.deleteByUserId(userId);
            }
        }

        notiDomainService.deleteNotiByUserId(userId);
        tryDomainService.deleteTryByUserId(userId);
        friendDomainService.deleteFriendByUserId(userId, userId);
        userDomainService.deleteUser(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserInfo loginUser = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException(userId));

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(loginUser.getAuth().toString()));

        return new User(String.valueOf(loginUser.getUserId()), "", grantedAuthorities);
    }

    // 닉네임으로 회원 목록 조회
    @Transactional(readOnly = true)
    public List<FindUserDetailDTO> findUserDetailsByNickname(String nickname) {
        List<FindUserDetailDTO> userDetailList = new ArrayList<>();

        List<FindUserDTO> userList = userDomainService.findAllUsers(nickname);

        Long allProblemCnt = userDomainService.findAllProblemCnt();

        for (FindUserDTO findUserDTO : userList) {
            FindUserDetailDTO userDetailDTO = modelMapper.map(findUserDTO, FindUserDetailDTO.class);
            userDetailList.add(userDetailDTO);

            Long doingProblemCnt = userDomainService.findDoingProblemCnt(findUserDTO.getUserId());
            Long finishedProblemCnt = userDomainService.findFinishedProblemCnt(findUserDTO.getUserId());
            Long notFinishedProblemCnt = allProblemCnt - doingProblemCnt;

            userDetailDTO.setDoingProblemCnt(doingProblemCnt);
            userDetailDTO.setFinishedProblemCnt(finishedProblemCnt);
            userDetailDTO.setNotFinishedProblemCnt(notFinishedProblemCnt);
        }

        return userDetailList;
    }

    // 닉네임으로 관리자 회원 목록 조회
    @Transactional(readOnly = true)
    public List<FindUserInfoDTO> findUserInfoByNickname(String nickname) {
        return userDomainService.findAllInfoUsers(nickname);
    }

    @Transactional(readOnly = true)
    public FindUserDetailDTO findUserDetail(Long loginUserId) {

        Long allProblemCnt = userDomainService.findAllProblemCnt();
        Long doingProblemCnt = userDomainService.findDoingProblemCnt(loginUserId);
        Long finishedProblemCnt = userDomainService.findFinishedProblemCnt(loginUserId);

        Long notFinishedProblemCnt = allProblemCnt - doingProblemCnt;

        FindUserDetailDTO findUserDetailDTO = userDomainService.findSimpleInfoById(loginUserId);

        findUserDetailDTO.setDoingProblemCnt(doingProblemCnt);
        findUserDetailDTO.setFinishedProblemCnt(finishedProblemCnt);
        findUserDetailDTO.setNotFinishedProblemCnt(notFinishedProblemCnt);

        return findUserDetailDTO;
    }

    @Transactional(readOnly = true)
    public List<FindUserListDTO> findRequestUser(Long loginUserId) {

        return userDomainService.findRequestUser(loginUserId);
    }

    @Transactional(readOnly = true)
    public FindFriendDetailDTO findFriendDetail(Long userId) {

        FindUserDetailDTO userDetailDTO = findUserDetail(userId);
        return modelMapper.map(userDetailDTO, FindFriendDetailDTO.class);
    }
}
