const updateCommentSectionStatus = (postId, newStatus) => {
    return $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/commentSectionStatus/${postId}`,
        data: { newStatus: newStatus }
    }).promise();
};

const updateCommentUpvote = (commentId) => {
    $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/0/comments/upvote/${commentId}`,
        data: { commentId: commentId }
    }).promise();
};

const updatePostBody = (postId, newPostBody) => {
    return $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/body/${postId}`,
        data: { newPostBody: newPostBody }
    }).promise();
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