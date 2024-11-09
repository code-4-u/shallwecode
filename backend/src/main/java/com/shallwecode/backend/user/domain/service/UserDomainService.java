package com.shallwecode.backend.user.domain.service;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shallwecode.backend.common.exception.CustomException;
import com.shallwecode.backend.common.exception.ErrorCode;
import com.shallwecode.backend.problem.domain.aggregate.QProblem;
import com.shallwecode.backend.problem.domain.aggregate.QTry;
import com.shallwecode.backend.user.application.dto.*;
import com.shallwecode.backend.user.domain.aggregate.FriendStatus;
import com.shallwecode.backend.user.domain.aggregate.QFriend;
import com.shallwecode.backend.user.domain.aggregate.QUserInfo;
import com.shallwecode.backend.user.domain.aggregate.UserInfo;
import com.shallwecode.backend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDomainService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JPAQueryFactory jpaQueryFactory;

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
    public void deleteUser(Long userId) {
        UserInfo userInfo = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("user not found " + userId));
        userRepository.delete(userInfo);
    }

    public FindUserDTO findById(Long userId) {
        UserInfo foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        return modelMapper.map(foundUser, FindUserDTO.class);
    }

    public void save(UserSaveDTO saveUserDTO) {
        UserInfo saveUser = userRepository.findById(saveUserDTO.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        modelMapper.map(saveUserDTO, saveUser);
        saveUser.updateAuth();
    }

    public List<FindUserListDTO> findRequestUser(Long loginUserId) {
        QUserInfo user = QUserInfo.userInfo;
        QFriend friend = QFriend.friend;

        return jpaQueryFactory
                .select(Projections.constructor(FindUserListDTO.class,
                        user.userId,
                        user.email,
                        user.nickname,
                        new CaseBuilder()
                                .when(friend.friendStatus.eq(FriendStatus.ACCEPTED)
                                        .and(
                                                friend.fromUser.userId.eq(loginUserId).and(friend.toUser.userId.eq(user.userId))
                                                        .or(friend.toUser.userId.eq(loginUserId).and(friend.fromUser.userId.eq(user.userId)))
                                        ))
                                .then(true)
                                .otherwise(false)))
                .from(user)
                .leftJoin(friend).on(
                        friend.fromUser.userId.eq(loginUserId).and(friend.toUser.userId.eq(user.userId))
                                .or(friend.toUser.userId.eq(loginUserId).and(friend.fromUser.userId.eq(user.userId)))
                )
                .where(user.userId.ne(loginUserId))
                .orderBy(user.userId.asc())
                .fetch();
    }

    public Long findFinishedProblemCnt(Long loginUserId) {

        QUserInfo userInfo = QUserInfo.userInfo;
        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        return jpaQueryFactory
                .select(qProblem.problemId.countDistinct())
                .from(qProblem)
                .leftJoin(qTry)
                .on(qProblem.problemId.eq(qTry.problemId))
                .where(qTry.userId.eq(loginUserId)
                        .and(qTry.isSolved.eq(true)))
                .fetchOne();
    }

    public Long findDoingProblemCnt(Long loginUserId) {

        QUserInfo userInfo = QUserInfo.userInfo;
        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        return jpaQueryFactory
                .select(qProblem.problemId.countDistinct())
                .from(qProblem)
                .leftJoin(qTry)
                .on(qProblem.problemId.eq(qTry.problemId))
                .where(qTry.userId.eq(loginUserId))
                .fetchOne();
    }

    public Long findAllProblemCnt(Long loginUserId) {

        QUserInfo userInfo = QUserInfo.userInfo;
        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        return jpaQueryFactory
                .select(qProblem.count())
                .from(qProblem)
                .fetchOne();
    }


    public FindUserDetailDTO findSimpleInfoById(Long loginUserId) {

        UserInfo userInfo = userRepository.findById(loginUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        return modelMapper.map(userInfo, FindUserDetailDTO.class);
    }

    // refreshToken 재저장
    @Transactional
    public void updateRefreshToken(Long userId, String refreshToken) {
        UserInfo userInfo = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        userInfo.updateRefreshToken(refreshToken);
    }
}
