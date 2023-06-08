package com.forum.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "tbl_user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "email_address", unique = true)
    private String email;

    @Column(name = "picture")
    private String picture;

    // user id reference is in post table
    @OneToMany(
            mappedBy = "author",
            cascade = CascadeType.REMOVE
    )
    @Setter(AccessLevel.NONE)
    private List<Post> posts;

    // user id reference is in comment table
    @OneToMany(
            mappedBy = "commenter",
            cascade = CascadeType.REMOVE
    )
    @Setter(AccessLevel.NONE)
    private List<Comment> comments;

    // user id reference is in reply table
    @OneToMany(
            mappedBy = "replier",
            cascade = CascadeType.REMOVE
    )
    @Setter(AccessLevel.NONE)
    private List<Reply> replies;
}
