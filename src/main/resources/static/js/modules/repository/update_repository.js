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

const updateCommentUpvote = (commentId) => {
    $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/0/comments/upvote/${commentId}`,
        data: { commentId: commentId }
    }).promise();
};

const updatePostBody = (href, newPostBody) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api${href}`,
        data: { newPostBody: newPostBody },
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
    return $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/0/comments/body/${commentId}`,
        data: {
            newCommentBody: newCommentBody
        }
    }).promise();
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

export {
    updateCommentSectionStatus,
    updateCommentUpvote,
    updatePostBody,
    updateCommentBody,
    updateReplyBody,
};