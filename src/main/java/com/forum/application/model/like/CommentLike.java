package com.forum.application.model.like;

import com.forum.application.model.Comment;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_liked_comment")
@Builder
@NoArgsConstructor
public final class CommentLike extends Like {

    @ManyToOne
    @JoinColumn(
            name = "comment_id",
            referencedColumnName = "comment_id"
    )
    private Comment comment;

    @Builder(builderMethodName = "commentLikeBuilder")
    public CommentLike(int id, User respondent, NotificationStatus notificationStatus, Comment comment) {
        super(id, respondent, notificationStatus);
        this.comment = comment;
    }
}
