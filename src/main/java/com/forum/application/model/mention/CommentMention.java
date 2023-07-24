package com.forum.application.model.mention;

import com.forum.application.model.Comment;
import com.forum.application.model.NotificationStatus;
import com.forum.application.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_mention_comment")
@NoArgsConstructor
public class CommentMention extends Mentions {

    @ManyToOne
    @JoinColumn(
            name = "comment_id",
            referencedColumnName = "comment_id"
    )
    @Getter @Setter
    private Comment comment;

    @Builder(builderMethodName = "commentMentionBuilder")
    public CommentMention(int id, LocalDateTime createdAt, NotificationStatus notificationStatus, User mentionedUser, User mentioningUser) {
        super(id, createdAt, notificationStatus, mentionedUser, mentioningUser);
    }
}
