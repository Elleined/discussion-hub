const updateCommentSectionStatus = (postId, newStatus) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/commentSectionStatus/${postId}`,
        data: {
            newStatus: newStatus
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Comment section status updated successfully to " + newStatus);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updateCommentUpvote = (commentId, newUpvoteCount) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/0/comments/upvote/${commentId}`,
        data: {
            newUpvoteCount: newUpvoteCount
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Comment with id of " + commentId + " updated successfully with new upvote count of " + newUpvoteCount);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updatePostBody = (href, newPostBody) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api${href}`,
        data: {
            newPostBody: newPostBody
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Post updated successfully with new body of " + newPostBody);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updateCommentBody = (commentId, newCommentBody) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/0/comments/body/${commentId}`,
        data: {
            newCommentBody: newCommentBody
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Comment with id of " + commentId + " updated successfully with new comment body of " + newCommentBody);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updateReplyBody = (replyId, newReplyBody) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/comments/0/replies/body/${replyId}`,
        data: {
            newReplyBody: newReplyBody
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Reply with id of " + replyId + " updated successfully with new reply body of " + newReplyBody);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updateTotalNotificationCount = (userId, id, type) => {
    const deferred = $.Deferred();
    const url = type === "REPLY" ? "unreadReplyCountOfSpecificComment" : "unreadCommentCountOfSpecificPost";
    $.ajax({
        type: "GET",
        url: `forum/api/users/${userId}/${url}/${id}`,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

export {
    updateCommentSectionStatus,
    updateCommentUpvote,
    updatePostBody,
    updateCommentBody,
    updateReplyBody,
    updateTotalNotificationCount
};