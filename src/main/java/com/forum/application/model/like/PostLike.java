package com.forum.application.model.like;

import com.forum.application.dto.NotificationResponse;
import com.forum.application.mapper.NotificationMapper;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Post;
import com.forum.application.model.User;
import jakarta.persistence.*;
import lombok.*;

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


    @Override
    public String getMessage() {
        return this.getRespondent().getName() + " liked your post: " +  "\"" + this.getPost().getBody() + "\"";
    }

    @Override
    public int getSubscriberId() {
        return post.getAuthor().getId();
    }

}
