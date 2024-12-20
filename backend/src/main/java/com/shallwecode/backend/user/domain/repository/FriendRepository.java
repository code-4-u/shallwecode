package com.shallwecode.backend.user.domain.repository;

import com.shallwecode.backend.user.domain.aggregate.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByFromUser_UserIdAndToUser_UserId(Long fromUserId, Long toUserId);

    void deleteByFromUser_UserIdAndToUser_UserId(Long userId, Long loginUserId);

    void deleteByFromUser_UserIdOrToUser_UserId(Long userId1, Long userId2);

}
