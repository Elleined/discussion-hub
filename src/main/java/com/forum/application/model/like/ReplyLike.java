package com.forum.application.model.like;

import com.forum.application.model.Reply;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_liked_reply")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class ReplyLike extends Like {

    @ManyToOne
    @JoinColumn(
            name = "reply_id",
            referencedColumnName = "reply_id"
    )
    private Reply reply;
}
