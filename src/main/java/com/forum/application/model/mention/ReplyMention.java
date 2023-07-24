package com.forum.application.model.mention;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_mention_reply")
@NoArgsConstructor
public final class ReplyMention extends Mention {

    @ManyToOne
    @JoinColumn(
            name = "reply_id",
            referencedColumnName = "reply_id"
    )
    @Getter @Setter
    private Reply reply;

    @Builder(builderMethodName = "replyMentionBuilder")
    public ReplyMention(int id, LocalDateTime createdAt, NotificationStatus notificationStatus, User mentionedUser, User mentioningUser, Reply reply) {
        super(id, createdAt, notificationStatus, mentionedUser, mentioningUser);
        this.reply = reply;
    }
}
