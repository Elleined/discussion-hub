package com.forum.application.model.like;

import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
import com.forum.application.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_liked_post")
@NoArgsConstructor
public final class PostLike extends Like {

    @ManyToOne
    @JoinColumn(
            name = "post_id",
            referencedColumnName = "post_id"
    )
    @Getter @Setter
    private Post post;

    @Builder(builderMethodName = "postLikeBuilder")
    public PostLike(int id, User respondent, NotificationStatus notificationStatus, Post post) {
        super(id, respondent, notificationStatus);
        this.post = post;
    }
}
