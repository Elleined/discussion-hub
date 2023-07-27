package com.forum.application.model.like;

import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
import com.forum.application.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_liked_post")
@NoArgsConstructor
public final class PostLike extends Like {

    @ManyToOne
    @JoinColumn(
            name = "post_id",
            referencedColumnName = "post_id"
    )
    @Getter
    private Post post;

    @Builder(builderMethodName = "postLikeBuilder")

    public PostLike(int id, User respondent, LocalDateTime createdAt, NotificationStatus notificationStatus, Post post) {
        super(id, respondent, createdAt, notificationStatus);
        this.post = post;
    }
}
