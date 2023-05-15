package com.forum.application.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_forum_reply")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private int id;

    @Column(name = "body")
    private String body;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @ManyToOne
    @JoinColumn(
            name = "comment_id",
            referencedColumnName = "comment_id",
            foreignKey = @ForeignKey(name = "FK_comment_id")
    )
    private Comment comment;

    @ManyToOne
    @JoinColumn(
            name = "replier_id",
            referencedColumnName = "user_id",
            foreignKey = @ForeignKey(name = "FK_replier_id")
    )
    private User replier;
}
