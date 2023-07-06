package com.forum.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "tbl_mention_user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Mention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(
            name = "mentioned_user",
            referencedColumnName = "user_id"
    )
    private User mentionedUser;

    @ManyToOne
    @JoinColumn(
            name = "mentioning_user",
            referencedColumnName = "user_id"
    )
    private User mentioningUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;

    @Column(name = "type_id")
    private int typeId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "notification_status")
    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;
}
