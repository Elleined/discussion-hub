const getAllCommentsOf = commentURI => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: "/forum/api" + commentURI,
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(error);
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
            deferred.reject(error);
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
            deferred.reject(error);
        }
    });
    return deferred.promise();
};

const getAllUsernames = name => {
    const deferred = $.Deferred();
    $.ajax({
        type: "GET",
        url: `/forum/api/users/0/getAllByProperty`,
        data: {
            name: name
        },
        success: function(response) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(error);
        }
    });
    return deferred.promise();
};

export {
    getAllCommentsOf,
    getAllRepliesOf,
    getCommentSectionStatus,
    getAllUsernames
};