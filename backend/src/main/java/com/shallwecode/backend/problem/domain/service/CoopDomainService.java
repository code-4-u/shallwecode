package com.shallwecode.backend.problem.domain.service;

import com.shallwecode.backend.problem.application.dto.CoopDTO;
import com.shallwecode.backend.problem.application.dto.CoopResDTO;
import com.shallwecode.backend.problem.domain.aggregate.Coop;
import com.shallwecode.backend.problem.domain.repository.CoopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoopDomainService {

    private final ModelMapper modelMapper;
    private final CoopRepository coopRepository;

    @Transactional
    public void inviteCoopFriend(CoopDTO coopDTO) {

        Coop coop = modelMapper.map(coopDTO, Coop.class);
        coopRepository.save(coop);
    }

    @Transactional
    public CoopResDTO findByCoop(Long codingRoomId,Long userId) {

        return modelMapper.map(coopRepository.findByCodingRoomIdAndUserId(codingRoomId, userId), CoopResDTO.class);
    }

    @Transactional
    public void delete(Long coopId){

        coopRepository.deleteById(coopId);

    }

}
