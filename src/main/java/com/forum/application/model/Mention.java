package com.forum.application.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "tbl_mention_user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
}
