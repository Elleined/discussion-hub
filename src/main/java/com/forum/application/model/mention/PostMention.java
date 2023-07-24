package com.forum.application.model.mention;

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
@Table(name = "tbl_mention_post")
@NoArgsConstructor
public final class PostMention extends Mention {

    @ManyToOne
    @JoinColumn(
            name = "post_id",
            referencedColumnName = "post_id"
    )
    @Getter
    private Post post;

    @Builder(builderMethodName = "postMentionBuilder")
    public PostMention(int id, LocalDateTime createdAt, NotificationStatus notificationStatus, User mentionedUser, User mentioningUser, Post post) {
        super(id, createdAt, notificationStatus, mentionedUser, mentioningUser);
        this.post = post;
    }
}
