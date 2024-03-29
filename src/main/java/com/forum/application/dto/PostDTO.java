package com.forum.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class PostDTO {
    private int id;
    private String body;
    private LocalDateTime dateCreated;
    private String formattedDateCreated;
    private String formattedTimeCreated;
    private int authorId;
    private String authorName;
    private String authorPicture;
    private int totalCommentAndReplies;
    private String status;
    private String commentSectionStatus;
    private int totalLikes;
    private String attachedPicture;
    private boolean isCurrentUserLikedPost;
    private Set<UserDTO> likers;
    private Set<UserDTO> mentionedUsers;
}
