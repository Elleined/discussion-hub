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
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updateCommentUpvote = (commentId, newUpvoteCount, commentURI) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api${commentURI}/upvote/${commentId}`,
        data: {
            newUpvoteCount: newUpvoteCount
        },
        success: function(response) {
            deferred.resolve(response);
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
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updateCommentBody = (commentId, newCommentBody, commentURI) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api${commentURI}/body/${commentId}`,
        data: {
            newCommentBody: newCommentBody
        },
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const updateReplyBody = (replyId, newReplyBody, replyURI) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "PATCH",
        url: `/forum/api${replyURI}/body/${replyId}`,
        data: {
            newReplyBody: newReplyBody
        },
        success: function(response) {
            deferred.resolve(response);
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