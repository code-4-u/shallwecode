package com.shallwecode.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_SAVED_FRIEND(HttpStatus.CONFLICT, "친구 신청 실패"),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "회원 조회 실패"),
    SENDED_FRIEND(HttpStatus.CONFLICT, "이미 존재하는 친구 초대 요청"),
    DUPLICATED_FRIEND_USER(HttpStatus.BAD_REQUEST, "자신에게 친구 요청을 보낼 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
