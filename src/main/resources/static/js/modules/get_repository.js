const getAllCommentsOf = commentURI => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: "/forum/api" + commentURI,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const getAllRepliesOf = replyURI => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: "/forum/api" + replyURI,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, response, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
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

const getAllUsernames = (userId, name) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: `/forum/api/users/${userId}/getAllByProperty`,
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

const getCommentById = (commentId, commentURI) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: "/forum/api" + commentURI + "/" + commentId,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
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
}

export {
    getPostById,
    getCommentById,
    getAllCommentsOf,
    getAllRepliesOf,
    getCommentSectionStatus,
    getAllUsernames,
    isUserBlocked,
};