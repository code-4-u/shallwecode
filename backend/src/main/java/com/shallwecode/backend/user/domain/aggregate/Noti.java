package com.shallwecode.backend.user.domain.aggregate;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity(name = "Noti")
@Table(name = "noti")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Noti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notiId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @Enumerated(EnumType.STRING)
    private NotiType notiType;
    private Long codingRoomId;
    private String content;
    private Integer IsRead;

    @CreatedDate
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        IsRead = 0; // 읽지 않음
        createdAt = LocalDateTime.now();
    }
}
