package com.forum.application.model.like;

import com.forum.application.model.Comment;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_liked_comment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class CommentLike extends Like {

    @ManyToOne
    @JoinColumn(
            name = "comment_id",
            referencedColumnName = "comment_id"
    )
    private Comment comment;
}
