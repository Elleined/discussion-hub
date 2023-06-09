package com.forum.application.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_forum_post")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private int id;

    @Column(name = "body")
    private String body;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @ManyToOne
    @JoinColumn(
            name = "author_id",
            referencedColumnName = "user_id",
            foreignKey = @ForeignKey(name = "FK_author_id")
    )
    private User author;

    // post id reference is in comment table
    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.REMOVE
    )
    private List<Comment> comments = new ArrayList<>();
}
