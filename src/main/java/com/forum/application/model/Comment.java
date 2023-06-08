package com.forum.application.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tbl_forum_comment")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private int id;

    @Column(name = "body")
    private String body;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "upvote")
    private int upvote;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(
            name = "post_id",
            referencedColumnName = "post_id",
            foreignKey = @ForeignKey(name = "FK_post_id")
    )
    private Post post;

    @ManyToOne
    @JoinColumn(
            name = "commenter_id",
            referencedColumnName = "user_id",
            foreignKey = @ForeignKey(name = "FK_commenter_id")
    )
    private User commenter;

    // comment id reference is in reply table
    @OneToMany(
            mappedBy = "comment",
            cascade = CascadeType.REMOVE
    )
    @Setter(AccessLevel.NONE)
    private List<Reply> replies;

    // comment id reference is in comment upvote transaction table
    @OneToMany(mappedBy = "comment")
    @Setter(AccessLevel.NONE)
    private List<CommentUpvoteTransaction> commentUpvoteTransactions;
}
