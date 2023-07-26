const getAllCommentsOf = postId => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: `/forum/api/posts/${postId}/comments`,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const getPostBlock = postDto => {
    return $.ajax({
        type: "POST",
        url: "/forum/api/views/getPostBlock",
        contentType: "application/json",
        data: JSON.stringify(postDto)
    });
};

const getCommentBlock = commentDto => {
    return $.ajax({
        type: "POST",
        url: "/forum/api/views/getCommentBlock",
        contentType: "application/json",
        data: JSON.stringify(commentDto)
    }).promise();
};

const getAllRepliesOf = commentId => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: `/forum/api/posts/comments/${commentId}/replies`,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, response, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const getReplyBlock = replyDto => {
    return $.ajax({
        type: "POST",
        url: "/forum/api/views/getReplyBlock",
        contentType: "application/json",
        data: JSON.stringify(replyDto)
    }).promise();
};

const getNotificationBlock = notificationResponse => {
    return $.ajax({
        type: "POST",
        url: "/forum/api/views/getNotificationBlock",
        contentType: "application/json",
        data: JSON.stringify(notificationResponse)
    }).promise();
};

const getMentionBlock = notificationResponse => {
    return $.ajax({
        type: "POST",
        url: "/forum/api/views/getMentionBlock",
        contentType: "application/json",
        data: JSON.stringify(notificationResponse)
    }).promise();
};

const getCommentSectionStatus = postId => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: `/forum/api/posts/commentSectionStatus/${postId}`,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const getSuggestedMentions = (userId, name) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: `/forum/api/users/${userId}/getSuggestedMentions`,
        data: {
            name: name
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

const isUserBlocked = (userId, userToCheckId) => {
    let blockedBy, youBeenBlockedBy;
    isBlockedBy(userId, userToCheckId).done(function(data) {
        blockedBy = data == true ? true : false;
    });
    isYouBeenBlockedBy(userId, userToCheckId).done(function(data) {
        youBeenBlockedBy = data == true ? true : false;
    });
    return blockedBy || youBeenBlockedBy;
};

const getPostById = postId => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: "/forum/api/posts/" + postId,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });

    return deferred.promise();
};

const getCommentById = commentId => {
    return $.ajax({
        type: "GET",
        url: `/forum/api/posts/0/comments/${commentId}`
    }).promise();
};

const getAllNotification = currentUserId => {
    return $.ajax({
        type: "GET",
        url: `/forum/api/users/${currentUserId}/getAllNotification`
    }).promise();
};

const getLikeIcon = isLiked => {
    return $.ajax({
        type: "POST",
        url: "/forum/api/views/getLikeIcon",
        data: { isLiked: isLiked }
    }).promise();
};

// Use isUserBlocked method instead
const isBlockedBy = (userId, userToCheckId) => {
    return $.ajax({
        type: "GET",
        url: `/forum/api/users/${userId}/isBlockedBy/${userToCheckId}`,
        async: false,
        success: function(isBlockedBy, response) {
            console.log("Is " + userToCheckId + " blocked by " + userId + ": " + isBlockedBy);
        },
        error: function(xhr, status, response) {
            alert("Error Occurred! is user blocked failed to fetch!")
        }
    });
}

// Use isUserBlocked method instead
const isYouBeenBlockedBy = (userId, suspectedBlockerId) => {
    return $.ajax({
        type: "GET",
        url: `/forum/api/users/${userId}/isYouBeenBlockedBy/${suspectedBlockerId}`,
        async: false,
        success: function(isYouBeenBlockedBy, response) {
            console.log("Is you been blocked by " + userId + ": " + isYouBeenBlockedBy);
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Is you been blocked by failed to fetch!");
        }
    });
};

export {
    getPostById,
    getCommentById,
    getAllCommentsOf,
    getCommentBlock,
    getAllRepliesOf,
    getReplyBlock,
    getNotificationBlock,
    getMentionBlock,
    getCommentSectionStatus,
    getSuggestedMentions,
    isUserBlocked,
    getAllNotification,
    getLikeIcon
};