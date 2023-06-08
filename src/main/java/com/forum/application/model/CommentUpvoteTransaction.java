package com.forum.application.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_comment_upvote_transaction")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CommentUpvoteTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_upvote_id")
    private int id;

    @ManyToOne
    @JoinColumn(
            name = "comment_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_comment_upvote_id")
    )
    private Comment comment;
    
    @ManyToOne
    @JoinColumn(
            name = "respondent_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_respondent_id")
    )
    private User respondent;
}
