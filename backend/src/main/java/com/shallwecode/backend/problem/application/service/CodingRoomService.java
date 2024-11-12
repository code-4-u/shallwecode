package com.shallwecode.backend.problem.application.service;

import com.shallwecode.backend.common.exception.CustomException;
import com.shallwecode.backend.common.exception.ErrorCode;
import com.shallwecode.backend.problem.application.dto.FindMyCodingRoomResDTO;
import com.shallwecode.backend.problem.application.dto.coop.CoopDTO;
import com.shallwecode.backend.problem.application.dto.coop.CoopResDTO;
import com.shallwecode.backend.problem.domain.aggregate.CodingRoom;
import com.shallwecode.backend.problem.domain.service.CodingRoomDomainService;
import com.shallwecode.backend.problem.domain.service.CoopDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodingRoomService {

    private final CodingRoomDomainService codingRoomDomainService;
    private final CoopDomainService coopDomainService;

    public List<FindMyCodingRoomResDTO> findMyCodingRoom(Long userId) {

        return codingRoomDomainService.findMyCodingRoom(userId);
    }

    /* 코딩방 생성 */
    public CodingRoom saveCodingRoom(Long problemId, Long loginUserId) {

        // 해당 번호의 문제가 있는지 확인해야 하지만 프론트에서 문제를 조회해서
        // 확인 후 코딩방 생성 예정이므로 패스.

        // 엔티티 반환 (생성된 codingRoomId 가 반환됨)
        CodingRoom codingRoom = codingRoomDomainService.saveCodingRoom(problemId);

        CoopDTO coopDTO = new CoopDTO();
        coopDTO.setCodingRoomId(codingRoom.getCodingRoomId());
        coopDTO.setUserId(loginUserId);
     // coopDTO.setUserId(23); - TestCode 실행시 유저 아이디를 가져올 수 없어서 임시로 작성한 코드임.
        coopDTO.setHost(true);

        coopDomainService.inviteCoopFriend(coopDTO);

        return codingRoom;
    }

    public void deleteCodingRoom(Long codingRoomId, Long loginUserId) {

        // 호스트인지 확인
        CoopResDTO UserInfo = coopDomainService.findByCoop(codingRoomId, loginUserId);

        if (!UserInfo.isHost()) {
            throw new CustomException(ErrorCode.NOT_HOST);
        }

        codingRoomDomainService.deleteCodingRoom(codingRoomId);

    }

    @Transactional
    public void updateCode(Long codingroomId, String code) {

        codingRoomDomainService.updateCode(codingroomId, code);
    }
}
