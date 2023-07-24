package com.forum.application.model.like;

import com.forum.application.model.NotificationStatus;
import com.forum.application.model.Reply;
import com.forum.application.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_liked_reply")
@NoArgsConstructor
public final class ReplyLike extends Like {

    @ManyToOne
    @JoinColumn(
            name = "reply_id",
            referencedColumnName = "reply_id"
    )
    @Getter @Setter
    private Reply reply;

    @Builder(builderMethodName = "replyLikeBuilder")
    public ReplyLike(int id, User respondent, NotificationStatus notificationStatus, Reply reply) {
        super(id, respondent, notificationStatus);
        this.reply = reply;
    }
}