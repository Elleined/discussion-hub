package com.forum.application.model.like;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Entity
@Table
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public abstract class Like {

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
    private User respondent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status")
    private NotificationStatus notificationStatus;

    public abstract String getMessage();
    public abstract int getSubscriberId();
}
