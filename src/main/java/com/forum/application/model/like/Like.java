package com.forum.application.model.like;

import com.forum.application.model.NotificationStatus;
import com.forum.application.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class Like {

    @Id
    @GeneratedValue(
            strategy = GenerationType.TABLE,
            generator = "autoIncrement"
    )
    @SequenceGenerator(
            allocationSize = 1,
            name = "autoIncrement",
            sequenceName = "autoIncrement"
    )
    @Column(name = "like_id")
    private int id;

    @ManyToOne
    @JoinColumn(
            name = "respondent_id",
            referencedColumnName = "user_id"
    )
    private User respondentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status")
    private NotificationStatus notificationStatus;
}
